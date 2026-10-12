# Abeona monorepo and release plan

Status: proposed; implementation follows visual selection. The imported review branch preserves the existing Kotlin/Compose and Node stacks.

## Proposed boundaries

| Destination | Existing source | Responsibility |
| --- | --- | --- |
| apps/android | android/app and Android Gradle build | UI, permissions, local storage, mobile cached data, guidance and notification surfaces |
| packages/transit-core | android/core | Pure Kotlin models, route/presentation logic, validators and existing unit tests |
| services/api | Express entrypoint and route modules | Validated client contracts, caching, readiness, auth where needed and stable API versions |
| packages/provider-connectors | Node scraper/parser modules and moved Android extraction | Source-specific fetching/normalization; no UI or mobile secrets |
| workers/scrapers | Existing cron plus migrated refresh jobs | Refresh scheduling, bounded upstream traffic, failures and retries |
| packages/contracts | Versioned JSON schema/OpenAPI and generated client DTOs | Routes, legs, stops, schedules, fares, disruptions, provenance and timestamps |
| fixtures/providers | Existing HTML/JSON/text/PDF fixtures | Contract and parser regression evidence, with source date and redacted content |
| infra | Docker/Compose, environment examples and deployment config | Reproducible staging/production, secrets references and rollback |
| docs | Existing useful Android/API docs plus new production records | Setup, architecture, integration limitations, device evidence and release decisions |

Move one module at a time and retain a runnable baseline. Update all Gradle, fixture, Docker and CI path references together. Do not reset the Room schema or Android application ID as part of a folder move.

## Play release requirements checked against official sources

As checked on 12 October 2026:

- New mobile app submissions and updates must target Android 16/API 36. The stabilisation branch sets targetSdk 36; runtime compatibility still needs testing. Official policy: https://support.google.com/googleplay/android-developer/answer/11926878
- A Play-distributed app must use Google Play's update mechanism and must not replace itself with an external APK. Keep the GitHub installer/permission outside the Play source set. Official policy: https://support.google.com/googleplay/android-developer/answer/16559646 ; supported update flows: https://developer.android.com/guide/playcore/in-app-updates
- Verify all native libraries and APK packaging for 16 KB page-size compatibility, and test a compatible device/emulator. The currently retrieved Android page describes a February 1, 2027 enforcement date; that date should be rechecked in the actual Play Console for the chosen release. Do not rely on older 2025 guidance or assume pure Kotlin means the MapLibre SDK has no native code. Official guide: https://developer.android.com/guide/practices/page-sizes
- Foreground service types must match user-visible use cases, be user-initiated/perceptible, stoppable and bounded. Complete the relevant Play Console declarations and demo evidence. Official policy: https://support.google.com/googleplay/android-developer/answer/16559646
- Data Safety must describe actual app and SDK behaviour. Reconcile the final build, hosted providers, optional sign-in, live sharing, place/walking searches, Health Connect and server retention. Official form guidance: https://support.google.com/googleplay/android-developer/answer/10787469
- Android promoted live notifications represent active ongoing tasks. Samsung placement remains device-dependent and requires real-device verification; retain a normal ongoing notification. Official guidance: https://developer.android.com/develop/ui/compose/notifications/live-update

These are planning checks, not a compliance certification. Current Play Console account requirements, testing eligibility, content rating, privacy/deletion URL applicability and final disclosures remain outstanding.

## Submission gates

- [ ] Selected design fully applied to light/dark screens and all required states.
- [ ] All core flows tested end to end on the current source.
- [ ] Saved places verified across restart, process death and signed in-place upgrade.
- [ ] Nearby suggestions permit alternatives and saved stations; no compulsory nearest choice.
- [ ] Recurring reminders checked through reboot, Doze, permission denial and missed schedule.
- [ ] Shared itinerary separate from explicit time-limited GPS sharing; stop/expiry verified.
- [ ] Slow/offline/unavailable provider behaviour verified without false live information.
- [ ] Play and GitHub flavours/source sets built separately; installer absent from Play merged manifest and package.
- [ ] Production-signed AAB built, signature/bundle/native-library checks completed; signing identity and versionCode reviewed.
- [ ] Release candidate tested on Android 8+ and Android 16, varied text scale, screen sizes, native back and Samsung device.
- [ ] CI tests, lint, device results and screenshots reviewed; no dialog-obscured or stale screenshot accepted.
- [ ] Provider access conditions, source provenance and production service endpoints documented.
- [ ] Hosted privacy policy, accurate Data Safety/permission/FGS declarations and account-deletion assessment complete.
- [ ] Play testing/pre-launch report and required account/testing requirements complete.
- [ ] Store copy, icon, feature graphic and screenshots describe only working features.
- [ ] Deployment/rollback documented, backend release tagged and mobile contract compatibility maintained.

No unchecked gate is silently treated as passed.

## Implementation checkpoint

The Android app is now under apps/android, pure Kotlin under packages/transit-core, Node API under services/api, JVM bus/PDF/alert extraction under workers/scrapers, shared provenance under packages/contracts, and public captured fixtures under fixtures/providers. The original Android package/database/schema and provider-specific notes/validity ranges are retained. Play and GitHub source sets are separate. Deployment, privacy worksheet and release gates are in docs/setup.md and docs/release.

Multimodal comparison includes a bounded rail + MyCiTi connection search when both providers and mapped walking are explicitly enabled. It joins only complete, catchable stages through at most two mapped rail interchanges near official MyCiTi stops. Golden Arrow surveyed route codes are excluded from automatic cross-provider joins. Mixed-route fares remain per stage and mixed-route recurrence is unavailable. Real provider journeys still require deployment/live verification; the temporal join tests are not proof of field service availability.
