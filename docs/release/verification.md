# Abeona repair verification

Candidate **1.11.1-build.14**, version code **1134**, package **za.co.cttrains.app**, target API **36**. Engineering/test source: `6a846b60bc31f349dccee192f54414dbe5180931`. Application changes are at `2b37be51cd24e13eaa8ba93fe174055f64bcb130`; the next commit corrects lifecycle test expectations without changing production code.

The **1.11.0-build.10 candidate is superseded**. It removed the original train connection while shipping an empty backend URL. Its cached-fixture journey test did not verify fresh train planning. Do not use the earlier 81 native executions as evidence that fresh searches worked in that build.

## Passed checks

| Check | Result |
| --- | --- |
| Shared Kotlin core | 157 passed; zero failures/skips, including 7 rail-source regression cases |
| Backend public PDF fixtures | 2 passed |
| Node API/connectors/contracts | 29 passed |
| GitHub debug, Android 16 / 4 KB | 30 passed |
| Play debug, Android 15 / 4 KB | 30 passed |
| Play debug, Android 16 / 16 KB | 30 passed; actual page size verified, compatibility disabled |
| GitHub release, Android 16 / 4 KB | 27 passed; no debug-only fixture activity added to release |
| All native runs | 117 executions, zero failures/errors/skips |
| Both distributions | Builds, four lint variants, signed artifact checks passed |
| Containers | API/worker/Redis contract/build/health checks passed |

[Android run 38313077461](https://github.com/Xhanti-mbasa/Abeona/actions/runs/38313077461), [backend run 38313077601](https://github.com/Xhanti-mbasa/Abeona/actions/runs/38313077601). Standalone Compose fixture layout tests use androidTestDebug; release tests launch the actual app. Earlier failing runs are not included in the passed counts.

## Real fresh train flow

Every native configuration starts the acceptance search without a backend or seeded query cache. The test refreshes the public station list, selects Salt River and then Unibell through Home, fetches fresh results, selects a journey and starts foreground guidance. The release run recorded four scheduled journeys for 13 October 2026 after 07:00; source `cttrains-web`, fetched epoch `1791842202938`. The complete acceptance case took 6.977 seconds. This verifies source retrieval and app startup of guidance, not actual vehicle tracking, GPS accuracy or physical travel.

Source-setting recovery retains theme/favorite stations. Encrypted cache tests retain original timestamps and reject a different leave-after query during an outage. Accepted rail queries are persisted; screen recreation retains their selected date, and a fresh ViewModel restores stations/time for today's planner. Saved-place identity/editing, recurrence, guidance/notifications, portable records and other existing checks also pass.

## Exact signed downloads

| File | SHA-256 |
| --- | --- |
| Abeona-1.11.1-build.14-development-upgrade.apk | `0196a5ccec02fd10e881ed182152ffc4954f12754821ffc0efec90d67c84f1c7` |
| Abeona-1.11.1-build.14-play-candidate.aab | `5cb04338d7cb218ac84e2cb812f789f454a098ff752940a6fb3971ec97e72237` |

Both artifacts were produced by the passing workflow and signed locally with the recovered development key. All non-signature ZIP payloads match the workflow artifacts byte-for-byte. APK signature, non-debuggable status, package, higher version and 16 KB ZIP alignment passed. Certificate SHA-256 is `212f7f9a7e6cb9ccff3e4c7c20512b993d83a661329e6cd6213612ef7e4dbbdc`, matching the previously supplied 1.11.0 and 1.9.0 upgrade APKs. No uninstall/data reset is part of this repair. Actual installation over the user's physical phone remains unverified.

The exact re-signed Play AAB passed bundletool validation, package/API/release/permission checks, GitHub installer exclusion and all six 64-bit ELF alignments. Its derived universal Play APK also passed signature, version 1134 and 16 KB ZIP alignment checks. Matching PlayDebug native dependencies passed strict 16 KB runtime checks. PlayRelease internal-track runtime, production upload signing, Console ownership and Play update eligibility remain release gates.

## Limits

Fresh rail works through public CT Trains pages without a backend. Fresh MyCiTi/Golden Arrow still require a deployed owned backend. Public source availability is point-in-time, not a licence, uptime promise, every-route guarantee or vehicle feed. Production capacity/access rights, publisher/privacy/contact/signing, physical/Samsung acceptance and Console testing remain blocked. **Google Play readiness is not established.**

Six current native release captures are in screenshots/, with source/checksum metadata. Routes and active guidance use actual freshly fetched scheduled data; saved places/settings use controlled test records. AI attribution remains repository-only.
