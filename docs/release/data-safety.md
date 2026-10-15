# Data Safety worksheet — requires operator verification

Checked against Google's official guidance on 12 October 2026. This is a code-based worksheet, not a submitted Console form. Complete it for every version distributed under this package and the final backend/SDK configurations.

| Data | Observed behavior | Proposed disclosure / remaining decision |
| --- | --- | --- |
| Precise/approximate location | Local guidance remains on device. Optional foot routing, map viewport and explicit live sharing can leave the device | Declare collection for coordinate-bearing services; optional functionality. Verify whether each flow meets ephemeral or end-to-end encryption exclusions; do not assume them |
| Address / in-app search history | Saved places local; optional typed Photon queries and transit route/time queries leave the phone | App functionality. Search-history/address categories depend on request content and actual provider/log retention |
| Name, email, user IDs | Optional Google local profile and existing Matrix account | Matrix account identity/metadata leave device. Review Google SDK/provider disclosures and final sign-in configuration |
| Other in-app messages / user content | Optional community reports/chat and itinerary sharing | Review readable public room messages and provider retention. User-reviewed sharing exclusions must be assessed per flow |
| Photos / files | Personal pin photos local; backup/file sharing explicitly selected | Do not claim default photo upload. Assess chosen hosted/backup destination and sharing exceptions |
| Fitness information | Optional Health Connect step totals processed locally | No off-device collection in inspected code. Permission/policy declaration still required if included |
| Device/other IDs / diagnostics | Local profile UUID; no analytics SDK installed | Local UUID alone is not an upload. Verify API/proxy logs, SDK network behavior and crash handling before answering |
| Calendar | Intent handoff; no calendar read permission | Verify final artifact and user-initiated export handling |
| App updates | Play library only in Play; updater excluded | Review Google Play library guidance. GitHub credentials are absent from the Play installer flow |

Suggested purposes: app functionality, account management for optional external sessions, and security/abuse prevention for bounded API rate limiting. No advertising, marketing or analytics purpose is implemented. Do not claim “no data collected” while optional network searches/maps/sharing transmit covered information.

Encrypt in transit: HTTPS is required for Android server URLs, providers, Matrix, walking and address services. The compose worker/Redis network is private; terminate public API TLS at the owned gateway. Verify the actual deployment and SDKs before selecting the Console encryption answer.

Deletion: local removal is implemented. Matrix is an existing external account, not an Abeona-created account. The publisher's public policy/contact and any identifiable backend-log deletion route remain unset. Confirm external provider retention and whether the account-deletion policy applies to the final app behavior.

Sources: [Data Safety guidance](https://support.google.com/googleplay/android-developer/answer/10787469), [User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311), [account-deletion guidance](https://support.google.com/googleplay/android-developer/answer/13327111). Owner review and Console answers remain required.
