#!/usr/bin/env sh
set -eu

project_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)

docker compose -f "$project_root/compose.yaml" run --rm -e FORCE_SEED=true mongo-seed
