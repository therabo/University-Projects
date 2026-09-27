const Game = require('../models/game');
const Appearance = require('../models/appearance');

/**
 * Creates the seasonal roster controller with match and appearance models.
 * @param {Object} gameModel - Match model.
 * @param {Object} appearanceModel - Appearance model.
 * @returns {Function} - getClubRoster(clubId, requestedSeason) handler.
 */
function createClubRosterController(gameModel, appearanceModel) {
  /**
   * Derives players who actually appeared in the club's seasonal matches.
   * @param {number} clubId - Club identifier.
   * @param {number|null} requestedSeason - Start year, or null for the latest season.
   * @returns {Promise<Object>} - Selected season, available seasons, and players.
   * @throws {RangeError} - Season unavailable for this club.
   */
  return async function getClubRoster(clubId, requestedSeason) {
    const clubGames = { $or: [{ home_club_id: clubId }, { away_club_id: clubId }] };
    const availableSeasons = [...new Set((await gameModel.distinct('season', clubGames))
      .filter((year) => Number.isInteger(year) && year >= 1900 && year <= 2100))]
      .sort((left, right) => right - left);

    if (availableSeasons.length === 0) {
      return { season: null, availableSeasons, players: [] };
    }

    const season = requestedSeason ?? availableSeasons[0];
    if (!availableSeasons.includes(season)) {
      const error = new RangeError('Unavailable club season');
      error.status = 400;
      throw error;
    }

    const games = await gameModel.find({ ...clubGames, season })
      .select('game_id -_id').lean();
    const gameIds = games.map((game) => game.game_id);
    if (gameIds.length === 0) {
      return { season, availableSeasons, players: [] };
    }

    const appearances = await appearanceModel.find({
      player_club_id: clubId,
      game_id: { $in: gameIds }
    }).select('player_id player_name -_id').lean();

    const playersById = new Map();
    for (const appearance of appearances) {
      if (Number.isSafeInteger(appearance.player_id) && appearance.player_id > 0) {
        playersById.set(appearance.player_id, {
          playerId: appearance.player_id,
          name: appearance.player_name || `Giocatore ${appearance.player_id}`
        });
      }
    }
    const players = [...playersById.values()].sort((left, right) =>
      left.name.localeCompare(right.name, 'it'));
    return { season, availableSeasons, players };
  };
}

module.exports = {
  createClubRosterController,
  getClubRoster: createClubRosterController(Game, Appearance)
};
