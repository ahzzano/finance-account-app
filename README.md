# Finance App
A budgeting app that displays your savings in different currencies

## Install

Prerequisites:
- Java 27 (the Maven wrapper `backend/mvnw` downloads Maven for you)
- Node.js LTS and pnpm 10 (`corepack enable` picks up the pinned version)
- Docker (for Mailpit and for the backend's Testcontainers tests)
- A PostgreSQL database (the project uses Supabase)

Configure the backend in `backend/.env`:

```properties
DB_URL=jdbc:postgresql://<host>:5432/<database>
DB_USERNAME=<user>
DB_PASSWORD=<password>
# Optional, defaults to public
DB_SCHEMA=public
```

Configure the frontend in `frontend/.env`:

```properties
BACKEND_URL=http://localhost:8080
```

Install frontend dependencies:

```sh
cd frontend
pnpm install
```

## Develop

Start the local services (Mailpit: SMTP on `1025`, web UI at http://localhost:8025):

```sh
docker compose up -d
```

Start the backend on http://localhost:8080. Flyway applies the migrations on startup:

```sh
cd backend
./mvnw spring-boot:run
```

Start the frontend on http://localhost:3000:

```sh
cd frontend
pnpm dev
```

### Tests

```sh
# Backend unit and integration tests (needs Docker)
cd backend && ./mvnw verify

# Frontend lint and build
cd frontend && pnpm lint && pnpm build

# End-to-end tests (needs the backend running; starts the frontend itself)
cd frontend && pnpm exec playwright install && pnpm exec playwright test
```
