# Abeona verification record

Engineering source: `f1e8359deec49964e3f3dd2179872ed1fe4bfce8`. Candidate: **1.11.0-build.10**, version code **1110**, package **za.co.cttrains.app**, target API **36**. Final follow-up commits change documentation/captured evidence only; they do not change the verified application/backend code.

## Automated results

| Check | Result |
| --- | --- |
| Kotlin core | 150 passed; no failures/skips |
| Backend PDF parser with public documents | 2 passed |
| Node API, resilience, connectors and JSON-schema contracts | 29 passed |
| GitHub distribution, Android 16 / 4 KB | 27 passed; zero failures/skips |
| Play distribution, Android 15 / 4 KB | 27 passed; zero failures/skips |
| Play distribution, Android 16 / 16 KB | 27 passed; zero failures/skips; compatibility mode disabled and page size verified |
| Both variants' build/lint/artifact checks | Passed |
| Docker API/Redis/worker builds and real private-network health | Passed |

[Android workflow 38291485295](https://github.com/Xhanti-mbasa/Abeona/actions/runs/38291485295), [backend workflow 38291485351](https://github.com/Xhanti-mbasa/Abeona/actions/runs/38291485351). The native runs use Pixel 2 x86_64 images; physical Samsung/arm64 acceptance is still pending. Unit/device failures uncovered during development were fixed and rerun; earlier failing runs are not counted as passing evidence.

## Exact downloadable local candidates

| File | SHA-256 |
| --- | --- |
| Abeona-1.11.0-build.10-development-upgrade.apk | `56574f0f42e8c55b2d6f3d201b02ca70ccd2157d27e93d475d5f8172ddc7a40a` |
| Abeona-1.11.0-build.10-play-candidate.aab | `25a5d1886bdc3350687f1d0c613da85ca8fbffd47a015495bbb49f4bf049f41e` |

Local signed candidates were built from the engineering source with JDK 17/Gradle 8.11.1 and Android SDK 36. Local GitHub/Play release compilation and lint passed. Signing uses the recovered development certificate, not an assumed Play upload identity. CI artifacts have independent runner development signatures; use the local matching-certificate APK for compatible upgrade review.

Certificate SHA-256: `212f7f9a7e6cb9ccff3e4c7c20512b993d83a661329e6cd6213612ef7e4dbbdc`. It matches the supplied **1.9.0-build.23** APK (code 923). The previous APK also contains the same `cttrains.local.profile.v1` Keystore alias, `transit.db` filename and AES/GCM record format. No Room schema change or destructive migration is introduced. A real signed in-place installation/data comparison remains required; certificate compatibility alone does not prove that check.

The Play AAB passed official bundletool validation, package/target/non-debuggable checks, installer-code and installer/background-location permission exclusion. All six arm64/x86_64 native libraries use 16 KB ELF load alignment. The AAB requests 16 KB native ZIP alignment; its generated universal Play APK also passed zipalign/signature checks. Runtime testing covers the matching PlayDebug source/native dependencies; the release-signed/internal-track installation and supported Play update path remain submission gates.

## Source and design evidence

Actual API → JVM worker → provider reads succeeded for CT Trains, MyCiTi and Golden Arrow. The captured figures are in [the live probe](../production/live-source-probe-2026-10-12.json); actual full MyCiTi/Golden Arrow responses passed the common JSON schema. This is point-in-time availability, not a source licence or every-route guarantee.

[Native design QA](../../design-qa.md) passed its scoped Home/results/active review. Six actual native candidate screenshots, their device/source/checksums and editable brand artwork are under screenshots/ and branding/. Controlled fixture UI is distinguished from the live source probe; neither an illustrative design nor a test screenshot is presented as current vehicle data.

**Google Play readiness: not established.** The candidates have no assumed production backend URL. Publisher/signing/policy, source rights, production operations and physical/internal-track acceptance gates remain in [acceptance-and-blockers.md](../production/acceptance-and-blockers.md) and [checklist.md](checklist.md). No release was published or store submission made. AI attribution remains repository-only.
