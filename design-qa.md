# Abeona native One UI design QA

result: passed

Scope: the accepted One UI design applied to the existing native Kotlin/Compose Android app. This review supersedes the earlier graphite/citron design review. It covers light/dark Home, route results, running guidance, Saved, Settings, Journeys, station selection and map presentation.

## Grounding and evidence

Approved proposal: the Abeona One UI screen set and design guide, with native dimensions documented in docs/design/one-ui/design-guide.md and tokens.json. The existing routing, database schema, provider interfaces, package identity, permissions and journey lifecycle are retained.

Final app source: `beb6f9668f27936a2a8ba1010e6b3520010bc01a`; Android workflow [38788704254](https://github.com/Xhanti-mbasa/Abeona/actions/runs/38788704254). Core comparisons use actual GitHub release captures; the narrow/large-text harness uses the corresponding debug run. Raw capture checksums and reference checksums are in [evidence.json](docs/design/one-ui/qa/evidence.json).

Capture environment: API 36 x86_64, 1080 × 2343 pixels, density 440, approximately 393 × 852 dp. Android owns status and gesture areas. Side-by-side comparisons normalize reference and native captures to the same width and preserve aspect ratio. Each pair was visually inspected together, including heading, card, source-stamp, primary-action and navigation regions.

The core screen captures use a fresh empty-cache public CT Trains fetch with no configured backend. Actual dates, train numbers, waiting buffers, platform availability, alternate-route durations and source timestamps replace illustrative proposal data. The active screen shows the started phone guidance service. Its boarding and arrival confirmations remain manual; the station progress display does not imply live train tracking.

## Side-by-side comparisons

| Screen | Light | Dark |
| --- | --- | --- |
| Home | [Comparison](docs/design/one-ui/qa/light-plan.webp) | [Comparison](docs/design/one-ui/qa/dark-plan.webp) |
| Results | [Comparison](docs/design/one-ui/qa/light-results.webp) | [Comparison](docs/design/one-ui/qa/dark-results.webp) |
| Active journey | [Comparison](docs/design/one-ui/qa/light-active.webp) | [Comparison](docs/design/one-ui/qa/dark-active.webp) |
| Saved | [Comparison](docs/design/one-ui/qa/light-saved.webp) | Theme roles covered by core dark captures |
| Settings | [Comparison](docs/design/one-ui/qa/light-settings.webp) | Also inspected in the native dark capture |

[Large-text navigation harness](docs/design/one-ui/qa/large-text-navigation.webp). Journeys, preferred-station choices, recurring journeys and the actual map were also inspected in the native test captures.

## Resolved findings

| Severity | Finding | Final resolution |
| --- | --- | --- |
| P2 | Home source caption could be covered by the pinned action | Caption and 56 dp action share a persistent footer; final light/dark captures show the entire caption |
| P2 | Navigation words broke at large text sizes | At large font scales, the selected tab expands and keeps its label on one line; other tabs retain named semantics and icon targets |
| P2 | Active heading had an extra 20 dp gap and missing station context | Header grouped with heading, labelled route progress restored, actual operational details retained |
| P2 | Small light accent text on tint fell just below 4.5:1 | On-container text uses #0068D1, measured 4.68:1 against #E6F0FF; main blue stays #006BD6 |
| P2 | Empty Saved cards used extra height for a separate edit action | Whole-card edit target with compact icon tile; persistent Add a place action |

## Dimensions and accessibility

Shared layout dimensions are 20–24 dp page/sheet margins, 12 dp spacing, 26 dp card corners, 32 dp sheets, 48 dp minimum interactive targets and 56 dp primary actions. Text uses Android native sans serif in sp; the hero role is 38/46 sp. Layout heights are minimums and content can scroll independently of pinned actions and the five-tab dock.

A dedicated component harness exercises a 320 × 640 dp layout at 200 percent text scale, verifies all five tabs can be selected, and checks the action stays above the dock. It is a focused action/navigation check, not a claim that every screen has been physically tested at every font size. The final captured Settings label is intact.

Measured colour-role contrast: light main text 16.46:1, light supporting text 5.37:1, white primary-action text 5.15:1, dark main text 18.55:1, dark supporting text 9.31:1, dark primary-action text 8.35:1 and dark tinted-label text 7.55:1. Explicit device-colour settings remain available.

## Native adaptations

The app retains existing System theme/device-colour preferences and the complete account, provider, backup, sharing, notification and travel controls. This makes Settings longer than the illustrative proposal. Real maps and the existing labelled offline station diagram remain functional. Existing save, repeat, share, fare and route details actions remain reachable from the route's details sheet; the proposal's duplicate global action row is consolidated there.

Native lifecycle controls, actual timetable provenance, permission explanations and missing-platform/GPS notices are retained even when they increase card height. The original unaffiliated-community-app notice remains under Timetables & privacy. No illustrative fare, tracking position, route time or screen bitmap is shipped as functional UI.

## Verification and limits

157 core tests, 2 PDF fixture tests, 29 Node/API/contract checks and all four lint/build variants passed. Native execution counts are 31 GitHub Android 16 debug, 27 GitHub Android 16 release, 31 Play Android 15 debug and 31 Play Android 16 strict 16 KB debug: 120 total, zero failures/errors/skips. Each configuration verifies a real empty-cache train search through started guidance without a configured backend. Saved-place identity, recurrence and encrypted backup checks pass. See [signed artifact verification](docs/release/one-ui-verification.md) for exact download/signature identity. The saved APK was read back and its ZIP/signature/payload/alignment verified again. Physical Samsung/Now Bar acceptance and in-place installation on the user's phone have not been performed. Existing backend requirements for fresh bus data and the production release gates remain unchanged.

No actionable unresolved P0/P1/P2 visual finding remains in this scoped native review.
