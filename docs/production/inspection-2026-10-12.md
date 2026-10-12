# Abeona: inspection and stabilisation

Date: 12 October 2026. Stage: inspect and stabilise, then select a visual direction.

**Abeona is not yet ready for Google Play submission.** This is an imported source baseline plus a small stabilisation pass, not the completed redesign or final monorepo structure.

## Source and preservation

The requested Abeona repository was empty on inspection. The existing Android app is on the open CTTrains Android PR: https://github.com/Xhanti-mbasa/cttrains-api/pull/1.

Recovered source: `feat/android-material3`, commit `10c260eabea53753cdf6911596c343ba8806f1c6`. All 208 files were recovered and checked against their Git blob SHA before editing, including the standard Gradle wrapper. The Android source version is 1.10.0, code 1000.

Keep `za.co.cttrains.app` as the application ID. Changing the display name to Abeona does not require changing that ID. Keep Room `transit.db`, schema version 1, encrypted local values, the Keystore codec and existing serialized field names. No destructive storage migration or data reset has been added. A signed upgrade over the user's installed APK remains a required device test.

The original source repository and its open PRs remain unchanged. Development signing material is excluded from Git. A pattern scan found only known test passwords, not provider credentials. This scan is not a substitute for secret-history review when importing additional repositories.

## Verified baseline and immediate changes

| Item | Evidence or change | Status |
| --- | --- | --- |
| Current Android build | Original CI run 37984686808 failed in both build and device jobs | Confirmed original failure |
| Journey-sharing compilation | Nullable Double durations used integer Elvis defaults; Kotlin could not resolve multiplication on lines 11 and 14 | Fixed to Double defaults and multipliers; regression test added |
| Abeona identity | Launcher label, app header and settings footer use Abeona; application ID retained | Implemented in this stage |
| AI credit | Repository README says exactly “Predominantly built with AI” for other developers; no AI-build label in the Android app | Implemented in this stage |
| Target API | Original targetSdk 35, compileSdk 36 | Target set to 36; Android 16 behaviour still needs verification |
| Redis credentials | Successful connection logged the complete REDIS_URL | Removed URL from that log |
| Publishing | Original CI automatically published GitHub releases on main/tags | Release publication now requires an explicit manual dispatch flag |
| Backend critical contract suite | `npm run test:mobile` | 11 passing tests locally |
| Pure Kotlin core | Same source/tests/fixtures, Kotlin 2.1.20, JDK 17, isolated JVM harness | 145 passing tests, 17 suites, zero skipped/failures |
| Current Android CI | Run 38251841919 compiled both app variants; lint found notification permission handling errors; 24 of 25 device tests passed, with ambiguous Salt River selection in the saved-place test | Permission-revocation handling and a stable station-result selector added; rerun required |
| Saved Home duplicates | Current source already contains `SavedAddresses.upsert` and identity/photo/station preservation tests | Present in source; full device rerun still required |
| Android runtime and signing | No configured Android SDK, emulator or production signing key in this environment | Unverified; CI and signed device tests required |

The 145 core tests are current local results, not historical counts, and do not replace a full Android app build. The Android CI rerun must be inspected on the draft PR. No verified release Android App Bundle is delivered at this stage.

## Technology stack

Android: Kotlin 2.1.20, Jetpack Compose, Material 3, AGP 8.9.2, Gradle 8.11.1, minSdk 26, Room 2.7.1, WorkManager, OkHttp, MapLibre, Android Keystore, Kotlin serialization and optional Health Connect, Google identity and Matrix integration.

Backend: Node.js/CommonJS, Express 5, Axios, Cheerio, optional Redis through ioredis, node-cron, plus MCP/Gemini tooling. Keep these stacks unless a specific blocker requires a change.

## Critical flows: existing code versus verification

