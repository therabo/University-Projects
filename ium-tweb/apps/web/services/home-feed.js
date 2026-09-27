const upcomingMatches = require('../data/upcoming-matches');
const { clients } = require('./upstream');

function collectClubIds(recentResults) {
  const clubIds = new Set();

  for (const fixture of upcomingMatches) {
    clubIds.add(fixture.homeClubId);
    clubIds.add(fixture.awayClubId);
  }

  for (const result of recentResults) {
    clubIds.add(result.home_club_id);
    clubIds.add(result.away_club_id);
  }

  return [...clubIds].filter(Number.isInteger);
}

async function fetchCrests(clubIds) {
  try {
    const response = await clients.catalog.get('/clubs/crests', {
      params: { ids: clubIds.join(',') }
    });

    return new Map(response.data.map((club) => [club.clubId, club]));
  } catch (error) {
    console.warn('Club crest metadata unavailable:', error.message);
    return new Map();
  }
}

function toTeam(clubId, displayName, crestByClubId) {
  const catalogueClub = crestByClubId.get(clubId);
  return {
    clubId,
    name: displayName || catalogueClub?.name || 'Squadra non disponibile',
    crestUrl: catalogueClub?.crestUrl || null
  };
}

async function getHomeFeed() {
  const matchResponse = await clients.match.get('/loadHP');
  const recentResults = matchResponse.data;
  const crestByClubId = await fetchCrests(collectClubIds(recentResults));

  return {
    upcomingMatches: upcomingMatches.map((fixture) => ({
      date: fixture.date,
      time: fixture.time,
      homeTeam: toTeam(fixture.homeClubId, fixture.homeClubName, crestByClubId),
      awayTeam: toTeam(fixture.awayClubId, fixture.awayClubName, crestByClubId)
    })),
    recentResults: recentResults.map((result) => ({
      id: result._id,
      stadium: result.stadium,
      score: result.aggregate,
      homeTeam: toTeam(result.home_club_id, result.home_club_name, crestByClubId),
      awayTeam: toTeam(result.away_club_id, result.away_club_name, crestByClubId)
    }))
  };
}

module.exports = { getHomeFeed };
