# Permission and foreground-service explanations

This is the code-based declaration worksheet for the Play variant. Recheck the merged manifest of the exact uploaded artifact. Console forms, demo videos and feature eligibility remain publisher release gates.

| Permission / service | User action and purpose | Behaviour when unavailable |
| --- | --- | --- |
| Approximate / precise location | A requested nearby-station check, map location, explicitly started phone guidance, walking observation, or separately confirmed sharing session | Station/place search and stored itineraries still work. Guidance displays the itinerary; approximate fixes may be too broad for progress. Denial/revocation is handled without claiming a fix |
| Notifications | Optional journey reminders and visible ongoing guidance | Saved journeys remain available. The app explains disabled notifications and offers Android settings |
| Promoted ongoing notifications | Active navigation on eligible Android 16 firmware | Normal ongoing navigation notification remains the fallback. Device settings and Samsung firmware determine placement |
| Foreground service: location | User-started directions (maximum four hours), optional walking observation, and explicit live-sharing session (maximum two hours) | Start must occur while the app is visible with location permission. Pause stops guidance GPS requests; Stop ends the session. No background-location permission is requested |
| Foreground service: data sync | Visible sharing/network work and WorkManager tasks where a long transfer requires it | Work respects Android's lifecycle/foreground restrictions. Play app updates use Play's own mechanism |
| Health Connect: read steps | Optional step totals for walking observations the user starts/completes | Separate Health Connect approval; feature can stay off. No health writing or background health permission; no health-data upload implemented |
| Internet / network state | Scheduled transport refresh through the configured API, optional maps/address/walking services, explicitly configured accounts | Exact saved timetables, saved places, bundled rail geography and offline maps remain usable with honest freshness/error states |

The Play artifact has no installer, background-location, microphone, contact, Bluetooth scan, calendar-read or health-write permission. The GitHub distribution has its separately reviewed installer permission and must never replace the Play AAB.

User-facing explanations are displayed before online address/walking requests, sharing setup and permission requests. Notification/service declaration videos should demonstrate a real start, visible notification, manual boarding/arrival, pause, stop and permission-denial flow in the final Play installation. Do not claim Samsung Now Bar placement is guaranteed.
