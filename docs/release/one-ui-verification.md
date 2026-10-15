# Abeona One UI upgrade verification

Candidate **1.12.0-build.20**, version code **1220**, package **za.co.cttrains.app**, target API **36**. Application/test source: `beb6f9668f27936a2a8ba1010e6b3520010bc01a`; CI merge source: `a68eab044282f05f8f5e7ae6f178cbb87628d626`.

The accepted One UI design is implemented in the existing Kotlin/Compose app. The five-tab navigation, light/dark colours, native icons, type scale, spacing, rounded panels and persistent primary actions replace the former graphite/citron presentation. Providers, encrypted storage/schema, package identity, permissions and direction-service lifecycle retain their prior behavior. The repaired direct CT Trains connection remains intact.

## Passed checks

| Check | Result |
| --- | --- |
| Kotlin transit core | 157 passed |
| Public PDF fixtures | 2 passed |
| Node API/connectors/contracts | 29 passed |
| GitHub debug, Android 16 / 4 KB | 31 passed |
| Play debug, Android 15 / 4 KB | 31 passed |
| Play debug, Android 16 / 16 KB | 31 passed, compatibility disabled |
| GitHub release, Android 16 / 4 KB | 27 passed |
| Native total | 120 executions, zero failures/errors/skips |
| Distribution builds and lint | Both distributions, all four lint variants passed |
| Play bundle checks | Workflow bundletool, permission/installer exclusion, ELF and derived-APK alignment checks passed |
| Containers | API/worker/Redis build and health checks passed |
| Native visual review | Light/dark Home, results and active guidance; Saved, Settings, Journeys, station picker and map |
| Narrow/large-text component acceptance | 320 × 640 dp at 200 percent text; five tabs selectable, primary action above dock |

[Android workflow](https://github.com/Xhanti-mbasa/Abeona/actions/runs/38788704254), [backend workflow](https://github.com/Xhanti-mbasa/Abeona/actions/runs/38788704164), [visual evidence](../../design-qa.md), [machine-readable verification](one-ui-verification.json).

Each native configuration clears the query cache, refreshes the public rail catalogue, selects stations in Home, obtains actual scheduled routes and starts phone guidance with no configured backend. Existing saved-place editing/identity, preferred stations, alternate station choices, source recovery, recurrence/backup, theme/native Back, map controls, storage, notification and pause/resume/stop tests remain in the suite. UI checks now identify their intended controls and await asynchronous row rendering.

## Signed download

File: `Abeona-1.12.0-build.20-development-upgrade.apk`  
Size: **67,437,954 bytes**  
SHA-256: `ad4be6e79fc86069483e60d0a191a5ca68d8c3e745a6586a65f699cea320bfef`  
Certificate SHA-256: `212f7f9a7e6cb9ccff3e4c7c20512b993d83a661329e6cd6213612ef7e4dbbdc`

The non-debuggable release APK was signed with the recovered development key. Its certificate matches the earlier valid build 10 installer; the version code increases above earlier candidates. Every non-signature ZIP payload matches the CI APK. APK signature, ZIP integrity, package/version, target API and 16 KB ZIP alignment passed. After saving, the download was materialized separately and all checks repeated successfully with the same SHA-256. Signing credentials are not included in the repository or download.

This supports an Android update over an installation using that certificate. Actual in-place installation on the user's phone and physical Samsung/Now Bar behavior have not been tested. No uninstall or data reset is required by this change.

Fresh buses still require the existing owned backend deployment. Production capacity/source rights, publisher/privacy/upload signing, Play Console/internal-track and physical-device acceptance remain release gates. This verification does not establish Play Store readiness or vehicle/GPS field accuracy.
