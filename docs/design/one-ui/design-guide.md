# Abeona · One UI-inspired redesign

This package is an editable design proposal and a local interactive preview for Abeona. It has not been applied to the Android APK.

Open **Abeona-One-UI-Preview.html** in Firefox or another modern browser. It is self-contained, requires no installation and makes no provider requests. Its example places, station choices, theme and recurring pattern are saved under a dedicated browser-storage key. Reset example data clears only this prototype's records.

## What is included

- Eleven base screen designs in both light and dark: Plan, route results, active journey, Map, Saved, Journeys, Updates, Settings, station selection, address confirmation and journey details.
- Additional cached, loading, empty, saved-address and alternative-station states: **34 individual SVG exports** in total.
- A six-screen overview showing the three representative journey screens in light and dark.
- A design-foundations sheet and machine-readable colour, typography, spacing, radius and control-size tokens.
- The interactive preview, its renderer and interaction model, source station-name fixture, icon licence, verification results and this handoff.

The SVG files contain vector shapes and text. They are editable in a vector editor and can be imported into a Figma design file. They are not native Figma component sets or a published Figma library. SVG import may outline or substitute text depending on the editor and fonts available.

## Design direction

The proposal uses One UI-inspired rounded groups, clear headings, neutral surfaces, blue primary actions and persistent journey controls. Light and dark retain the same layout and hierarchy.

The user-supplied reference is:
https://www.figma.com/community/file/1456035621603784201/one-ui-design-kit

The Community file's component layers could not be inspected. A Figma draft was created, but the Starter-plan MCP quota blocked the subsequent design-system search and canvas construction. Consequently, this package uses **Abeona proposal tokens and original screen compositions**, not extracted Samsung kit components or colours. It contains no proprietary Samsung font files. The vector preview uses Arial/Helvetica fallback; the Android implementation should use the device's native sans-serif font and font scaling.

The existing app package, source, integrations and personal data have not been altered. No AI-build label has been added to the product.

## Navigation and scope

Keep **Map, Plan, Saved, Journeys and Settings** in the main dock. Service updates remain available from the header and Settings. The preview opens on Plan to make the principal task immediately reviewable; it does not change the existing app's launch destination.

The preview removes redundant search surfaces within each screen. From and To are separate endpoints, not competing searches. Station/address search belongs inside the relevant picker. There is no extra global Back button. The preview supports Escape and browser history for navigation; Android should keep native Back and predictive-back behaviour.

Settings preserves entry points for the broader product: bus schedules/fares, profiles/backups, sharing/community and walking/provider preferences. Detailed account, WhatsApp, Health Connect, fare-budget and updater screens remain existing Android features; this package does not recreate or integrate those flows. Their treatment should use the same design system during implementation.

## Screen and behaviour handoff

| Screen | Core design behaviour |
| --- | --- |
| Plan | Explicit origin/destination, swap, provider choice, comparison, date/time, walking preferences, saved-place shortcuts and reachable Find action |
| Results | Consistent duration, departure/arrival, changes, access, fare availability and source order; selected route; details, reminders and sharing; pinned Start action |
| Active journey | Current instruction, scheduled timing, step count, schematic route, next action, optional speech, manual boarding/alighting, Pause and Stop |
| Map | Retain the geographic entry point, station search, location choice and selected station; diagram fallback remains labelled |
| Saved | Home/Work/School identity, confirmed address, optional station, custom-place list and From/To/edit/remove actions |
| Journeys | Selected weekdays, leave-after time, reminder preference, recurring editor, saved plan and calendar-review handoff |
| Updates | Source-unavailable and refreshing states; no notice is not an all-clear signal; keep reports separate from schedules |
| Settings | Theme and transport-tool entry points; providers and sharing remain independent choices |
| Station picker | Suggestion plus alternatives; searchable mapped catalogue; explicit confirmation; retained choice |
| Address confirmation | Review selected address/description and station before saving; editing retains the record identity |
| Details | Departure, interchange, arrival, source limitations, reminders, sharing and Start remain reachable |

## Design system

| Role | Light | Dark |
| --- | --- | --- |
| Background | #F3F3F6 | #080809 |
| Panel | #FFFFFF | #1C1C1F |
| Primary text | #151518 | #F6F6F8 |
| Supporting text | #63636C | #B0B0BA |
| Accent | #006BD6 | #91BFFF |
| Accent container | #E6F0FF | #162B47 |
| Text on primary action | #FFFFFF | #10233D |

Type roles are 38/46, 30/38, 22/28, 16/24, 14/20, 13/18 and 12/16 sp. The smaller role is for navigation/supporting labels; primary travel information uses larger roles. The renderer includes a few small nonessential badges and attribution labels that are not separate canonical text styles.

