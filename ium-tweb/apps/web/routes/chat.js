const express = require('express');
const { roomRegistry } = require('../realtime/chat-room-registry');

const router = express.Router();

/**
 * Lists the general chat and available competition channels.
 * @name GET/api/chat/rooms
 * @function
 * @returns {Array} - Available channels or a service error.
 */
router.get('/api/chat/rooms', async (req, res) => {
  try {
    res.json(await roomRegistry.getRooms());
  } catch (error) {
    console.error('Chat rooms unavailable:', error.message);
    res.status(502).json({ error: 'Chat rooms unavailable' });
  }
});

module.exports = router;
