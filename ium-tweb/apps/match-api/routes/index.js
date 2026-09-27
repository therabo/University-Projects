const express = require('express');
const router = express.Router();
const gameController = require('../controllers/game-controller');
const clubGameController = require('../controllers/club-game-controller');
const clubRosterController = require('../controllers/club-roster-controller');
const clubStatsController = require('../controllers/club-stats-controller');
const playerStatsController = require('../controllers/player-stats-controller');
const { mongoose } = require('../database/connection');

/**
 * Checks the connection to the match database.
 * @name GET/health
 * @function
 * @returns {Object} - Service status (HTTP 200 or 503).
 */
router.get('/health', async function (req, res) {
  try {
    await mongoose.connection.db.admin().ping();
    res.json({ status: 'ok', service: 'match-api' });
  } catch (error) {
    res.status(503).json({ status: 'error', service: 'match-api' });
  }
});

/**
 * Returns player statistics for a season and competition scope.
 * @name GET/players/:playerId/stats
 * @function
 * @param {number} playerId - Player identifier in the URL path.
 * @param {number} [season] - Requested season start year.
 * @param {string} [scope=all] - Either all or league.
 * @returns {Object} - Calculated statistics or a validation error.
 */
router.get('/players/:playerId/stats', async function (req, res) {
  const playerId = Number(req.params.playerId);
  const season = req.query.season === undefined ? null : Number(req.query.season);
  const scope = req.query.scope === undefined ? 'all' : req.query.scope;
  if (!/^[1-9][0-9]*$/.test(req.params.playerId) || !Number.isSafeInteger(playerId)
      || (season !== null && !/^(19|20)[0-9]{2}$/.test(req.query.season))
      || !['all', 'league'].includes(scope)) {
    return res.status(400).json({ error: 'Invalid player, season or scope' });
  }
  try {
    return res.json(await playerStatsController.getPlayerStats(playerId, season, scope));
  } catch (error) {
    if (error.status === 400) return res.status(400).json({ error: error.message });
    console.error('Failed to load player statistics:', error);
    return res.status(500).json({ error: 'Player statistics unavailable' });
  }
});

/**
 * Derives a club roster from appearances recorded in the season.
 * @name GET/clubs/:clubId/roster
 * @function
 * @param {number} clubId - Club identifier in the URL path.
 * @param {number} [season] - Requested season start year.
 * @returns {Object} - Roster and available seasons, or an error.
 */
router.get('/clubs/:clubId/roster', async function (req, res) {
  const clubId = Number(req.params.clubId);
  const season = req.query.season === undefined ? null : Number(req.query.season);
  if (!Number.isSafeInteger(clubId) || clubId <= 0
      || (season !== null && (!Number.isInteger(season) || season < 1900 || season > 2100))) {
    return res.status(400).json({ error: 'Invalid club or season' });
  }
  try {
    return res.json(await clubRosterController.getClubRoster(clubId, season));
  } catch (error) {
    if (error.status === 400) return res.status(400).json({ error: error.message });
    console.error('Failed to load seasonal roster:', error);
    return res.status(500).json({ error: 'Seasonal roster unavailable' });
  }
});

/**
 * Returns a club's seasonal statistics.
 * @name GET/clubs/:clubId/stats
 * @function
 * @param {number} clubId - Club identifier in the URL path.
 * @param {number} [season] - Requested season start year.
 * @param {string} [scope=league] - Either league or all.
 * @returns {Object} - Calculated statistics or a validation error.
 */
router.get('/clubs/:clubId/stats', async function (req, res) {
  const clubId = Number(req.params.clubId);
  const season = req.query.season === undefined ? null : Number(req.query.season);
  const scope = req.query.scope === undefined ? 'league' : req.query.scope;
  if (!/^[1-9][0-9]*$/.test(req.params.clubId) || !Number.isSafeInteger(clubId)
      || (season !== null && !/^(19|20)[0-9]{2}$/.test(req.query.season))
      || !['league', 'all'].includes(scope)) {
    return res.status(400).json({ error: 'Invalid club, season or scope' });
  }
  try {
    return res.json(await clubStatsController.getClubSeasonStats(clubId, season, scope));
  } catch (error) {
    if (error.status === 400) return res.status(400).json({ error: error.message });
    console.error('Failed to load seasonal statistics:', error);
    return res.status(500).json({ error: 'Seasonal statistics unavailable' });
  }
});

/**
 * Loads the 10 most recent matches.
 * @name GET/loadHP
 * @function
 * @param {string} path - Route endpoint ("/loadHP").
 * @param {callback} middleware - Asynchronous request and response handler.
 * @throws {Error} - Returns HTTP 500 if the request fails.
 */
router.get('/loadHP', async function (req, res, next) {
  try {
    const recentGame = await gameController.getRecentGames();

    res.json(recentGame);
  } catch (error) {
    console.error('Errore durante la richiesta dei giochi:', error);
    res.status(500).json({ error: 'Errore durante la richiesta dei giochi' });
  }
});
/**
 * Returns statistics for a specific club.
 * @name POST/squad_stats
 * @function
 * @param {string} path - Route endpoint ("/squad_stats").
 * @param {callback} middleware - Asynchronous request and response handler.
 * @throws {Error} - Returns HTTP 500 if the request fails.
 */
router.post('/squad_stats', async function (req, res, next) {
  try {
    const squadName = req.body.squad;

    const squadId = await gameController.getClubIdByClubName(squadName);

    const squadStats = await clubGameController.getClubStats(squadId);

    res.json(squadStats);
  } catch (error) {
    console.error('Error:', error);
    res.status(500).send('Internal Server Error');
  }
});
module.exports = router;
