const express = require('express');
const { clients, sendUpstreamError } = require('../services/upstream');

const router = express.Router();

/**
 * Lists seasons available in the catalog.
 * @name GET/seasons
 * @function
 * @returns {Array} - Available seasons.
 */
router.get('/seasons', proxyGet('/seasons'));

/**
 * Lists countries available in the catalog.
 * @name GET/country
 * @function
 * @returns {Array} - Available countries.
 */
router.get('/country', proxyGet('/country'));

/**
 * Lists competitions available in the catalog.
 * @name GET/list_competitions
 * @function
 * @returns {Array} - Available competitions.
 */
router.get('/list_competitions', proxyGet('/list_competitions'));

/**
 * Forwards the competition-name request to the catalog.
 * @name GET/list_competitions_SoloName
 * @function
 * @returns {Array} - Catalog response or a service error.
 */
router.get('/list_competitions_SoloName', proxyGet('/list_competitions_SoloName'));

/**
 * Loads a competition's PNG logo from the catalog.
 * @name GET/competitions/:id/logo
 * @function
 * @param {string} id - Competition code in the URL path.
 * @returns {Buffer} - PNG logo or a service error.
 */
router.get('/competitions/:id/logo', async (req, res) => {
  if (!/^[A-Z0-9]{1,12}$/.test(req.params.id)) {
    return res.sendStatus(404);
  }
  try {
    const response = await clients.catalog.get(`/competitions/${req.params.id}/logo`, {
      responseType: 'arraybuffer'
    });
    res.set('Content-Type', 'image/png');
    res.set('Cache-Control', response.headers['cache-control'] || 'public, max-age=86400');
    res.set('X-Content-Type-Options', 'nosniff');
    return res.send(Buffer.from(response.data));
  } catch (error) {
    return sendUpstreamError(res, error);
  }
});

/**
 * Records a completed competition selection.
 * @name POST/competitions/selections
 * @function
 * @param {Object} body - Selected competition code.
 * @returns {number} - HTTP 204 status or a catalog error.
 */
router.post('/competitions/selections', async (req, res) => {
  try {
    await clients.catalog.post('/competitions/selections', req.body);
    res.sendStatus(204);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

/**
 * Returns details of the requested competition.
 * @name POST/info_competition
 * @function
 * @param {string} comp - Competition code in the request body.
 * @returns {Object} - Competition details or an error.
 */
router.post('/info_competition', async (req, res) => {
  try {
    const params = new URLSearchParams({ competition: req.body.comp });
    const response = await clients.catalog.post('/info_competition', params);
    res.json(response.data);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

/**
 * Lists players in the requested competition.
 * @name POST/comp_players
 * @function
 * @param {string} comp - Competition code in the request body.
 * @returns {Array} - Competition players or an error.
 */
router.post('/comp_players', async (req, res) => {
  try {
    const response = await clients.catalog.post('/comp_players', req.body.comp);
    res.json(response.data);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

function proxyGet(path) {
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
