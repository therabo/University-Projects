const assert = require('node:assert/strict');
const test = require('node:test');
const { createPlayerStatsController } = require('../controllers/player-stats-controller');

function collection(rows) {
  return {
    find(query) {
      const selected = query.player_id ? rows.filter((row) => row.player_id === query.player_id)
        : rows.filter((row) => query.game_id.$in.includes(row.game_id));
      return { select: () => ({ lean: async () => selected }) };
    }
  };
}

const appearances = [
  { player_id: 10, game_id: 1, date: '2022-08-01', minutes_played: 90, goals: 1, assists: 1, yellow_cards: 1, red_cards: 0 },
  { player_id: 10, game_id: 2, date: '2022-09-01', minutes_played: 45, goals: 0, assists: 1, yellow_cards: 0, red_cards: 0 },
  { player_id: 10, game_id: 3, date: '2023-08-01', minutes_played: 90, goals: 2, assists: 0, yellow_cards: 0, red_cards: 1 },
  { player_id: 10, game_id: 99, date: '2023-10-01', minutes_played: 90, goals: 8, assists: 0, yellow_cards: 0, red_cards: 0 },
  { player_id: 11, game_id: 1, date: '2022-08-01', minutes_played: 90, goals: 9, assists: 0, yellow_cards: 0, red_cards: 0 }
];
const games = [
  { game_id: 1, season: 2022, competition_type: 'domestic_league' },
  { game_id: 2, season: 2022, competition_type: 'domestic_cup' },
  { game_id: 3, season: 2023, competition_type: 'domestic_league' }
];
const getStats = createPlayerStatsController(collection(appearances), collection(games));

test('selects the latest recorded season, ignores other players and unmatched games', async () => {
  const data = await getStats(10);
  assert.deepEqual(data.availableSeasons, [2023, 2022]);
  assert.equal(data.summary.goals, 2);
  assert.equal(data.summary.appearances, 1);
  assert.equal(data.summary.goalsPer90, 2);
  assert.equal(data.coverage.unmatchedAppearances, 1);
});

test('filters league matches and derives rates from played minutes', async () => {
  const league = await getStats(10, 2022, 'league');
  assert.equal(league.summary.appearances, 1);
  assert.equal(league.summary.assists, 1);
  assert.equal(league.summary.contributionsPer90, 2);
  assert.deepEqual(league.coverage, {
    firstAppearanceDate: '2022-08-01', lastAppearanceDate: '2022-08-01', unmatchedAppearances: 1
  });
  const all = await getStats(10, 2022, 'all');
  assert.equal(all.summary.appearances, 2);
  assert.equal(all.summary.minutes, 135);
  assert.equal(all.summary.assistsPer90, 1.33);
});

test('returns explicit empty data and rejects unavailable filters', async () => {
  const empty = await getStats(15);
  assert.equal(empty.season, null);
  assert.equal(empty.summary.appearances, 0);
  await assert.rejects(() => getStats(10, 2021), { status: 400 });
  await assert.rejects(() => getStats(10, null, 'invalid'), { status: 400 });
});
