# Abeona

A native Android journey planner for Cape Town, formerly CT Trains. This repository consolidates the recovered app, its Node API, provider connectors, JVM scraper workers and shared contracts/fixtures.

The redesign uses graphite and pale citron with coherent light/dark themes, four main tabs, optional station suggestions, saved places, recurring journeys and local active directions. Transport data is explicitly scheduled or cached; no integrated source provides verified vehicle tracking.

Start with [setup and operations](docs/setup.md), [integration inventory](docs/production/provider-inventory.md), [design system](docs/design/design-system.md), [acceptance and blockers](docs/production/acceptance-and-blockers.md), [native design QA](design-qa.md), [verification record](docs/release/verification.md) and the [release checklist](docs/release/checklist.md).

| Component | Path |
| --- | --- |
| Android / Compose | apps/android |
| Shared Kotlin transit core | packages/transit-core |
| Express API | services/api |
| Provider adapters | packages/provider-connectors |
| Scraper workers | workers/scrapers |
| Data contracts | packages/contracts |
| Captured provider fixtures | fixtures/providers |
| Deployment | infra |

```bash
npm ci --ignore-scripts
npm test
npm start
```

Open `apps/android` in Android Studio. A reachable HTTPS backend is required for new transport searches. Use the `play` distribution for Play submission and the separate `github` distribution for APK updates. The existing app ID and encrypted data schema remain unchanged.

This is a production refinement in review. A development-signed candidate, passing fixtures or a design concept alone do not establish Google Play readiness. Production backend configuration, provider access, release signing, device evidence and Console checks must pass the release gates.

## Source and development provenance

**Predominantly built with AI.** This credit is for developers in the repository only; it is not shown in the app. Source recovered from `Xhanti-mbasa/cttrains-api`, branch `feat/android-material3`, commit `10c260eabea53753cdf6911596c343ba8806f1c6`. Original code was imported with its Git blob hashes verified. Subsequent changes are reviewable commits in the Abeona repository.
