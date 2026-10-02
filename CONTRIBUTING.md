# Contributing

Thanks for considering a contribution.

## Development setup

1. Install Java 17+, Maven 3.9+, Docker Desktop with Compose v2.
2. Copy `.env.example` to `.env`; set an AI key only if you need AI features.
3. Start the services with `docker compose up --build`.
4. Run backend tests from `AI-dianping-backend` with `mvn test`.

## Pull requests

- Keep changes focused and explain the user-visible behavior they address.
- Add or update tests for behavior changes.
- Do not include credentials, personal data, production dumps, or generated build output.
- Preserve upstream attribution and do not imply a project-wide license until redistribution rights are confirmed.
- Update both `README.md` and `README.zh-CN.md` when setup or user-facing behavior changes.
- Ensure `mvn test` passes before opening a pull request.
