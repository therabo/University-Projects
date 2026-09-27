const { roomRegistry } = require('./chat-room-registry');

const MAX_MESSAGE_LENGTH = 1000;
const MAX_NAME_LENGTH = 32;

function normalizeName(value) {
  if (typeof value !== 'string') return '';
  return value.replace(/[\u0000-\u001f\u007f<>]/g, '').replace(/\s+/g, ' ').trim().slice(0, MAX_NAME_LENGTH);
}

function attachChatServer(io, registry = roomRegistry) {
  const members = new Map();

  function getMembers(roomId) {
    return members.get(roomId) || new Map();
  }

  function broadcastPresence(roomId) {
    io.to(roomId).emit('chat:presence', {
      roomId,
      users: [...getMembers(roomId).values()]
    });
  }

  io.on('connection', (socket) => {
    socket.data.roomId = null;
    socket.data.username = null;
    let joinVersion = 0;

    async function leaveCurrent() {
      const roomId = socket.data.roomId;
      if (!roomId) return;
      const username = socket.data.username;
      socket.data.roomId = null;
      socket.data.username = null;
      getMembers(roomId).delete(socket.id);
      if (getMembers(roomId).size === 0) members.delete(roomId);
      await socket.leave(roomId);
      broadcastPresence(roomId);
      io.to(roomId).emit('chat:system', { roomId, text: `${username} ha lasciato la chat.` });
    }

    /**
     * Joins an available chat and returns its current participants.
     * @name chat:join
     * @param {Object} payload - Room ID and username supplied by the client.
     * @param {Function} ack - Acknowledgment with status, room, and participants.
     */
    socket.on('chat:join', async (payload, ack) => {
      const respond = typeof ack === 'function' ? ack : () => {};
      const version = ++joinVersion;
      try {
        const room = await registry.getRoom(payload?.roomId);
        if (version !== joinVersion) return respond({ ok: false, error: 'Selezione sostituita.' });
        if (!room) return respond({ ok: false, error: 'Canale non disponibile.' });
        const username = normalizeName(payload?.username);
        if (!username) return respond({ ok: false, error: 'Accedi prima di entrare in un canale.' });

        if (socket.data.roomId === room.id) {
          return respond({ ok: true, room, username: socket.data.username, users: [...getMembers(room.id).values()] });
        }
        await leaveCurrent();
        if (version !== joinVersion) return respond({ ok: false, error: 'Selezione sostituita.' });

        await socket.join(room.id);
        if (version !== joinVersion) {
          await socket.leave(room.id);
          return respond({ ok: false, error: 'Selezione sostituita.' });
        }
        socket.data.roomId = room.id;
        socket.data.username = username;
        if (!members.has(room.id)) members.set(room.id, new Map());
        members.get(room.id).set(socket.id, username);
        respond({ ok: true, room, username, users: [...getMembers(room.id).values()] });
        broadcastPresence(room.id);
        io.to(room.id).emit('chat:system', { roomId: room.id, text: `${username} si è unito alla chat.` });
      } catch (error) {
        console.error('Chat join failed:', error.message);
        respond({ ok: false, error: 'Impossibile entrare nel canale. Riprova.' });
      }
    });

    /**
     * Leaves the current chat and updates participant presence.
     * @name chat:leave
     * @param {Function} ack - Leave acknowledgment.
     */
    socket.on('chat:leave', async (ack) => {
      joinVersion += 1;
      await leaveCurrent();
      if (typeof ack === 'function') ack({ ok: true });
    });

    /**
     * Publishes a chat message after validating its content.
     * @name chat:send
     * @param {Object} payload - Message text, up to 1000 characters.
     * @param {Function} ack - Send acknowledgment or validation error.
     */
    socket.on('chat:send', (payload, ack) => {
      const respond = typeof ack === 'function' ? ack : () => {};
      const roomId = socket.data.roomId;
      const text = typeof payload?.text === 'string' ? payload.text.trim() : '';
      if (!roomId) return respond({ ok: false, error: 'Seleziona prima un canale.' });
      if (!text || text.length > MAX_MESSAGE_LENGTH) {
        return respond({ ok: false, error: 'Il messaggio deve contenere da 1 a 1000 caratteri.' });
      }
      io.to(roomId).emit('chat:message', {
        roomId,
        text,
        username: socket.data.username,
        senderId: socket.id,
        createdAt: new Date().toISOString()
      });
      respond({ ok: true });
    });

    socket.on('disconnect', () => {
      joinVersion += 1;
      void leaveCurrent();
    });
  });
}

module.exports = attachChatServer;