Use a four-unit spacing rhythm with 20–24 dp page margins. Main action height is 56 dp. Vector action targets are at least 48 × 48 design units. Panels use 26 dp corners, sheets 32 dp, fields 18 dp and pill actions 28 dp. On Android, actual layouts must reflow rather than scale a fixed-size image.

Primary contrast pairs were calculated from the proposal colours: light text/background 16.46:1, supporting text/panel 5.95:1, accent/panel and primary-button text 5.15:1; dark text/background 18.55:1, supporting text/panel 7.91:1, accent/panel 8.99:1 and primary-button text 8.35:1. These checks cover the named pairs, not every possible state or system-wallpaper palette.

## Data and privacy rules to preserve in Android

1. A nearby station is a suggestion. Preserve an explicit alternative and explain unsupported access rather than silently replacing it.
2. Keep place identity separate from station choice. Updating Home must not create another Home, change a custom place's identity or erase its photo/station.
3. Cached data must match its source and accepted query, retain original timestamps and stay distinct from a fresh fetch. A restored plan is also a separate state.
4. Distinguish scheduled times, published report dates, check times, provider availability and local phone progress. Do not infer a single fare, walk duration, platform or live train location.
5. Compare only complete verified provider options. Unverified Golden Arrow survey corridors stay candidates. Do not invent stop-to-timetable joins or automatic bus transfers.
6. Re-check each recurring travel date. Android reminders are optional and affected by system scheduling.
7. Share itinerary details only after review. Live sharing needs separate account/consent, an expiry of at most two hours and an immediate Stop action.
8. Leaving the active screen must not stop guidance. Pause must stop sampling; Stop must end the service and its ongoing notification.
9. Use native system insets, accessibility semantics and text scaling. Do not render these SVGs as production UI. Route selection should expose radio/selected semantics; sheets need proper focus handling and Back dismissal.
10. Keep ordinary notification fallback dependable. Android/Samsung promoted surfaces remain device-dependent and require field verification.

## Prototype boundaries

All itinerary times are illustrative. The main example is based on the previously captured Unibell–Salt River journey structure. The later alternative and dynamically shifted example times are layout fixtures, not newly fetched schedules. Changing the query does not contact a transport provider.

The map is an original labelled schematic, not geographic street imagery. Address selection uses local examples or a manually entered description; it does not geocode or verify an address. Station names come from the existing app's mapped catalogue fixture.

Preview reminders do not schedule Android notifications. Live-sharing controls simulate expiry/Stop without reading GPS or sending a location. Calendar and backup dialogs illustrate the review state without accessing another app or private files. Clipboard copying and optional browser speech happen only after the user activates those controls.

## Verification

**23 automated checks passed** for the interaction model and vector export contracts. They cover station persistence, place identity/editing/restart, flexible recurrence, manual guidance progression, leaving versus stopping, time-limited sharing, edited queries, theme persistence, provider-unavailable behaviour, escaping, modal background tab order, touch bounds, custom-place collisions, selected-route timing and empty/loading states.

JavaScript syntax and SVG XML structure were checked. The overview, foundations and supporting screens were rendered and visually inspected. Visual findings fixed include transparent hit regions appearing as black blocks in the SVG renderer, a map caption touching the primary action, a settings-group corner mismatch and hidden-state keyboard targets.

**Not verified:** browser end-to-end rendering/interactions, TalkBack/Switch Access, native Android implementation, real provider requests, real address/map selection, reminders under Doze, GPS/speech on the phone, private account-to-account sharing, Samsung live surfaces and Google Play readiness. The preview is not an APK or a release candidate.

## Continue implementation

Review the three main screens in the overview first. Use the interactive file to inspect endpoint/station selection, place editing, recurrence and active guidance. The remaining SVGs show the supporting states.

After the visual direction is accepted, translate the tokens and component structure into the existing Kotlin/Compose app. Keep data repositories, provider sources, persistence, package ID and verified train repairs intact. Implement in reviewable screen groups, then run the existing native journey, station, saved-place and lifecycle checks with fresh provider data. Reassess release readiness separately.

For future Figma work, the draft key and quota block are recorded in design-workflow-state.json. Resume that draft rather than creating another blank file. Native Figma variables, component properties, layouts and prototype wiring remain outstanding.

## Reproduce exports

The build uses Node.js and the included source files. Run node build.cjs to regenerate the self-contained HTML, SVG screens, overview and foundations. Run node checks.cjs for the model/export checks. Inkscape can render the SVG exports to PNG. The icon paths come from Lucide; its licence is included.
