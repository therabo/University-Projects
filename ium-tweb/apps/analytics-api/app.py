import csv
import logging
import os
from pathlib import Path

from flask import Flask, jsonify


LOGGER = logging.getLogger(__name__)
DEFAULT_DATA_DIR = Path(__file__).resolve().parent / "data"


def load_valuation_index(data_dir: Path):
    """Read only the fields needed by the player page, once per process image."""
    by_player = {}
    with (data_dir / "player_valuations.csv").open(newline="", encoding="utf-8") as source:
        for row in csv.DictReader(source):
            try:
                player_id = int(row["player_id"])
                value = int(row["market_value_in_eur"])
                date = row["date"]
            except (ValueError, TypeError, KeyError):
                continue
            if not date or value < 0:
                continue
            by_player.setdefault(player_id, {})[date] = value

    index = {}
    for player_id, dated_values in by_player.items():
        history = tuple(
            {"date": date, "valueEur": value}
            for date, value in sorted(dated_values.items())
        )
        latest = history[-1]
        peak = max(history, key=lambda item: (item["valueEur"], item["date"]))
        index[player_id] = {
            "playerId": player_id,
            "history": history,
            "summary": {
                "firstDate": history[0]["date"],
                "latestDate": latest["date"],
                "latestValueEur": latest["valueEur"],
                "peakDate": peak["date"],
                "peakValueEur": peak["valueEur"],
            },
        }
    LOGGER.info("Indexed %s valuation records for %s players", sum(len(item["history"]) for item in index.values()), len(index))
    return index


def create_app(data_dir=None):
    app = Flask(__name__)
    directory = Path(data_dir or os.getenv("ANALYTICS_DATA_DIR", DEFAULT_DATA_DIR)).resolve()
    valuations = load_valuation_index(directory)

    @app.get("/health")
    def health():
        """Return the analytics service health status."""
        return jsonify(status="ok", service="analytics-api")

    @app.get("/players/<int:player_id>/valuations")
    def player_valuations(player_id):
        """Return the stored valuation history and summary for a player ID."""
        if player_id <= 0:
            return jsonify(error="Invalid player ID"), 400
        response = jsonify(valuations.get(player_id, {
            "playerId": player_id, "history": [], "summary": None
        }))
        response.headers["Cache-Control"] = "public, max-age=3600"
        return response

    return app


logging.basicConfig(level=os.getenv("LOG_LEVEL", "INFO"))
app = create_app()


if __name__ == "__main__":
    app.run(
        host=os.getenv("HOST", "127.0.0.1"),
        port=int(os.getenv("PORT", "5055")),
        debug=os.getenv("FLASK_DEBUG", "false").lower() == "true",
    )
