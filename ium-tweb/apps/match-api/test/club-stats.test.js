const test = require('node:test');
const assert = require('node:assert/strict');
const { createClubStatsController } = require('../controllers/club-stats-controller');

function query(rows) {
  return { select() { return this; }, async lean() { return rows; } };
}

test('scopes results by club, season and competition and derives outcomes from scores', async () => {
  let gamesFilter;
  let resultsFilter;
  let appearancesFilter;
  const games = {
    async distinct() { return [2022, 2023]; },
    find(filter) {
      gamesFilter = filter;
      return query([
        { game_id: 1, date: '2023-08-20', home_club_id: 506, away_club_id: 10,
          home_club_name: 'Juventus', away_club_name: 'Team A', competition_type: 'domestic_league' },
        { game_id: 2, date: '2023-08-27', home_club_id: 11, away_club_id: 506,
          home_club_name: 'Team B', away_club_name: 'Juventus', competition_type: 'domestic_league' },
        { game_id: 3, date: '2023-09-01', home_club_id: 506, away_club_id: 12,
          home_club_name: 'Juventus', away_club_name: 'Team C', competition_type: 'domestic_league' }
      ]);
    }
  };
  const results = {
    find(filter) {
      resultsFilter = filter;
      return query([
        { game_id: 1, own_goals: 2, opponent_goals: 0 },
        { game_id: 2, own_goals: 1, opponent_goals: 1 },
        { game_id: 3, own_goals: 0, opponent_goals: 2 }
      ]);
    }
  };
  const appearances = {
    find(filter) {
      appearancesFilter = filter;
      return query([
        { player_id: 4, player_name: 'Player A', goals: 1, assists: 1,
          minutes_played: 90, yellow_cards: 1, red_cards: 0 },
        { player_id: 4, player_name: 'Player A', goals: 1, assists: 0,
          minutes_played: 80, yellow_cards: 0, red_cards: 0 },
        { player_id: 5, player_name: 'Player B', goals: 0, assists: 1,
          minutes_played: 60, yellow_cards: 0, red_cards: 1 }
      ]);
    }
  };
  const data = await createClubStatsController(games, results, appearances)(506, 2023, 'league');
  assert.equal(gamesFilter.season, 2023);
  assert.equal(gamesFilter.competition_type, 'domestic_league');
  assert.deepEqual(resultsFilter, { club_id: 506, game_id: { $in: [1, 2, 3] } });
  assert.deepEqual(appearancesFilter, { player_club_id: 506, game_id: { $in: [1, 2, 3] } });
  assert.deepEqual(data.summary.home, {
    matches: 2, wins: 1, draws: 0, losses: 1, goalsFor: 2, goalsAgainst: 2
  });
  assert.deepEqual(data.summary.away, {
    matches: 1, wins: 0, draws: 1, losses: 0, goalsFor: 1, goalsAgainst: 1
  });
  assert.equal(data.summary.draws, 1);
  assert.equal(data.summary.losses, 1);
  assert.equal(data.summary.cleanSheets, 1);
  assert.equal(data.summary.goalDifference, 0);
  assert.equal(data.summary.winRate, 33.33);
  assert.equal(data.attack.playersUsed, 2);
  assert.equal(data.attack.playerGoals, 2);
  assert.equal(data.attack.assists, 2);
  assert.equal(data.attack.topScorers[0].name, 'Player A');
  assert.equal(data.attack.topScorers[0].minutes, 170);
  assert.equal(data.defense.yellowCards, 1);
  assert.equal(data.defense.redCards, 1);
  assert.equal(data.recentMatches[1].venue, 'away');
  assert.equal(data.recentMatches[1].outcome, 'draw');
});

test('reports missing results and never counts a match without a valid score', async () => {
  const games = {
    async distinct() { return [2023]; },
    find() { return query([{ game_id: 7, date: '2023-10-01', home_club_id: 506,
      away_club_id: 10, competition_type: 'domestic_cup' }]); }
  };
  const results = { find() { return query([]); } };
  const appearances = { find() { throw new Error('No scored matches: do not query appearances'); } };
  const data = await createClubStatsController(games, results, appearances)(506, 2023, 'all');
  assert.equal(data.summary.matches, 0);
  assert.equal(data.coverage.missingResults, 1);
  assert.deepEqual(data.recentMatches, []);
});

test('returns at most 25 recent matches, newest first, without truncating season totals', async () => {
  const games = Array.from({ length: 30 }, (_, index) => ({
    game_id: index + 1,
    date: `2023-01-${String(index + 1).padStart(2, '0')}`,
    home_club_id: 506,
    away_club_id: 10,
    away_club_name: 'Team A',
    competition_type: 'domestic_league'
  }));
  const scores = games.map((game) => ({
    game_id: game.game_id,
    own_goals: 1,
    opponent_goals: 0
  }));
  const gameModel = { async distinct() { return [2023]; }, find() { return query(games); } };
  const clubGameModel = { find() { return query(scores); } };
  const appearanceModel = { find() { return query([]); } };

  const data = await createClubStatsController(gameModel, clubGameModel, appearanceModel)(506, 2023);
  assert.equal(data.summary.matches, 30);
  assert.equal(data.recentMatches.length, 25);
  assert.equal(data.recentMatches[0].gameId, 30);
  assert.equal(data.recentMatches.at(-1).gameId, 6);
});

test('returns empty state for absent clubs and rejects unsupported seasons and scopes', async () => {
  const games = { async distinct() { return []; }, find() { throw new Error('Unexpected game query'); } };
  const unused = { find() { throw new Error('Unexpected query'); } };
  const getStats = createClubStatsController(games, unused, unused);
  const empty = await getStats(999, null, 'league');
  assert.equal(empty.season, null);
  assert.equal(empty.summary.matches, 0);
  await assert.rejects(getStats(999, 2022, 'league'), { status: 400 });
  await assert.rejects(getStats(999, null, 'cup'), { status: 400 });
});
