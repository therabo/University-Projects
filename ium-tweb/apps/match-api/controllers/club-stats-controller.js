const Game = require('../models/game');
const ClubGame = require('../models/club-game');
const Appearance = require('../models/appearance');

const round = (value) => Math.round(value * 100) / 100;
const dateKey = (value) => new Date(value).toISOString().slice(0, 10);

function emptySplit() {
  return { matches: 0, wins: 0, draws: 0, losses: 0, goalsFor: 0, goalsAgainst: 0 };
}

/**
 * Creates the club statistics controller with the required models.
 * @param {Object} gameModel - Match model.
 * @param {Object} clubGameModel - Club result model.
 * @param {Object} appearanceModel - Appearance model.
 * @returns {Function} - getClubSeasonStats(clubId, requestedSeason, scope) handler.
 */
function createClubStatsController(gameModel, clubGameModel, appearanceModel) {
  /**
   * Aggregates results, attack, defense, and the 25 most recent seasonal matches.
   * @param {number} clubId - Club identifier.
   * @param {number|null} requestedSeason - Start year, or null for the latest season.
   * @param {string} [scope=league] - Either league or all.
   * @returns {Promise<Object>} - Seasonal statistics and data coverage.
   * @throws {RangeError} - Invalid scope or unavailable season.
   */
  return async function getClubSeasonStats(clubId, requestedSeason, scope = 'league') {
    if (!['league', 'all'].includes(scope)) {
      const error = new RangeError('Invalid competition scope');
      error.status = 400;
      throw error;
    }

    const clubFilter = { $or: [{ home_club_id: clubId }, { away_club_id: clubId }] };
    const availableSeasons = [...new Set((await gameModel.distinct('season', clubFilter))
      .filter((year) => Number.isInteger(year) && year >= 1900 && year <= 2100))]
      .sort((left, right) => right - left);
    const season = requestedSeason ?? availableSeasons[0] ?? null;
    if (season !== null && !availableSeasons.includes(season)) {
      const error = new RangeError('Unavailable club season');
      error.status = 400;
      throw error;
    }

    const result = {
      clubId, season, scope, availableScopes: ['league', 'all'],
      coverage: { firstMatchDate: null, lastMatchDate: null, missingResults: 0 },
      summary: {
        ...emptySplit(), goalDifference: 0, cleanSheets: 0,
        winRate: 0, goalsPerMatch: 0, concededPerMatch: 0,
        home: emptySplit(), away: emptySplit()
      },
      attack: { playerGoals: 0, assists: 0, playersUsed: 0, topScorers: [], topAssistProviders: [] },
      defense: { yellowCards: 0, redCards: 0 },
      recentMatches: []
    };
    if (season === null) return result;

    const games = await gameModel.find({
      ...clubFilter,
      season,
      ...(scope === 'league' ? { competition_type: 'domestic_league' } : {})
    }).select('game_id date home_club_id away_club_id home_club_name away_club_name competition_type -_id').lean();
    if (games.length === 0) return result;

    const gameIds = games.map((game) => game.game_id);
    const clubGames = await clubGameModel.find({ club_id: clubId, game_id: { $in: gameIds } })
      .select('game_id own_goals opponent_goals -_id').lean();
    const resultsByGame = new Map(clubGames.map((row) => [row.game_id, row]));
    const recorded = [];

    for (const game of games) {
      const score = resultsByGame.get(game.game_id);
      if (!score || !Number.isFinite(score.own_goals) || !Number.isFinite(score.opponent_goals)) {
        result.coverage.missingResults += 1;
        continue;
      }
      const isHome = game.home_club_id === clubId;
      const split = isHome ? result.summary.home : result.summary.away;
      const outcome = score.own_goals > score.opponent_goals ? 'win'
        : score.own_goals < score.opponent_goals ? 'loss' : 'draw';
      const key = outcome === 'win' ? 'wins' : outcome === 'loss' ? 'losses' : 'draws';
      result.summary.matches += 1;
      result.summary[key] += 1;
      result.summary.goalsFor += score.own_goals;
      result.summary.goalsAgainst += score.opponent_goals;
      if (score.opponent_goals === 0) result.summary.cleanSheets += 1;
      split.matches += 1;
      split[key] += 1;
      split.goalsFor += score.own_goals;
      split.goalsAgainst += score.opponent_goals;
      recorded.push({
        gameId: game.game_id,
        date: dateKey(game.date),
        opponent: isHome ? game.away_club_name : game.home_club_name,
        venue: isHome ? 'home' : 'away',
        outcome,
        goalsFor: score.own_goals,
        goalsAgainst: score.opponent_goals,
        competitionType: game.competition_type
      });
    }

    if (recorded.length === 0) return result;
    recorded.sort((left, right) => left.date.localeCompare(right.date) || left.gameId - right.gameId);
    result.coverage.firstMatchDate = recorded[0].date;
    result.coverage.lastMatchDate = recorded[recorded.length - 1].date;
    result.recentMatches = recorded.slice(-25).reverse();
    const summary = result.summary;
    summary.goalDifference = summary.goalsFor - summary.goalsAgainst;
    summary.winRate = round(summary.wins / summary.matches * 100);
    summary.goalsPerMatch = round(summary.goalsFor / summary.matches);
    summary.concededPerMatch = round(summary.goalsAgainst / summary.matches);

    const appearances = await appearanceModel.find({
      player_club_id: clubId,
      game_id: { $in: recorded.map((game) => game.gameId) }
    }).select('player_id player_name goals assists minutes_played yellow_cards red_cards -_id').lean();
    const playersById = new Map();
    for (const appearance of appearances) {
      result.attack.playerGoals += appearance.goals || 0;
      result.attack.assists += appearance.assists || 0;
      result.defense.yellowCards += appearance.yellow_cards || 0;
      result.defense.redCards += appearance.red_cards || 0;
      if (!Number.isSafeInteger(appearance.player_id) || appearance.player_id <= 0) continue;
      const player = playersById.get(appearance.player_id) || {
        playerId: appearance.player_id,
        name: appearance.player_name || `Giocatore ${appearance.player_id}`,
        appearances: 0, minutes: 0, goals: 0, assists: 0
      };
      player.appearances += 1;
      player.minutes += appearance.minutes_played || 0;
      player.goals += appearance.goals || 0;
      player.assists += appearance.assists || 0;
      playersById.set(player.playerId, player);
    }
    const players = [...playersById.values()];
    result.attack.playersUsed = players.length;
    result.attack.topScorers = players.filter((player) => player.goals > 0)
      .sort((left, right) => right.goals - left.goals || right.assists - left.assists
        || left.name.localeCompare(right.name, 'it')).slice(0, 3);
    result.attack.topAssistProviders = players.filter((player) => player.assists > 0)
      .sort((left, right) => right.assists - left.assists || right.goals - left.goals
        || left.name.localeCompare(right.name, 'it')).slice(0, 3);
    return result;
  };
}

module.exports = {
  createClubStatsController,
  getClubSeasonStats: createClubStatsController(Game, ClubGame, Appearance)
};
