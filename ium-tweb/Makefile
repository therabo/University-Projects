.PHONY: build up down logs seed check

build:
	docker compose build

up:
	docker compose up --build -d

down:
	docker compose down

logs:
	docker compose logs --follow

seed:
	./scripts/import-mongo-data.sh

check:
	shasum -a 256 -c data/seed/checksums.sha256
	npm --prefix apps/web run check
	npm --prefix apps/web test
	npm --prefix apps/match-api run check
	python3 -m unittest discover -s apps/analytics-api -p 'test_*.py'
	./apps/catalog-api/gradlew -p apps/catalog-api test
	docker compose config --quiet
