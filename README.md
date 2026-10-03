# Neighbor Smart Living

Neighbor Smart Living is an AI-assisted local discovery and merchant operations demo built with Spring Boot and Vue 2. It combines local shop discovery with merchant-facing AI content tools and an optional, token-protected MCP interface.

[简体中文版](README.zh-CN.md)

## Features

- Browse local shops and categories, view shop details, and discover nearby shops.
- Explore posts, likes, follows, check-ins, coupons, and flash-sale orders.
- Create and review restaurant reservations while signed in.
- Generate review summaries, reply drafts, blog drafts, coupon copy, and short-video scripts with configurable AI Skills.
- Use the signed-in AI consultant, which can call the application's business tools. Actions such as reservation creation still run through the authenticated user flow.
- Optionally expose read-only shop and voucher lookup tools over MCP. The MCP endpoint is disabled until `MCP_API_TOKEN` is set.

## Architecture

```text
Browser (Vue 2 + Element UI) -> Nginx -> Spring Boot REST API
                                             |-> MySQL 8
                                             |-> Redis 7 / Redisson
                                             |-> OpenAI-compatible chat API (optional)
                                             `-> MCP HTTP endpoint (optional, bearer token)
```

## Quick start with Docker Compose

Requirements: Docker Desktop with Compose v2. An AI provider key is optional; without one, browsing features still work and AI calls will not return generated content.

```bash
cp .env.example .env
# Edit .env and set AI_API_KEY if you want to use chat and AI Skills.
docker compose up --build
```

Open <http://localhost:8080>. The API is proxied through the same origin. MySQL is available on `127.0.0.1:3307`; Redis is only exposed inside the Compose network.

The first startup initializes the schema and small, synthetic shop/category records. MySQL only runs files under `db/mysql` when its data volume is empty. To reset this demo database and its Redis data, run `docker compose down -v` and start it again.

## Run the backend without Docker

Use Java 17+, Maven 3.9+, MySQL 8+, and Redis 7+. Create a database and apply these files in order:

```text
db/mysql/001-schema.sql
db/mysql/002-demo-data.sql
```

Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, and `REDIS_PORT` as needed, then run:

```bash
cd AI-dianping-backend
mvn spring-boot:run
```

For AI features, also set `AI_BASE_URL`, `AI_API_KEY`, and `AI_MODEL_NAME`. `MCP_API_TOKEN` is optional; without it `/mcp` responds with HTTP 503. When enabled, use `Authorization: Bearer <token>`. The MCP tool set contains only read-only shop, category, and voucher lookups.

## Tests

Run backend unit tests with:

```bash
cd AI-dianping-backend
mvn test
```

Tests are designed to run without a local MySQL or Redis service. The GitHub Actions workflow runs the same Maven test command on Java 17.

## Configuration

| Variable | Purpose | Default |
| --- | --- | --- |
| `DB_URL` | JDBC connection string | Local Compose MySQL on port 3307 |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials | `hmdp` / development-only password |
| `REDIS_HOST` / `REDIS_PORT` | Redis connection | `localhost:6379` outside Compose |
| `AI_BASE_URL` | OpenAI-compatible API base URL | DashScope compatible endpoint |
| `AI_API_KEY` | AI provider key | Placeholder; replace to enable AI |
| `AI_MODEL_NAME` | Chat model | `qwen-flash` |
| `MCP_API_TOKEN` | Bearer token for standalone MCP endpoint | Empty means disabled |

Never commit `.env` or production credentials. Change the development database password before exposing a deployment beyond your machine.

## Project layout

- `AI-dianping-backend/`: Spring Boot API, business services, AI Skills, consultant tools, and MCP server.
- `AI-dianping-frontend/html/hmdp/`: static Vue frontend.
- `AI-dianping-frontend/conf/nginx.conf`: local/Compose Nginx configuration.
- `db/mysql/`: schema and synthetic demo data; no imported user accounts, phone numbers, or review records.

## Current limits

- SMS login delivery requires an SMS provider configuration; the repository does not include one.
- AI chat and generation require a valid compatible provider key.
- The project has no verified benchmark artifact for the performance percentages sometimes quoted in project descriptions. No such performance claim is made here.
- This is a learning/demo project. Review authentication, rate limiting, storage, and deployment settings before production use.

## Contributing and security

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup and pull request guidance, and [SECURITY.md](SECURITY.md) for vulnerability reports. See [UPSTREAM.md](UPSTREAM.md) for source attribution. This repository is licensed under the MIT License; see [LICENSE](LICENSE).
Frontend dependency attributions are listed in [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).
