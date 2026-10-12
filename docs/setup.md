# Abeona setup and operations

## Monorepo

| Directory | Responsibility |
| --- | --- |
| apps/android | Native Kotlin/Compose Android app, Play and GitHub distributions, device checks and release scripts |
| packages/transit-core | Pure Kotlin parsing, routing, station choices, fares, recurrence and guidance contracts; shared with JVM scraper workers |
| services/api | Existing Node 22/Express API, compatibility endpoints and normalized mobile endpoints |
| packages/provider-connectors | Provider inventory/capabilities, bounded CT Trains fetches, host gates and request coalescing |
| packages/contracts | Common normalized route/stop/schedule/fare/disruption schema and provider provenance |
| workers/scrapers | JVM MyCiTi/Golden Arrow extraction and published rail alerts; PDFBox remains backend only |
| fixtures/providers/html | Captured source pages shared by Kotlin, Node and Android test assets |
| infra | Docker images and private worker/Redis deployment configuration |
| docs | Design, sources, setup, inspection, privacy and release evidence |

## Local API

Use Node 22, JDK 17, Android Studio/SDK 36, and the checked-in Gradle 8.11.1 wrapper. Android builds use Kotlin 2.1.20 and AGP 8.9.2.

```bash
npm ci --ignore-scripts
npm test
npm start
```

The API defaults to port 3000. `/health` reports process/cache health and observed provider availability; unknown availability is not a claim that a source is working. `/mobile/v1/providers` describes supported capabilities. Never expose credentials in a URL. `.env.example` lists optional server configuration. Redis is optional and a bounded memory cache survives a Redis connection failure. A full process restart loses memory-only cached timetables.

Start the JVM worker in a second terminal:

```bash
cd apps/android
./gradlew :scrapers:run
```

It binds localhost:8081 by default. MyCiTi and Golden Arrow catalogues refresh in the background at startup and daily. First catalogue requests can be unavailable while warming. Timetable requests only accept URLs present in a published catalogue. Source format changes yield errors; they cannot turn into invented schedules. Persistent last known good worker snapshots retain their original check dates.

For a backend-only Gradle build without an Android SDK, use an installed Gradle 8.11.1:

```bash
gradle -p workers/scrapers installDist
```

## Android

Open `apps/android` in Android Studio. Configure a real, reachable HTTPS base URL in `apps/android/local.properties`:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
CTTRAINS_TRANSIT_BASE_URL=https://your-owned-backend.example
```

The example domain must be replaced. No production URL has been invented or provisioned. The environment variable `CTTRAINS_TRANSIT_BASE_URL` overrides this value. Client IDs for optional Google profile selection are public configuration, not provider secrets. Keep production signing credentials in CI secrets or private environment variables, never the repository.

```bash
cd apps/android
./gradlew :core:test :app:assembleGithubDebug :app:assemblePlayDebug
./gradlew :app:lintGithubRelease :app:lintPlayRelease :app:bundlePlayRelease
```

Both distributions retain `za.co.cttrains.app`, database `transit.db`, encrypted record keys, saved-place IDs and schema version 1. Rebranding does not reset a profile. An in-place upgrade also needs a matching signing certificate; an unrelated debug key cannot update an existing installation. Back up the profile through Settings before manually changing distributions or signing identities.

All operator extraction runs on the backend. An unconfigured build can display bundled geography and exact locally cached searches; it cannot refresh transport data. A stale match must use the same route, travel date and leave-after query. Map tiles, explicitly enabled Photon searches and consented walking requests still use their disclosed public services directly.

`play` contains Play flexible in-app updates and no GitHub installer code or install-packages permission. `github` contains the reviewed APK staging/update mechanism. Test Play update behavior through a Play testing track; sideloading cannot validate Play ownership/update eligibility.

## Containers and deployment

```bash
docker compose -f infra/compose.yml config --quiet
docker compose -f infra/compose.yml build
docker compose -f infra/compose.yml up -d
curl --fail http://127.0.0.1:3000/health
```

Expose only the API through a TLS reverse proxy on your owned domain. The sample API port binds localhost; worker and Redis ports are private to the compose network. Restrict ingress and set an upstream gateway rate limit appropriate to device traffic. The in-process API IP limiter is per replica and does not replace a distributed gateway. Provider requests have independent host gates, timeouts, size limits and bounded retries. Do not treat a public website as unlimited permission to scrape it.

Persistent volumes contain public provider snapshots and Redis cached queries. Server logs record event type, provider and failure category, not raw credentials or private Matrix tokens. Configure proxy access-log retention and sanitisation deliberately: route queries and IP addresses can identify travel habits. No API analytics service is installed. Source caches have bounded TTLs and memory budgets, but proxy/cloud log retention is an operator configuration that must be reflected in the privacy policy.

Before production, verify every source's terms/access permission, TLS, actual timetable freshness, weekday/holiday behavior, failure recovery and offline behavior from a phone. Confirm HTTPS connectivity with the exact release build. Health alone does not verify usable service data.

## Release and rollback

CI runs contracts, Kotlin fixtures, Android lint, native device tests, release builds and artifact checks. Container CI builds both images and checks API-to-worker connectivity. GitHub publication remains an explicit manual operation after successful build/device checks. It does not upload to Play.

For release signing, supply all four variables: `CTTRAINS_KEYSTORE_PATH`, `CTTRAINS_KEYSTORE_PASSWORD`, `CTTRAINS_KEY_ALIAS`, `CTTRAINS_KEY_PASSWORD`. CI restores the path from `CTTRAINS_KEYSTORE_BASE64`. Play upload signing and the final on-device Play App Signing certificate are different roles; verify the Console configuration before signing a bundle. `CTTRAINS_USE_DEVELOPMENT_SIGNING=true` creates an installable development candidate, never proof of production signing.

Deploy immutable image tags/digests from a tested source commit. Preserve Redis and scraper volumes during rollback; keep a previous known-good image pair and API contract version. Switch the reverse proxy back to that pair if error rates or source-age monitoring regress. For Android, halt a rollout and publish a higher version code with the fix or previous behavior. Do not uninstall/reinstall as a rollback mechanism for user data.

No production schema migration was introduced. Any future migration needs a versioned Room schema and a real upgrade fixture; never use destructive fallback for personal records.
