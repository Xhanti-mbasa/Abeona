# Abeona native design QA

result: passed

Scope: the user-selected graphite/citron direction implemented as a native Kotlin/Compose Android product. This result covers the representative Home, route-results and active-journey presentation and the checked native states. It does not certify Google Play readiness or field accuracy.

## Grounding and capture context

Approved image: docs/design/approved-direction.webp, recovered losslessly from the selected image. Rejected concepts were not used. Production layout/tokens: apps/android/app/src/main/kotlin/za/co/cttrains/app/ui/Theme.kt, AbeonaHome.kt, JourneyPlanner.kt and directions/DirectionsScreen.kt.

Native capture source: commit 34f001184ab316c5a3530d23320814e9ee8d7f3f, Android workflow 38285978588. API 36 x86_64 Pixel 2, 1080 × 1920, density approximately 393 dp wide, native system insets and gestures, no browser shell. Play Android 15 and Play Android 16/16 KB also completed the same critical suite. The later cached-alert, saved-place-retention/form and inverse-palette changes do not change these three primary layouts; their checks are in the release verification record.

The reference is an illustrative, taller phone layout. Comparisons normalize column width to 393 px, preserve aspect ratio and retain Android system areas. Height parity is not asserted: the emulator's shorter viewport requires scrolling. Data parity is also explicit: the captured journey is a cached station-to-station transfer with unknown fare and no access walk. The concept's 42 minutes, walking distances, saved-place names and street imagery were not copied into production.

## Combined source and implementation evidence

| Scope | Combined comparison | Regions checked |
| --- | --- | --- |
| Home | docs/design/qa/home-comparison.png | Heading/search, compact origin/date, three destinations, optional station selection and four-item dock |
| Results | docs/design/qa/results-comparison.png | Journey summary, readable duration/transfer/time/source, selected radio/border, secondary details, reachable persistent Start action |
| Active | docs/design/qa/active-comparison.png | Focused header/instruction, real saved-route diagram, phone-progress wording, next stage, manual boarding, persistent pause/stop |

Source and implementation were viewed together, then checked at focused heading/action/data regions. Raw native captures live in docs/release/screenshots with exact source and checksum metadata. The active capture depicts the started directions service, not a static concept or the results screen.

## Resolved findings

| Severity | Finding | Resolution/evidence |
| --- | --- | --- |
| P1 | A retired source preference could keep the observer returning without applying theme/profile changes | Migrate to the supported backend source, retain old query caches; native restart/theme/favorites regression passes |
| P1 | Earlier guidance-preview capture showed a previous compositor frame | Excluded that invalid image. Captured and inspected the running active journey with current step controls; do not use stale preview evidence |
| P2 | Home spacing/date rows crowded nearby choices on smaller displays | Compact date/time row and destination cards; scroll remains available for additional stations |
| P2 | Headings/navigation inherited unrelated default colors | Explicit main-text/background and selected-navigation citron roles; inverse snackbar roles also aligned |
| P2 | Results hierarchy spent too much space on verbose headings/timestamps | Concise duration and source stamps, clear transfer count/selected radio, one persistent primary action |
| P2 | Conditional status rows could shift settings controls during refresh | Inline status follows the operator controls; independent-switch native test passes |
| P2 | Map address form could lose reachable controls with an IME/large text | Scrollable form with a required address/description and meaningful map-unavailable fallback |

## Native behavior and accessibility

27 instrumented checks per platform passed at the capture source: saved address edit/restart, custom confirmation, optional/preferred station selection, recurrence/backup, theme/native Back, calendars/provider comparison, independent settings, map collapse/full-screen, encrypted storage and active notification/pause/resume/stop. Existing large-font bus captures/checks pass. Controls use native semantics and at least 48 dp touch targets; Start is 56 dp. Route selection exposes radio/selected semantics. No fixed font-scale cap is introduced.

Typography uses native sans serif at the specified 32/38, 28/34, 16/23, 14/20 and 13/18 roles. Core dark/light text and action pairs meet normal-text contrast; the hierarchy, panel shapes, margins and persistent actions are consistent. Maps retain geographic/provider layer colors; action/navigation colors follow Abeona's system.

Offline diagrams draw saved geography and expose controls; no vehicle position, walking duration or fare is fabricated. The actual captured boarding stage differs from the concept's walking stage because no mapped access walk exists in that test journey. Spoken guidance/sharing/source information remains reachable by scrolling; sharing stays off unless separately confirmed. Further physical-device/map-network/account acceptance remains in the release checklist.

No actionable unresolved P0/P1/P2 visual finding remains in this scoped review. Candidate store captures still require normal publisher review and recapture with the final production configuration; they do not prove timetable availability or Play eligibility.
