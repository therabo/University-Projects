const test = require('node:test');
const assert = require('node:assert/strict');
const attachChatServer = require('../realtime/chat-server');
const { createRoomRegistry } = require('../realtime/chat-room-registry');

function createHarness() {
  const emitted = [];
  const sockets = [];
  const io = {
    on(event, handler) { if (event === 'connection') this.connect = handler; },
    to(roomId) { return { emit(event, payload) { emitted.push({ roomId, event, payload }); } }; }
  };
  const registry = createRoomRegistry(async () => [{ Value: 'serie-a', Name: 'Serie A' }]);
  attachChatServer(io, registry);

  function connect(id) {
    const handlers = new Map();
    const socket = {
      id,
      data: {},
      rooms: new Set(),
      on(event, handler) { handlers.set(event, handler); },
      join(roomId) { this.rooms.add(roomId); },
      leave(roomId) { this.rooms.delete(roomId); },
      async send(event, payload) {
        return new Promise((resolve) => {
          const args = payload === undefined ? [resolve] : [payload, resolve];
          handlers.get(event)(...args);
        });
      },
      disconnect() { handlers.get('disconnect')(); }
    };
    sockets.push(socket);
    io.connect(socket);
    return socket;
  }
  return { connect, emitted, sockets };
}

test('rejects unknown rooms and sends only to the joined room', async () => {
  const { connect, emitted } = createHarness();
  const first = connect('first');
  const second = connect('second');
  assert.equal((await first.send('chat:join', { roomId: 'arbitrary', username: 'Alice' })).ok, false);
  assert.equal((await first.send('chat:join', { roomId: 'general', username: ' ' })).ok, false);
  assert.equal((await first.send('chat:join', { roomId: 'general', username: 'Alice' })).ok, true);
  assert.equal((await second.send('chat:join', { roomId: 'competition:serie-a', username: 'Bob' })).ok, true);
  assert.equal((await first.send('chat:send', { text: 'Ciao', roomId: 'competition:serie-a', username: 'Bob' })).ok, true);
  const message = emitted.findLast((item) => item.event === 'chat:message');
  assert.equal(message.roomId, 'general');
  assert.equal(message.payload.username, 'Alice');
  assert.equal(message.payload.text, 'Ciao');
  assert.equal((await first.send('chat:send', { text: ' ' })).ok, false);
});

test('room switching and disconnect remove stale presence', async () => {
  const { connect, emitted } = createHarness();
  const first = connect('first');
  const second = connect('second');
  await first.send('chat:join', { roomId: 'general', username: 'Alex' });
  const duplicate = await second.send('chat:join', { roomId: 'general', username: 'Alex' });
  assert.equal(duplicate.username, 'Alex');
  await first.send('chat:join', { roomId: 'competition:serie-a', username: 'Alex' });
  assert.equal(first.rooms.has('general'), false);
  assert.equal(first.rooms.has('competition:serie-a'), true);
  second.disconnect();
  await new Promise((resolve) => setImmediate(resolve));
  const presence = emitted.filter((item) => item.event === 'chat:presence' && item.roomId === 'general').at(-1);
  assert.deepEqual(presence.payload.users, []);
});

test('a superseded asynchronous join cannot keep an old room membership', async () => {
  const { connect } = createHarness();
  const socket = connect('quick-switch');
  const originalJoin = socket.join;
  let releaseFirstJoin;
  socket.join = function (roomId) {
    originalJoin.call(this, roomId);
    if (roomId === 'general') return new Promise((resolve) => { releaseFirstJoin = resolve; });
  };

  const firstJoin = socket.send('chat:join', { roomId: 'general', username: 'Alex' });
  await new Promise((resolve) => setImmediate(resolve));
  const secondJoin = await socket.send('chat:join', { roomId: 'competition:serie-a', username: 'Alex' });
  releaseFirstJoin();
  const superseded = await firstJoin;

  assert.equal(secondJoin.ok, true);
  assert.equal(superseded.ok, false);
  assert.deepEqual([...socket.rooms], ['competition:serie-a']);
  assert.equal(socket.data.roomId, 'competition:serie-a');
});
