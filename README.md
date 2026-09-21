# Ethereum Transparency Observatory

A Spring Boot 3 application that observes Ethereum Mainnet blocks through JSON-RPC, persists blocks and transactions, derives address activity, and exposes a retro dashboard with REST and STOMP/WebSocket endpoints.

## Local development

Requirements: Java 21 and Maven 3.9+.

```bash
cp .env.example .env
source "$HOME/.sdkman/bin/sdkman-init.sh"
mvn test
mvn spring-boot:run
```

The application starts at http://localhost:8080 and defaults to the keyless PublicNode Ethereum Mainnet RPC. Public RPCs are free but rate-limited. To use another endpoint, set `ETH_RPC_URL` explicitly.

## Configuration

Important environment variables:

- `ETH_RPC_URL`: Optional Ethereum Mainnet HTTP JSON-RPC endpoint. Defaults to PublicNode.
- `ETH_RPC_WS_URL`: Optional WebSocket endpoint; the current monitor uses HTTP polling.
- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`: PostgreSQL connection settings for production.
- `HISTORY_BLOCK_COUNT`: Number of recent blocks loaded at startup.
- `ETH_POLLING_INTERVAL_MS`: Delay between latest-block checks. The default is 15 seconds for public-RPC friendliness.

## Docker Compose

Set an RPC endpoint in the shell, then start the PostgreSQL-backed application:

```bash
unset ETH_RPC_URL
docker compose up --build
```

Open http://localhost:8080. Flyway creates the schema on first startup.

## Deployment

This is a long-running Spring Boot application with scheduled blockchain polling, WebSockets, and PostgreSQL persistence. Deploy the complete observatory with the included `Dockerfile` on a Java-capable host such as Render, Railway, Fly.io, or a VPS. Configure `DATABASE_URL`, database credentials, and optionally `ETH_RPC_URL` as platform secrets; never commit `.env` or provider keys.

Vercel cannot run the Spring Boot JAR as a persistent Java service. A Vercel deployment can host a separate static frontend, but it will not provide the live monitor, WebSocket broker, or database-backed API. Use the complete Docker deployment for the real observatory.

## API

- `GET /api/status`
- `GET /api/health`
- `GET /api/stats`
- `GET /api/transactions?size=8`
- `GET /api/repeated`
- `GET /api/graph`
- `GET /api/search?q=...`
- `GET /api/addresses/{address}`
- STOMP endpoint: `/ws`, topic: `/topic/status`
