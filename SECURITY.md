# Security policy

## Supported versions

Only the current default branch is supported for security fixes.

## Reporting a vulnerability

Please do not report exploitable vulnerabilities in public issues. Use GitHub's private vulnerability reporting for this repository. Include affected versions, steps to reproduce, and any relevant logs with credentials and personal data removed. We will acknowledge reports as soon as practical and coordinate a fix before public disclosure.

## Deployment notes

- Keep `.env` and provider credentials out of version control.
- The standalone `/mcp` endpoint stays disabled until a bearer token is configured. Use a long, randomly generated token and HTTPS when exposing it outside a trusted network.
- Compose binds MySQL to loopback and does not publish Redis.
- Change development-only database credentials before any non-local deployment.