| Flow | Existing implementation | Remaining verification or work |
| --- | --- | --- |
| Journey planning | Rail schedules, walking legs, MyCiTi timetables and Golden Arrow published PDFs | Backend provider consolidation; current live-source checks; genuine combinations across providers |
| Nearby stations | Ranked suggestions, station selection and saved preference | Explicit distant alternatives can still be filtered by walking radius; preserve choice and explain unsupported access legs |
| Saved places | Search, map pins, confirmation, editing, encrypted local persistence; Home/Work/School deduplication | Restart, process death, signed upgrade, custom-place editing and legacy Campus-to-School behaviour |
| Recurring journeys | Weekday selection, scheduler, editing and reminders | Missed runs, reboot, Doze, denied notifications, timezone/date boundaries and unsupported-source days |
| Sharing | Reviewed itinerary files and optional encrypted Matrix live updates | Two-hour expiry, stop/revoke, dropped connection, notification denial and recipients' stale-state handling |
| Guidance | Walking instructions, manual board/alight controls, local GPS progress and optional speech | No GPS or approximate GPS, offline route, process restart, screen off and changed timetable |
| Ongoing notification | Android 16 progress notification with older Android notification fallback | Promoted-surface eligibility and actual Samsung Now Bar behaviour, including fallback |
| Data freshness | Rail source IDs, fetch dates, cached-search flags; separate bus map/catalogue/fare timestamps | Unified scheduled/live/cached/source-unavailable contract; source publication time must never be invented |

Golden Arrow survey route codes do not prove timetable membership at a stop. The current code explicitly declines automatic direct connections based on those codes. It must remain honest about that limitation until a valid crosswalk is verified.

## Confirmed production blockers and risks

1. The redesigned visual direction has not been selected or implemented.
2. Source import is an initial review branch. App, API and mobile scrapers still have their existing layout.
3. Play/GitHub distribution is not yet split into independent source sets/flavours. The imported app still has `REQUEST_INSTALL_PACKAGES` and a GitHub self-updater. Do not submit that APK to Play.
4. MyCiTi HTML, Golden Arrow HTML/PDF/GIS and direct rail scraping still run on Android. Move provider extraction and operational credentials to backend workers while retaining cached mobile read models.
5. A common capability-aware provider interface, shared schemas and provider-isolated worker health are not yet implemented across all providers.
6. Backend cache storage is unbounded; expired entries are generally removed on access. Provider retries, concurrency, per-provider rate budgets and structured failure metrics need a review.
7. The legacy service-update parser assigns retrieval time to `published_at`. Replace with separate fetch time and nullable upstream publication time.
8. Startup/decode errors, data upgrade, navigation, denied permissions, slow networks and battery use require current device evidence. Existing source alone cannot prove reliability.
9. No signed AAB, bundle validation, 16 KB native-library checks, production signing comparison or Play pre-launch report has been completed.
10. No hosted privacy policy, final Data Safety form, account-deletion applicability assessment, foreground-service declarations or verified store screenshots are ready.

## Visual evidence limits

The latest source could not be launched locally during initial inspection. Historical screenshot artifacts were inspected directly: run 37282955982 includes a “System UI isn't responding” dialog over several captures, so those captures are rejected as visual acceptance evidence. This is a System UI dialog, not proof of an Abeona startup crash.

Unobstructed planner captures from run 37013477933 provide historical design grounding only. They do not verify current navigation, accessibility or runtime performance. A fresh screenshot-based flow audit is still required after implementation.

## Reviewable next stages

1. Choose one of three visual directions, each covering Home, route results and active guidance.
2. Apply its light/dark tokens, typography and reusable Compose components consistently; retain native back behaviour and system insets.
3. Move the recovered modules to the agreed monorepo boundaries; migrate scrapers backend-first and verify each connector using retained fixtures.
4. Complete and verify all journey, saved-place, station-choice, recurring and sharing flows.
5. Produce signed distribution artifacts, accurate disclosures and screenshots from the running release candidate; complete the submission checklist.

Implemented work, image concepts and future requirements must remain labelled separately.
