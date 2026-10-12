# Abeona privacy policy — review draft

Prepared 12 October 2026 for the source in this repository. This document must be matched to the deployed backend and approved publisher information before publication.

**Publisher:** Xhanti-mbasa, pending the verified legal/publisher identity in Google Play Console.
**Privacy contact and public policy URL:** not supplied; required release blockers. Do not submit this draft as a final public policy.

Abeona helps you plan journeys using published Cape Town transport information. You can use the planner without an Abeona account. We do not include advertising or an analytics SDK. Timetables are scheduled information, not live vehicle tracking.

## Information on your phone

Saved places and station preferences, regular routes, recurring journeys, reminders, personal fare corrections and saved itinerary records stay on your phone. Personal records and optional sign-in/session credentials are encrypted with Android Keystore in the app's private Room database. Public station coordinates, scheduling metadata, downloaded map imagery and offline map bounds are not encrypted. Android automatic cloud backups are disabled for app data.

Your last twelve timetable searches can be retained locally for up to fourteen days. Saved places and routes remain until you edit or remove them. Old scheduled records are pruned after ninety days. Offline maps are removed using the map download manager. Optional encrypted profile backups are written only to the document location you select; deleting local data does not delete those independent files.

## Network requests

New transit searches send the chosen stations, travel date and departure time to the configured Abeona backend. The backend fetches public CT Trains community rail information, MyCiTi published bus information, Golden Arrow published timetables/fares and Western Cape surveyed map data. The operators receive requests from the backend. The API and its hosting/reverse proxy receive your IP address and request metadata. The final operator must publish its verified hosting providers, access-log policy and retention periods before release.

Online maps request the viewed geographic area and tiles from OpenFreeMap or the selected City of Cape Town imagery source. Those services receive the request and IP address. Offline or station-only map mode stops online tile requests. Downloading an offline area requires network requests for that area.

Online address suggestions are optional. When enabled, typed searches go to Photon/komoot using OpenStreetMap data. Optional mapped walking estimates send the chosen endpoints and candidate stop coordinates to the FOSSGIS routing service. Their availability, terms and privacy practices are governed by those services. Saved local places and stations remain searchable without online suggestions.

## Location, guidance and sharing

One-time location sampling occurs when you request it and grant Android location permission. Active directions use your phone's location while the explicitly started session runs, for a maximum of four hours, with a visible ongoing notification where allowed. Pause stops GPS sampling; Stop ends the session. Guidance progress does not prove a vehicle's location or boarding. The app does not save or upload a GPS history for local directions.

Live location sharing is a separate opt-in action using a Matrix homeserver and a private circle link. You review the journey/session and duration before starting, for a maximum of two hours. Updates are encrypted using the circle's shared key; recipients with that key can read them. Matrix hosting receives account, room, network and timing metadata. You can stop sharing from the active journey or sharing settings. Friend positions become stale after five minutes without a fresh update. Stopping cannot retract information already received or forwarded by another person.

Itinerary sharing opens Android's share sheet only after you review the journey. Shared text/files disclose the journey details you selected; this action does not start live tracking. Community reports/chat are optional and go to the selected room; public reports can be readable by that room's members and server. Avoid including private addresses or identifying details in public reports.

## Optional services and permissions

Google profile selection is optional and distinct from Matrix. Abeona uses the selected local profile information and does not create an Abeona server account. Matrix connects to an existing account on a homeserver you choose. Tokens are encrypted locally and excluded from profile backups. Disconnecting revokes the app's Matrix session when the server is reachable; if revocation fails, remove the session in the homeserver's trusted client. Abeona does not register a new Matrix account.

Health Connect step totals are read only after a separate permission and remain on this phone. Abeona does not request health writing or background health access. Spoken directions use Android's selected speech engine; an engine may have its own network behavior and privacy settings. Abeona does not request microphone permission. Calendar handoff opens your calendar app for review and does not read your calendar. Optional WhatsApp reader integration uses a separately configured gateway/account and read-only credentials; those integration practices require verification before enabling it for a production release.

Location, notification and Health Connect permissions can be denied or revoked in Android Settings. The Play distribution uses Google Play's update mechanism and has no APK installer permission. GitHub distribution is a separate build with a user-reviewed updater.

## Your choices and deletion

Edit/remove places and recurring journeys in the app. Clear cached timetables, forget sampled location, disconnect optional accounts, stop sharing and delete local personal data through Settings. Manage offline maps separately. These controls do not delete externally shared messages, recipient copies, calendar events, independent backup files or another provider's account. Use that provider's supported deletion controls for those records.

The production publisher must provide a discoverable contact/deletion-request route for any identifiable server records and describe exceptions/retention. Local deletion is not a substitute for a server deletion process. Nothing in this draft claims independent security certification or guaranteed deletion from federated Matrix servers.

## Changes

The final policy must carry its effective date, verified publisher/contact details, public URL, deployed data processors and retention practices. Reassess it when providers, accounts, diagnostics, advertising, sharing or data retention change. Update Google Play disclosures to match the distributed package's actual behavior.
