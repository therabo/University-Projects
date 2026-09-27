
const GENERAL_ROOM = Object.freeze({ id: 'general', label: 'Chat generale', type: 'general' });
const CACHE_TTL_MS = 5 * 60 * 1000;

function createRoomRegistry(fetchCompetitions = async () => {
  const { clients } = require('../services/upstream');
  const response = await clients.catalog.get('/list_competitions');
  return response.data;
}, now = Date.now) {
  let cached = null;
  let expiresAt = 0;
  let pending = null;

  async function getRooms() {
    if (cached && now() < expiresAt) return cached;
    if (!pending) {
      pending = Promise.resolve().then(fetchCompetitions).then((competitions) => {
        if (!Array.isArray(competitions)) throw new Error('Invalid competition catalog');
        const seen = new Set();
        const leagueRooms = competitions.flatMap((competition) => {
          const slug = competition?.Value;
          const label = competition?.Name;
          if (typeof slug !== 'string' || !/^[a-z0-9][a-z0-9-]{0,79}$/.test(slug)
              || typeof label !== 'string' || !label.trim() || seen.has(slug)) return [];
          seen.add(slug);
          return [{ id: `competition:${slug}`, label: label.trim(), type: 'competition' }];
        });
        cached = [GENERAL_ROOM, ...leagueRooms];
        expiresAt = now() + CACHE_TTL_MS;
        return cached;
      }).catch((error) => {
        if (cached) return cached;
        throw error;
      }).finally(() => { pending = null; });
    }
    return pending;
  }

  async function getRoom(roomId) {
    if (roomId === GENERAL_ROOM.id) return GENERAL_ROOM;
    if (typeof roomId !== 'string' || !roomId.startsWith('competition:')) return null;
    const rooms = await getRooms();
    return rooms.find((room) => room.id === roomId) || null;
  }

  return { getRooms, getRoom };
}

module.exports = { createRoomRegistry, roomRegistry: createRoomRegistry(), GENERAL_ROOM };
