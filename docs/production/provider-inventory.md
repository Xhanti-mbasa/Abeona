# Abeona integration inventory

Inspection date: 12 October 2026. This inventory is based on recovered source commit 10c260e. Fixture support is not a live availability guarantee. Current source access conditions and provider permissions must be confirmed before deployment.

## Transport sources

| Provider/source | Data and code | Current execution | Operational limits |
| --- | --- | --- | --- |
| PRASA/Metrorail via CTTrains community website | Catalogue, rail timetables, fares and alerts. `CtTrainsParser.kt`, `TrainRepository.kt`, `ServiceAlerts.kt`; Node `services/api/src/scrapers/cttrains.js` and normalized `services/api/src/routes/mobile.js` | Restored public Android rail connection; optional backend Node API with direct source fallback | Unofficial/community source. No vehicle GPS feed demonstrated. Parser must distinguish unfamiliar markup from explicit no-service results. Catalogue source defines supported times, dates and holidays. Terms, permission and uptime not verified in this pass |
| MyCiTi official website | Embedded route/stop/map data, route timetable HTML and fares. `BusProviders.kt`, `BetaTransit.kt` | Backend JVM worker | The worker reuses the shared Kotlin parsers; MyCiTi/Golden Arrow extraction remains outside the Android process. Service coverage depends on official published routes/days. No real-time vehicle feed demonstrated. Web access does not imply an integration licence |
| Golden Arrow official website | ASP.NET timetable catalogue, public PDFs and published fare products. `GoldenArrowProvider`, `GoldenTimetables.kt` | Backend JVM worker | Cookie/viewstate handling and multiple catalogue requests. PDF extraction bounded to 4 MB and 24 pages; encrypted PDFs rejected. Current route-number-to-stop membership not established |
| Western Cape government GIS survey | Golden Arrow stop coordinates and simplified route geometry. `GoldenSurvey.kt`, bundled survey fixtures/assets | Backend JVM refresh and bundled fallback | Survey capture date absent. One representative geometry per stop code; route codes do not establish current service, platform or direction. Official licence/access conditions need verification |

Source URLs preserved from code:

- Rail: https://cttrains.co.za/train-form.php, https://cttrains.co.za/train-schedule.php, https://cttrains.co.za/status2.php
- MyCiTi map: https://www.myciti.org.za/en/routes-stops/route-stop-station-map/
- MyCiTi timetable downloads: https://www.myciti.org.za/en/timetables/timetable-downloads/
- MyCiTi fares: https://www.myciti.org.za/en/myconnect-fares/pay-as-you-go/
- Golden Arrow catalogue: https://www.gabs.co.za/Timetable.aspx
- Golden Arrow fares: https://www.gabs.co.za/ProductDetails.aspx
- GIS: https://gis.westerncape.gov.za/server2/rest/services/SpatialDataWarehouse/Transportation/MapServer

No taxi, rideshare, intercity or other transport integration is assumed.

## Supporting services and data disclosure candidates

| Integration | Purpose and current safeguards | Disclosure or verification needed |
| --- | --- | --- |
| Photon demo API | Explicit place autocomplete, Cape Town bounding box, 8 results, local small cache and a request gate | Query text and IP leave the device; production service capacity and usage policy must be verified |
| OpenStreetMap walking routing | Explicit coordinate-based walking requests, request gate and cached walking legs | Origin/destination coordinates and IP leave the device; public demo is not a production SLA |
| MapLibre/OpenFreeMap | Online/offline maps | Online viewed area and IP; native binary/page-size and offline license/attribution checks |
| City of Cape Town imagery / bundled rail geometry | Map layers and rail traces | Provenance retained under Android docs; verify rights, attribution and current endpoints |
| Android location and TTS | Local guidance, optional spoken directions | Explain location before request; pause/stop bounds and notification fallback; TTS engine behaviour |
| Matrix | Optional rooms, public reports, private encrypted live updates and invitations | User-selected homeserver, account IDs, messages/reports and optional time-limited location. Confirm retention, consent, stop/revoke and deletion applicability |
| Google identity | Optional profile sign-in | Build-time public client ID and certificate configuration; final account/privacy disclosure |
| Health Connect | Optional step totals | Separate user permission; policy declaration and clear reason. No health upload claimed by existing code |
| GitHub updater | Download and verify an APK for external distribution | Must be excluded from Play build and replaced with Play-supported updates |
| Redis | Optional backend cache | Credentials backend-only; server retention and cache bounds; connection URL no longer logged |
| MCP/Gemini bridge | Existing backend tools | Optional backend-only API key; keep out of mobile package and deployments unless deliberately enabled |

## Provider boundary

Each provider declares capabilities (stops, schedules, fares, disruptions and live-vehicle availability). The common versioned contract in `packages/contracts` normalizes provider data and retains provider-specific fields. Fetch/report/check times remain distinct. Cached data retains its source kind; only mark data live when a supported upstream observation exists.

Keep independently testable parsers and original fixtures. Backend connector errors should identify provider, capability and retryability, avoid secrets and private route/location data in logs, and return useful cached data with its original timestamps when allowed.

Workers need bounded requests and concurrency, timeouts, capped retries with jitter, provider-specific rate budgets, health/readiness checks and observable failures. Prefer an official licensed feed if discovered; do not replace a working source based on an assumed feed.

## Monorepo execution update

Backend operator requests use server-side host gates, response caps, bounded retries and last known good data with original timestamps. Rail also retains a bounded direct public-page adapter in Android after the 1.11.0 regression; it remains usable without a configured service. MyCiTi and Golden Arrow catalogue refreshes run in the JVM worker; Android PDFBox was removed. Public PDF regression fixtures moved to fixtures/providers/pdf and execute with server PDFBox. No private provider feed or live vehicle GPS integration has been supplied. Node legacy endpoints remain compatibility adapters; shared captured fixtures guard the normalized rail representation.

## Live source probe, 12 October 2026

The real Node API, JVM worker and their HTTP network were exercised locally; the observation log is [live-source-probe-2026-10-12.json](live-source-probe-2026-10-12.json). No account, provider secret or user location was used.

| Source | Observed result | Remaining qualification |
| --- | --- | --- |
| CT Trains | 102 catalogue stations; 4 Unibell → Salt River journeys for 13 October at 07:00, including separate transfer legs | Point-in-time scheduled website data, not vehicle tracking or licensed feed access |
| MyCiTi | 47 routes, 530 stops; route 101 Monday timetable parsed 28 trips | Published route/day coverage, source permissions and production reliability still require review |
| Golden Arrow | 1,884 catalogue documents, 3,569 surveyed stop locations; first published Airport Industria → Bellville Monday PDF parsed 18 trips | Cold refresh needs time; survey membership/current operation remains unverified. This probe does not validate every document, fare or journey |

Golden Arrow initially returned a warming response and then completed successfully. The app/API retain usable cached data during that period. Health now distinguishes partial source failures; component timestamps are not reset when data is retained. Published/source ages, permission, licensing and optional service capacity are separate from HTTP availability.
