const express = require('express');
const { clients, sendUpstreamError } = require('../services/upstream');

const router = express.Router();

/**
 * Lists positions available for player search.
 * @name GET/get_role
 * @function
 * @returns {Array} - Positions available in the catalog.
 */
router.get('/get_role', proxyCatalogGet('/get_role'));

/**
 * Lists birth years available for player search.
 * @name GET/get_birth_years
 * @function
 * @returns {Array} - Birth years available in the catalog.
 */
router.get('/get_birth_years', proxyCatalogGet('/get_birth_years'));

/**
 * Runs an advanced player search with the requested filters.
 * @name POST/advanced_search
 * @function
 * @param {Object} body - Search filters forwarded to the catalog.
 * @returns {Array} - Matching players or an error.
 */
router.post('/advanced_search', async (req, res) => {
  try {
    const response = await clients.catalog.post('/advanced_search', req.body);
    res.json(response.data);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

/**
 * Loads information for a player searched by name.
 * @name POST/info_player
 * @function
 * @param {string} playerName - Player name in the request body.
 * @returns {Object} - Player information or an error.
 */
router.post('/info_player', async (req, res) => {
  try {
    const params = new URLSearchParams({ Name: req.body.playerName });
    const response = await clients.catalog.post('/info_player', params);
    res.json(response.data);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

/**
 * Loads the player's biographical profile from the catalog.
 * @name GET/api/players/:playerId/profile
 * @function
 * @param {number} playerId - Player identifier in the URL path.
 * @returns {Object} - Player profile or an error.
 */
router.get('/api/players/:playerId/profile', async (req, res) => {
  const playerId = Number(req.params.playerId);
  if (!/^[1-9][0-9]*$/.test(req.params.playerId) || !Number.isSafeInteger(playerId)) {
    return res.status(400).json({ error: 'Invalid player ID' });
  }
  try {
    const response = await clients.catalog.get(`/players/${playerId}/profile`);
    res.json(response.data);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

/**
 * Loads the player's seasonal statistics from the match service.
 * @name GET/api/players/:playerId/stats
 * @function
 * @param {number} playerId - Player identifier in the URL path.
 * @param {number} [season] - Requested season start year.
 * @param {string} [scope=all] - Either all or league.
 * @returns {Object} - Player statistics or an error.
 */
router.get('/api/players/:playerId/stats', async (req, res) => {
  const playerId = Number(req.params.playerId);
  const season = req.query.season;
  const scope = req.query.scope ?? 'all';
  if (!/^[1-9][0-9]*$/.test(req.params.playerId) || !Number.isSafeInteger(playerId)
      || (season !== undefined && !/^(19|20)[0-9]{2}$/.test(season))
      || !['all', 'league'].includes(scope)) {
    return res.status(400).json({ error: 'Invalid player, season or scope' });
  }
  try {
    const response = await clients.match.get(`/players/${playerId}/stats`, { params: { season, scope } });
    res.json(response.data);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

/**
 * Loads the player's historical valuation series.
 * @name GET/api/players/:playerId/valuations
 * @function
 * @param {number} playerId - Player identifier in the URL path.
 * @returns {Object} - Valuations and summary, or an error.
 */
router.get('/api/players/:playerId/valuations', async (req, res) => {
  const playerId = Number(req.params.playerId);
  if (!/^[1-9][0-9]*$/.test(req.params.playerId) || !Number.isSafeInteger(playerId)) {
    return res.status(400).json({ error: 'Invalid player ID' });
  }
  try {
    const response = await clients.analytics.get(`/players/${playerId}/valuations`);
    res.set('Cache-Control', response.headers['cache-control'] || 'no-cache');
    res.json(response.data);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

function proxyCatalogGet(path) {
  return async (req, res) => {
    try {
      const response = await clients.catalog.get(path);
      res.json(response.data);
    } catch (error) {
      sendUpstreamError(res, error);
    }
  };
}

module.exports = router;
