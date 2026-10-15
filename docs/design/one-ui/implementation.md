# Native One UI implementation

The existing Kotlin/Compose Android app implements the accepted Abeona proposal in this branch. The source proposal is preserved in design-guide.md and tokens.json; its prototype-only limitations do not describe the native implementation.

## Presentation boundaries

Changes are confined to Compose screens, typography, colours, icons, layout dimensions and UI acceptance checks. RailDataSource, repositories, database schema, package identity, provider interfaces, permissions, sharing consent and journey lifecycle are unchanged. The CT Trains direct-connection repair remains intact.

The navigation uses Map, Plan, Saved, Journeys and Settings. Service reports stay in the header and Settings. Supporting account, bus, backup, community, WhatsApp, fare, reminder and notification controls remain reachable.

## Layout rules

Use dp for layout and sp with native system sans serif for type. Shared sizes live in AbeonaDesign.kt: 20 dp page margins, 24 dp sheet insets, 12 dp rhythm, 48 dp minimum touch targets and 56 dp primary actions. Panels use 26 dp corners, sheets 32 dp and primary actions 28 dp. Reference colours are in Theme.kt; an existing explicit device-colour preference still takes precedence.

Main content scrolls independently of pinned primary actions and navigation. Heights are minimums, not fixed text containers. System status/navigation insets belong to Android. The app does not draw a fake status bar or home indicator. Native Back is retained; there is no global Back button. Live maps remain real maps and retain the existing labelled diagram fallback.

Route summaries use actual journey durations, timestamps, changes, walking availability and provenance. Fixture dates, fares, example routes and schematic locations from the design preview are not shipped as timetable data. Station selection preserves the existing immediate-confirm behaviour, optional auto selection and preferred-station persistence.

Original Lucide paths from the proposal are supplied as native vector resources; see LUCIDE-LICENSE.txt. No UI screenshots or SVG screen renders are used as production UI.

## Verification gates

Before delivery: compile both distributions and release; run existing core, provider and native acceptance suites; verify an empty-cache live train search through route selection and guidance; verify saved-place identity, station alternatives, recurrence, sharing and notification lifecycle; inspect actual light/dark captures against the proposal; exercise a 320 dp viewport at 200 percent text scaling; verify the upgrade certificate and increasing version code. Record exact outcomes after CI and visual review complete.
