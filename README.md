[![CI](https://github.com/Daegybyte/URL-Shortener-Java-Springboot/actions/workflows/ci.yml/badge.svg)](https://github.com/Daegybyte/URL-Shortener-Java-Springboot/actions/workflows/ci.yml)

# URL Shortener

A URL shortening service built with Java and Spring Boot. It creates short links, redirects visitors with a server-side 302 so every click passes through the service, and uses Redis to keep redirects fast.

## Features

- Create a short link for any http or https URL, with an optional expiry time
- Redirect by short code (302), with 404 for unknown, inactive, or expired links
- Random, non-guessable short codes with automatic collision handling
- Redis cache for redirect lookups with LRU eviction
- Schema managed by versioned Flyway migrations

## Tech stack

Java 25, Spring Boot 4.1.1, PostgreSQL 16, Redis 7, Flyway, Maven, JUnit 5, AssertJ, Mockito, Docker Compose, k6

## Quick start

Requirements: JDK 25 and Docker.

From the `shortener` directory:

```bash
docker compose up -d        # start Postgres and Redis
./mvnw spring-boot:run      # start the app on port 8080
```

Check it is running:

```bash
curl localhost:8080/actuator/health
```

Stop the services with `docker compose stop`. Run the tests with `./mvnw test`.

## API

**Create a link**

```bash
curl -X POST localhost:8080/api/links \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com", "expiresAt": "2026-12-31T00:00:00Z"}'
```

`expiresAt` is optional. A valid request returns `201 Created`:

```json
{"shortCode": "8iNZgPv", "shortUrl": "http://localhost:8080/8iNZgPv"}
```

Invalid input (blank URL, longer than 2048 characters, not http or https, or an expiry in the past) returns `400`.

**Follow a link**

```bash
curl -i localhost:8080/8iNZgPv
```

Returns `302 Found` with a `Location` header, or `404` if the code is unknown, inactive, or expired.

The base URL in responses comes from the `app.base-url` property and defaults to `http://localhost:8080`.

## Design decisions

- **Random codes, UUID keys.** Encoding a sequential ID would make links guessable and let anyone enumerate them. Codes are 7 random characters from a 62-character alphabet, generated with `SecureRandom` (about 3.5 trillion combinations).
- **Collisions.** A unique constraint on `short_code` is the source of truth. If an insert collides, the service retries with a new code, up to 5 attempts. `create` is deliberately not wrapped in a transaction, because in Postgres a failed statement poisons its transaction and the retries would fail too.
- **Duplicate URLs are allowed.** Each link is its own record. See the design challenge below.
- **302, not 301.** Browsers cache 301 redirects permanently, which would bypass the service and break analytics, expiry, and deactivation.
- **Cache-aside with Redis.** Redirects check Redis first, then Postgres, and store successful lookups under `link:{code}`. The TTL is one hour or the link's expiry, whichever is sooner. Redis is configured with a 64 MB limit and `allkeys-lru` eviction.
- **Flyway owns the schema.** Hibernate only validates that entities match the tables. Applied migrations are never edited, only added to.

## Design challenge: duplicate links

My biggest problem was deciding whether to allow duplicate links. I decided to allow them because my earlier design decisions already pointed that way:

1. I had already chosen 302 redirects over 301. Analytics matter, and handling the redirect on the server lets me count every click.
2. I had already chosen Redis for LRU caching to keep lookups cheap.

The real problem was that I asked the wrong question. I kept asking which option was better, when I should have asked what I was already designing for.

## Benchmarks

k6, 20 virtual users for 20 seconds against one short code, with the app, Postgres, and Redis on a single laptop. Second run of each, after warm-up.

| | No cache | Redis cache |
| --- | --- | --- |
| p95 latency | 3.94 ms | 2.13 ms |
| Average latency | 2.29 ms | 1.37 ms |
| Requests per second | ~8,600 | ~13,700 |
| Failed requests | 0% | 0% |

These are local numbers with a single hot key and a tiny table, so real gains depend on data size and the network distance to the database. Reproduce with:

```bash
k6 run -e CODE=<shortCode> k6/redirect.js
```

## Testing

Unit tests cover short code generation and the service logic (collision retry, retry limit, and resolution of missing, inactive, and expired links), using Mockito in place of the database.

## Known limitations and next steps

- Redirects fail if Redis is unavailable, with no fallback to the database
- Deactivating a link will need to delete its cache entry
- Planned: custom aliases, integration tests against real Postgres and Redis, CI with GitHub Actions
- Possible later: rate limiting and asynchronous click analytics