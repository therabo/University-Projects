const test = require('node:test');
const assert = require('node:assert/strict');
const { createClubRosterController } = require('../controllers/club-roster-controller');

function query(rows) {
  return { select() { return this; }, async lean() { return rows; } };
}

test('returns only unique players with appearances in the selected season', async () => {
  let gameFilter;
  let appearanceFilter;
  const games = {
    async distinct() { return [2022, 2023, 2023]; },
    find(filter) {
      gameFilter = filter;
      return query([{ game_id: 10 }, { game_id: 11 }]);
    }
  };
  const appearances = {
    find(filter) {
      appearanceFilter = filter;
      return query([
        { player_id: 7, player_name: 'Mario Rossi' },
        { player_id: 7, player_name: 'Mario Rossi' },
        { player_id: 8, player_name: 'Andrea Bianchi' }
      ]);
    }
  };

  const result = await createClubRosterController(games, appearances)(506, 2022);
  assert.deepEqual(result, {
    season: 2022,
    availableSeasons: [2023, 2022],
    players: [
      { playerId: 8, name: 'Andrea Bianchi' },
      { playerId: 7, name: 'Mario Rossi' }
    ]
  });
  assert.equal(gameFilter.season, 2022);
  assert.deepEqual(appearanceFilter, { player_club_id: 506, game_id: { $in: [10, 11] } });
});

test('defaults to the latest year and rejects unavailable seasons', async () => {
  const games = {
    async distinct() { return [2021, 2023]; },
    find() { return query([]); }
  };
  const appearances = { find() { throw new Error('Unexpected appearance lookup'); } };
  const getRoster = createClubRosterController(games, appearances);
  assert.deepEqual(await getRoster(506, null), {
    season: 2023,
    availableSeasons: [2023, 2021],
    players: []
  });
  await assert.rejects(getRoster(506, 2022), { status: 400, message: 'Unavailable club season' });
});

test('returns an empty roster when the club has no recorded games', async () => {
  const getRoster = createClubRosterController(
    { async distinct() { return []; } },
    { find() { throw new Error('Unexpected appearance lookup'); } }
  );
  assert.deepEqual(await getRoster(999, null), {
    season: null,
    availableSeasons: [],
    players: []
  });
});
