const test = require('node:test');
const assert = require('node:assert/strict');
const { createRoomRegistry } = require('../realtime/chat-room-registry');

test('chat rooms mirror valid catalog competitions and include general chat', async () => {
  let calls = 0;
  const registry = createRoomRegistry(async () => {
    calls += 1;
    return [
      { Value: 'serie-a', Name: 'Serie A' },
      { Value: 'premier-league', Name: 'Premier League' },
      { Value: 'serie-a', Name: 'Duplicate' },
      { Value: '../invalid', Name: 'Invalid' }
    ];
  });

  const rooms = await registry.getRooms();
  assert.deepEqual(rooms.map((room) => room.id), [
    'general', 'competition:serie-a', 'competition:premier-league'
  ]);
  assert.equal((await registry.getRoom('competition:serie-a')).label, 'Serie A');
  assert.equal(await registry.getRoom('competition:unknown'), null);
  await registry.getRooms();
  assert.equal(calls, 1);
});

test('general chat remains available when the catalog is unavailable', async () => {
  const registry = createRoomRegistry(async () => { throw new Error('offline'); });
  assert.equal((await registry.getRoom('general')).id, 'general');
  await assert.rejects(registry.getRooms(), /offline/);
});
