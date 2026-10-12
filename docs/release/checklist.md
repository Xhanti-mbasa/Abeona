# Abeona release gates

Status: **not Google Play ready**. Fill evidence and dates for the exact source SHA/artifacts being submitted; do not mark an item complete from an earlier app version or an illustrative design.

## Engineering

- [x] Recover existing source with blob-hash provenance; preserve package/database identity.
- [x] Approve visual direction and implement native design tokens/navigation.
- [x] Move operator extraction and PDF execution to the backend monorepo.
- [x] Separate Play updates from GitHub installer source/permission.
- [x] Add contract/outage/cache/retry checks; Node suite passed 29 tests.
- [x] Engineering source f1e8359 passes Android builds/lint and 27 critical device tests on GitHub API 36, Play API 35, and Play API 36/16 KB.
- [x] Compare actual Home/results/active captures against the approved design; scoped native QA passed.
- [x] Verify local candidate AAB/APK identities, permissions, installer exclusion, signatures, 16 KB packaging and SHA-256; exact evidence in verification.md.
- [x] Verify all six 64-bit native ELF load alignments, 16 KB bundle configuration and APK ZIP alignment; strict Play API 36/16 KB runtime suite passed.
- [ ] Verify release-signed in-place upgrade over a real existing installation and preserved data.
- [ ] Exercise slow/offline networks, permission denial/revocation, reminders, rotation/font scaling and process restart.
- [ ] Exercise live sharing expiry/stop and Android notification fallback on actual devices; Samsung Now Bar eligibility is firmware-controlled.

## Sources and deployment

- [ ] Provision an owned HTTPS backend; build its real URL into the candidate.
- [ ] Verify PRASA/CT Trains, MyCiTi and Golden Arrow access, parser behavior, source age and terms/licences.
- [x] Verify worker warm-up, retained snapshots, bounded fetches and outage/cache contracts locally; container API-to-worker health passed. Production deployment acceptance remains required.
- [ ] Verify logs/retention, gateway rate limits, TLS, secrets, immutable image deploy and rollback.
- [ ] Verify real routes at supported transfers. Unsupported or unconfirmed links must remain unavailable.
- [ ] Verify optional Matrix, Google profile, Photon/walking, map and WhatsApp configurations, or leave unverified optional integrations disabled.

## Google Play

Official requirements checked 12 October 2026:

- Target API 36 for new submissions/updates is implemented. [Official target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878).
- The Play variant uses Play in-app updates. External self-updating is excluded. [Device and Network Abuse policy](https://support.google.com/googleplay/android-developer/answer/16559646), [Play update implementation](https://developer.android.com/guide/playcore/in-app-updates/kotlin-java).
- The current 16 KB guide states a 1 February 2027 update enforcement date. Support/checks are still required for applicable 64-bit native libraries; recheck Console policy at submission. [Official page-size guide](https://developer.android.com/guide/practices/page-sizes).
- Data Safety covers relevant SDK and off-device processing. Complete the code-based worksheet and verify deployed data practices. [Official guidance](https://support.google.com/googleplay/android-developer/answer/10787469).
- Optional account/session behavior must be matched to account deletion requirements. This app connects to existing external accounts; it does not register an Abeona account. [Account-deletion guidance](https://support.google.com/googleplay/android-developer/answer/13327111).

Outstanding Console gates:

- [ ] Verified publisher account, app ownership/package eligibility and Play App Signing/upload key.
- [ ] Public final privacy policy with verified contact, processors and retention; accurate Data Safety form.
- [ ] Permission explanations/foreground-service declarations and any Health Connect declaration required by the final artifact.
- [ ] App access instructions for optional account features, content rating, target audience, countries and developer contact.
- [x] Prepare accurate store-copy draft, editable brand assets and six actual candidate screenshots. Final publisher/production listing review remains required.
- [ ] Internal testing install/update, pre-launch report, crash/ANR review and any account-specific closed-testing requirement shown in Console.
- [ ] Release review, phased rollout monitoring and a higher-version rollback plan.

A build produced without the production backend or upload-signing configuration is a development candidate. The compatible local development candidate uses the certificate from the provided 1.9.0 APK. Its package, Keystore alias and database filename also match. This does not establish a Play upload key or replace an actual in-place device test.
