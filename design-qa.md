# Abeona native One UI design QA

result: blocked

Scope: the accepted One UI proposal implemented in the existing Kotlin/Compose Android app. This supersedes the graphite/citron visual review. Functionality, providers and storage remain unchanged.

## Evidence and capture conditions

Initial native captures: commit 84fc8d2, workflow 38784267541, API 36, 1080 × 2343 pixels at density 440, approximately 393 × 852 dp. Light and dark Home, live route results and running guidance were compared side by side with the approved proposal at the same width. Saved, Journeys and Settings were also reviewed. The Android system owns status and gesture areas. Native sans serif and actual journey data replace the proposal's illustrative text.

## Findings awaiting recapture

| Severity | Finding | Applied correction |
| --- | --- | --- |
| P2 | Scheduled-information caption could be clipped behind Home's persistent action | Caption moved into the footer above the action |
| P2 | Large-font navigation labels wrapped inside words | Selected tab expands and retains its full label; other tabs retain named accessibility semantics and icons |
| P2 | Active heading had an extra 20 dp gap and lacked station context | Merge header group, restore labelled station progress, retain real phone-guidance status |
| P2 | Light small accent text on tint measured just below 4.5:1 | Darken only the on-container text role to #0068D1 |
| P2 | Empty saved-place cards spent extra height on a separate button | Whole-card edit action, compact icon tile, persistent Add action |

Initial build/core/lint and bundle checks passed. All four native configurations found one duplicate Compare selector in a test; that selector is now scoped to its provider choice. This is not yet a final verification result. Final captures, test counts and signing evidence must be recorded before delivery.
