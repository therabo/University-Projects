const express = require('express');
const { clients, sendUpstreamError } = require('../services/upstream');

const router = express.Router();

/**
 * Loads a club's PNG crest from the catalog.
 * @name GET/clubs/:id/crest
 * @function
 * @param {number} id - Club identifier in the URL path.
 * @returns {Buffer} - PNG crest or a service error.
 */
router.get('/clubs/:id/crest', async (req, res) => {
  if (!/^[0-9]+$/.test(req.params.id)) {
    return res.sendStatus(404);
  }
  try {
    const response = await clients.catalog.get(`/clubs/${req.params.id}/crest`, {
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
 * Lists clubs available in the catalog.
 * @name GET/all_teams
 * @function
 * @returns {Array} - Available clubs or a service error.
 */
router.get('/all_teams', async (req, res) => {
  try {
    const response = await clients.catalog.get('/all_teams');
    res.json(response.data);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

/**
 * Lists clubs in a competition for the requested season.
 * @name POST/list_teamsbycompetition
 * @function
 * @param {string} comp - Competition code in the request body.
 * @param {number} [season] - Season start year in the request body.
 * @returns {Array} - Clubs and crest URLs, or an error.
 */
router.post('/list_teamsbycompetition', async (req, res) => {
  const season = req.body.season;
  if (season !== undefined && (!Number.isInteger(season) || season < 1900 || season > 2100)) {
    return res.status(400).json({ error: 'Invalid season' });
  }
  try {
    const response = await clients.catalog.post('/list_teamsbycompetition', req.body.comp, {
      params: season === undefined ? undefined : { season }
    });
    res.json(response.data);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

/**
 * Returns details of the requested club from the catalog.
 * @name POST/list_info_squad
 * @function
 * @param {string} squad - Club name in the request body.
 * @returns {Array} - Club details or an error.
 */
router.post('/list_info_squad', proxySquadParam('/list_info_squad'));

/**
 * Combines seasonal appearances with player names and portraits from the catalog.
 * @name GET/clubs/:id/roster
 * @function
 * @param {number} id - Club identifier in the URL path.
 * @param {number} [season] - Requested season start year.
 * @returns {Object} - Roster, selected season, and available seasons.
 */
router.get('/clubs/:id/roster', async (req, res) => {
  if (!/^[1-9][0-9]*$/.test(req.params.id)
      || (req.query.season !== undefined && !/^(19|20)[0-9]{2}$/.test(req.query.season))) {
    return res.status(400).json({ error: 'Invalid club or season' });
  }

  try {
    const roster = (await clients.match.get(`/clubs/${req.params.id}/roster`, {
      params: req.query.season === undefined ? undefined : { season: req.query.season }
    })).data;
    const appearances = Array.isArray(roster.players) ? roster.players : [];
    const playerIds = appearances.map((player) => player.playerId);
    const details = playerIds.length === 0 ? []
      : (await clients.catalog.post('/players/by-ids', playerIds)).data;
    const detailsById = new Map(details.map((player) => [player.playerId, player]));
    const players = appearances.map((appearance) => {
      const detail = detailsById.get(appearance.playerId);
      return {
        playerId: appearance.playerId,
        name: detail?.name || appearance.name,
        image_url: detail?.imageUrl || null
      };
    }).sort((left, right) => left.name.localeCompare(right.name, 'it'));

    return res.json({
      season: roster.season,
      availableSeasons: roster.availableSeasons,
      players
    });
  } catch (error) {
    return sendUpstreamError(res, error);
  }
});

/**
 * Returns the club's seasonal statistics from the match service.
 * @name GET/clubs/:id/stats
 * @function
 * @param {number} id - Club identifier in the URL path.
 * @param {number} [season] - Requested season start year.
 * @param {string} [scope] - Either league or all.
 * @returns {Object} - Club statistics or an error.
 */
router.get('/clubs/:id/stats', async (req, res) => {
  if (!/^[1-9][0-9]*$/.test(req.params.id)
      || (req.query.season !== undefined && !/^(19|20)[0-9]{2}$/.test(req.query.season))
      || (req.query.scope !== undefined && !['league', 'all'].includes(req.query.scope))) {
    return res.status(400).json({ error: 'Invalid club, season or scope' });
  }
  try {
    const response = await clients.match.get(`/clubs/${req.params.id}/stats`, {
      params: { season: req.query.season, scope: req.query.scope }
    });
    return res.json(response.data);
  } catch (error) {
    return sendUpstreamError(res, error);
  }
});

/**
 * Returns statistics for a club searched by name.
 * @name POST/squad_stats
 * @function
 * @param {string} squad - Club name in the request body.
 * @returns {Object} - Club statistics or an error.
 */
router.post('/squad_stats', async (req, res) => {
  try {
    const response = await clients.match.post('/squad_stats', { squad: req.body.squad });
    res.json(response.data);
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

function proxySquadParam(path) {
  return async (req, res) => {
    try {
      const params = new URLSearchParams({ squadName: req.body.squad });
      const response = await clients.catalog.post(path, params);
      res.json(response.data);
    } catch (error) {
      sendUpstreamError(res, error);
    }
  };
}

module.exports = router;
