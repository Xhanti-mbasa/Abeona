# Abeona design system

Approved direction: graphite and pale citron, selected after the first three concepts were rejected. The native implementation uses Kotlin, Compose Material 3 and MapLibre. The image contains illustrative journey data; production routes never use those example values.

## Foundations

| Role | Dark | Light |
| --- | --- | --- |
| Background | #171C20 | #F4F6F2 |
| Panel | #232B30 | #E9EEE6 |
| Primary action | #D8ED9A | #425B19 |
| Main text | #F4F6F2 | #171C20 |
| Secondary text | #AFBBB5 | #515C55 |
| Divider | #3D484A | #CDD5C9 |

Native sans serif follows Android font scaling. Headline 32/38 sp, secondary headline 28/34, title 18/24, body 16/23, secondary body 14/20, supporting timestamp 13/18. No fixed font-scale cap. Spacing uses a 4 dp rhythm, with 20 dp screen margins, 12–20 dp section gaps and 16 dp panel padding. Controls have at least 48 dp touch targets; primary journey actions use 56 dp. Shape tokens are 8, 12, 16, 20 and 24 dp. Material outlined icons have spoken labels for independent controls.

## Navigation and states

Home, Journeys, Saved and Updates form the dock. Settings opens from the top-right icon. Results belong to Home and native Back returns to planning. Active directions use a focused screen with a persistent action and native back behavior; leaving that screen does not silently end GPS guidance. Stop explicitly ends it.

Home has one destination search, a compact origin, departure time and date, saved-place shortcuts and optional station suggestions. Detailed provider, station and walking preferences live in the trip editor. Results show a concise scheduled itinerary and selected route border, with optional details, fares, sharing and recurrence actions. Data timestamps stay visible and unknown walking durations or fares are never inferred.

Real maps preserve geographic attribution. A unavailable street map uses existing station/itinerary geometry and a retry control. Guidance reflects phone position and manual boarding confirmation, never vehicle tracking. Live sharing is off by default and has a direct Stop sharing control when enabled.

Light and dark share component sizes, hierarchy and semantic states. Existing opt-in Android wallpaper colours remain a user preference. Loading announces the provider being checked; empty and failure states offer another time, station or source. Cached results keep their original source check times.

## Verification

Actual emulator captures, accessibility checks, source comparisons and unresolved visual findings belong in the root design-qa.md. Store screenshots must come from the built app; the approved image is a design concept and cannot be used as evidence of functionality.
