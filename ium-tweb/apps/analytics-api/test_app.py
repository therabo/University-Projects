import tempfile
import unittest
from pathlib import Path

from app import create_app, load_valuation_index


class ValuationApiTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        directory = Path(self.temporary.name)
        (directory / "player_valuations.csv").write_text(
            "player_id,date,market_value_in_eur\n"
            "10,2022-01-01,1000000\n"
            "11,2022-01-01,9000000\n"
            "10,2023-01-01,3000000\n"
            "10,2022-06-01,5000000\n"
            "10,2022-06-01,6000000\n"
            "10,2024-01-01,\n",
            encoding="utf-8",
        )
        self.directory = directory

    def tearDown(self):
        self.temporary.cleanup()

    def test_index_orders_deduplicates_and_isolates_player_ids(self):
        index = load_valuation_index(self.directory)
        self.assertEqual([point["valueEur"] for point in index[10]["history"]],
                         [1000000, 6000000, 3000000])
        self.assertEqual(index[10]["summary"]["peakDate"], "2022-06-01")
        self.assertEqual(index[10]["summary"]["latestValueEur"], 3000000)
        self.assertEqual(len(index[11]["history"]), 1)

    def test_endpoint_has_bounded_json_and_empty_state(self):
        client = create_app(self.directory).test_client()
        response = client.get("/players/10/valuations")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json["playerId"], 10)
        self.assertEqual(len(response.json["history"]), 3)
        self.assertEqual(client.get("/players/12/valuations").json["history"], [])
        self.assertEqual(client.get("/players/0/valuations").status_code, 400)


if __name__ == "__main__":
    unittest.main()
