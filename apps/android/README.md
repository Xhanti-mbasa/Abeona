# Abeona Android

Native Kotlin and Jetpack Compose app. Keep application ID `za.co.cttrains.app`, encrypted Room `transit.db` schema 1, Keystore aliases and serialized legacy identifiers for compatible upgrades.

See the monorepo [setup guide](../../docs/setup.md), [design system](../../docs/design/design-system.md), [integration inventory](../../docs/production/provider-inventory.md) and [release gates](../../docs/release/checklist.md).

With JDK 17 and Android SDK 36 configured:

```sh
./gradlew :core:test :scrapers:test :app:lintGithubDebug :app:lintPlayRelease
./gradlew :app:assembleGithubDebug :app:bundlePlayRelease
./gradlew :app:connectedGithubDebugAndroidTest
./gradlew :app:connectedPlayDebugAndroidTest
```

`play` uses supported Google Play updates and excludes the GitHub installer and installer permission. `github` supports separately reviewed external APK updates. Development signing is a testing configuration; production upload signing and an owned HTTPS transit backend remain release requirements. Never uninstall an existing installation as an upgrade workaround: confirm the certificate and export a backup before testing. CI publishing requires an explicit manual flag and is separate from Google Play submission.

Operator extraction and PDF processing run in the backend worker. The app reads its configured API and retains usable local data on failure. Source-specific fixtures and pure planning logic live at the monorepo root.
