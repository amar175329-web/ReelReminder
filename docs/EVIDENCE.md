# EVIDENCE LOG (append only): issue, command, date, environment, real output summary, label

## Step 0 — Existing Project Protection & Freshness (2026-11-28)
- **Command:** `gh auth status && gh repo list --limit 10 && gh release list -R amar175329-web/ReelReminder && git tag -f pre-v6 HEAD && git checkout -B v6/work && gitleaks version`
- **Date:** 2026-11-28
- **Environment:** Linux (local machine matching v3.0.0 build host), JDK 17, `/root/android-sdk` (`android-34`, `android-35`, `android-36`, `android-37.0`), local `/root/.android/debug.keystore` (Nov 22, 2618 bytes), `gitleaks 8.30.1`.
- **Output summary:** `gh` authenticated as `amar175329-web`; repo `amar175329-web/ReelReminder` clean at `21e3e81`; tag `pre-v6` and branch `v6/work` pushed to `origin`. Official freshness URLs verified HTTP 200 (`developers.facebook.com/documentation/instagram-platform/oembed`, `developer.android.com/google/play/requirements/target-sdk`, `github.com/gitleaks/gitleaks`).
- **Label:** REAL

## Issue 1 — Exact Reel Opening (ReelOpener, ShortcodeParser, MediaIdCodec, PlayerScreen, Debug Selector)
- **Command:** `cd /root/work/reelreminder && ./gradlew lintDebug testDebugUnitTest assembleDebug`
- **Date:** 2026-11-28
- **Environment:** Linux local build host, AGP 8.7.3, Kotlin 2.0.21, `compileSdk = 36` (with `android.suppressUnsupportedCompileSdk=36` in `gradle.properties`), `targetSdk = 36`, `minSdk = 26`, local debug keystore `/root/.android/debug.keystore`.
- **Output summary:** `BUILD SUCCESSFUL in 1m 23s`. `lintDebug` passed with 0 errors (fixed 22 pre-existing `MissingTranslation` entries in `values-hi/strings.xml`). `testDebugUnitTest` passed 100% (`ShortcodeParserTest`, `MediaIdCodecTest`, `ReelOpenerTest`, `UrlExtractorTest`, `NextTriggerCalculatorTest`). `assembleDebug` succeeded locally.
- **Label:** REAL (code, lint, unit tests, local debug build) | NOT RUN (device checks A3, A4 in `docs/DEVICE_CHECKLIST.md` — pending Milestone A gate)

## Issue 2 — Notification Tap Opens Exact Reel (PlayerActivity + Isolated PendingIntent)
- **Command:** `cd /root/work/reelreminder && ./gradlew lintDebug testDebugUnitTest assembleDebug`
- **Date:** 2026-11-28
- **Environment:** Linux local build host, AGP 8.7.3, `compileSdk = 36`, `targetSdk = 36`, local `/root/.android/debug.keystore`.
- **Output summary:** `BUILD SUCCESSFUL in 4m 2s`. `lintDebug` passed with 0 errors. `testDebugUnitTest` passed 100% including `NotificationIntentSpecTest` (proving 3 reels generate distinct `requestCode` and `reelreminder://reel/<id>` data URIs). `assembleDebug` succeeded locally. `NotificationPendingIntentTest` (`androidTest`) written; execution `NOT RUN` (no emulator/device attached).
- **Label:** REAL (code, lint, unit tests, local debug build) | NOT RUN (instrumented `NotificationPendingIntentTest` and device check A5 — pending Milestone A gate)

## Issue 3 — Missing Link Guard ("Not saved: this reel has no link") + Debug Helper
- **Command:** `cd /root/work/reelreminder && ./gradlew lintDebug testDebugUnitTest assembleDebug`
- **Date:** 2026-11-28
- **Environment:** Linux local build host, AGP 8.7.3, `compileSdk = 36`, `targetSdk = 36`, local `/root/.android/debug.keystore`.
- **Output summary:** `BUILD SUCCESSFUL in 2m 13s`. `lintDebug` 0 errors. `testDebugUnitTest` passed 100% including `ReelOpenerTest.missingOrInvalidLinkReturnsMissingLinkWithZeroLauncherCalls` (0 launcher calls for empty/blank/non-shortcode URLs). Added Settings > Debug > "Create reel without link" helper.
- **Label:** REAL (code, lint, unit tests, local debug build) | NOT RUN (device check A6 — pending Milestone A gate)

## Issue 4 — Share Capture, Diagnostics ("Last shared"), and Clipboard Dedup
- **Command:** `cd /root/work/reelreminder && ./gradlew lintDebug testDebugUnitTest assembleDebug`
- **Date:** 2026-11-28
- **Environment:** Linux local build host, AGP 8.7.3, `compileSdk = 36`, `targetSdk = 36`, local `/root/.android/debug.keystore`.
- **Output summary:** `BUILD SUCCESSFUL in 1m 41s`. `lintDebug` 0 errors. `testDebugUnitTest` passed 100% including `ShareCaptureParserTest`. Added `ShareDiagnosticsStore` + `Settings > Debug > Last shared` panel (MIME, extra keys, canonical link, shortcode, stream extra flag) and clipboard dedup check.
- **Label:** REAL (code, lint, unit tests, local debug build) | NOT RUN (device checks A7, A8 — pending Milestone A gate)

## Milestone A Gate — Pre-release v6-mA APK Artifact
- **Command:** `sha256sum app/build/outputs/apk/debug/app-debug.apk && stat -c "bytes=%s" app/build/outputs/apk/debug/app-debug.apk`
- **Date:** 2026-11-28
- **Environment:** Local build host with original `/root/.android/debug.keystore` (same signing key as `v3.0.0`).
- **Output summary:** `app-debug.apk` = `18973084` bytes, SHA-256 = `f730e89a12491f5fbd6d713ef3b239ce60511e34431d065103efafa3cfd2a10e`.
- **Label:** REAL
