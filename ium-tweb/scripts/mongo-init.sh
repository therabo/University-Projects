#!/usr/bin/env sh
set -eu

database=${MONGO_DATABASE:-DynamicDatabase}
host=${MONGO_HOST:-mongo-db}

if [ "${FORCE_SEED:-false}" != "true" ]; then
  existing=$(mongosh --host "$host" --quiet "$database" --eval 'db.games.estimatedDocumentCount()')
  if [ "$existing" -gt 0 ]; then
    echo "MongoDB is already seeded ($existing games); skipping import."
    exit 0
  fi
fi

for collection in games club_games appearances game_events game_lineups; do
  echo "Seeding MongoDB collection: $collection"
  gzip -dc "/seed/$collection.csv.gz" | \
    mongoimport --quiet --type=csv --headerline \
      --drop --host "$host" --db "$database" --collection "$collection"
done

echo "MongoDB seed completed."
