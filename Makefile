.PHONY: up down logs restart clean ps

up:
	docker compose up --build -d

down:
	docker compose down

logs:
	docker compose logs -f

restart:
	docker compose down && docker compose up --build -d

ps:
	docker compose ps

clean:
	docker compose down -v
