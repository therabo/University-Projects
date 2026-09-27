const express = require('express');
const path = require('path');

const router = express.Router();

/**
 * Serves the initial login page.
 * @name GET/
 * @function
 * @returns {HTML} - Login page.
 */
router.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/login.html'));
});

/**
 * Returns the web service status.
 * @name GET/health
 * @function
 * @returns {Object} - Service status.
 */
router.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'web' });
});

module.exports = router;
