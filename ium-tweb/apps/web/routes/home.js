const express = require('express');
const { sendUpstreamError } = require('../services/upstream');
const { getHomeFeed } = require('../services/home-feed');

const router = express.Router();

/**
 * Loads the match feed displayed on the homepage.
 * @name GET/loadHP
 * @function
 * @returns {Object} - Upcoming matches and recent results, or an upstream error.
 */
router.get('/loadHP', async (req, res) => {
  try {
    res.json(await getHomeFeed());
  } catch (error) {
    sendUpstreamError(res, error);
  }
});

module.exports = router;
