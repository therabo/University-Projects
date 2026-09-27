const Appearance = require('../models/appearance');
const Game = require('../models/game');

const round = (value) => Math.round(value * 100) / 100;
const emptyTotals = () => ({
  appearances: 0, minutes: 0, goals: 0, assists: 0,
  goalContributions: 0, yellowCards: 0, redCards: 0,
  goalsPer90: 0, assistsPer90: 0, contributionsPer90: 0,
  minutesPerAppearance: 0
});

function addAppearance(totals, appearance) {
  totals.appearances += 1;
  totals.minutes += appearance.minutes_played || 0;
  totals.goals += appearance.goals || 0;
  totals.assists += appearance.assists || 0;
  totals.yellowCards += appearance.yellow_cards || 0;
  totals.redCards += appearance.red_cards || 0;
}

function finalize(totals) {
  totals.goalContributions = totals.goals + totals.assists;
  if (totals.minutes > 0) {
    totals.goalsPer90 = round(totals.goals * 90 / totals.minutes);
    totals.assistsPer90 = round(totals.assists * 90 / totals.minutes);
    totals.contributionsPer90 = round(totals.goalContributions * 90 / totals.minutes);
  }
  if (totals.appearances > 0) {
    totals.minutesPerAppearance = round(totals.minutes / totals.appearances);
  }
  return totals;
}

/**
 * Creates the player statistics controller with the required models.
 * @param {Object} appearanceModel - Appearance model.
 * @param {Object} gameModel - Match model.
 * @returns {Function} - getPlayerStats(playerId, requestedSeason, scope) handler.
 */
function createPlayerStatsController(appearanceModel, gameModel) {
  /**
   * Aggregates appearances, goals, and assists by season and competition scope.
   * @param {number} playerId - Player identifier.
   * @param {number|null} [requestedSeason=null] - Start year, or null for the latest season.
   * @param {string} [scope=all] - Either all or league.
   * @returns {Promise<Object>} - Seasonal summary, history, and data coverage.
   * @throws {RangeError} - Invalid scope or unavailable season.
   */
  return async function getPlayerStats(playerId, requestedSeason = null, scope = 'all') {
    if (!['all', 'league'].includes(scope)) {
      const error = new RangeError('Invalid competition scope');
      error.status = 400;
      throw error;
    }
    const appearances = await appearanceModel.find({ player_id: playerId })
      .select('game_id date goals assists minutes_played yellow_cards red_cards -_id').lean();
    const gameIds = [...new Set(appearances.map((row) => row.game_id))];
    const games = gameIds.length ? await gameModel.find({ game_id: { $in: gameIds } })
      .select('game_id season competition_type -_id').lean() : [];
    const gamesById = new Map(games.map((game) => [game.game_id, game]));
    const linked = appearances.map((appearance) => ({ appearance, game: gamesById.get(appearance.game_id) }))
      .filter((row) => row.game && Number.isInteger(row.game.season));
    const availableSeasons = [...new Set(linked.map((row) => row.game.season))]
      .sort((left, right) => right - left);
    const season = requestedSeason ?? availableSeasons[0] ?? null;
    if (season !== null && !availableSeasons.includes(season)) {
      const error = new RangeError('Unavailable player season');
      error.status = 400;
      throw error;
    }

    const scoped = linked.filter((row) => scope === 'all' || row.game.competition_type === 'domestic_league');
    const seasons = availableSeasons.map((year) => {
      const totals = emptyTotals();
      for (const row of scoped) if (row.game.season === year) addAppearance(totals, row.appearance);
      return { season: year, ...finalize(totals) };
    });
    const summary = seasons.find((row) => row.season === season) || { season, ...emptyTotals() };
    const dates = scoped.filter((row) => row.game.season === season)
      .map((row) => new Date(row.appearance.date).toISOString().slice(0, 10)).sort();
    return {
      playerId, season, scope, availableSeasons,
      coverage: {
        firstAppearanceDate: dates[0] || null,
        lastAppearanceDate: dates.at(-1) || null,
        unmatchedAppearances: appearances.length - linked.length
      },
      summary, seasons
    };
  };
}

module.exports = {
  createPlayerStatsController,
  getPlayerStats: createPlayerStatsController(Appearance, Game)
};
