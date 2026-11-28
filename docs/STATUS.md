# STATUS

## Step 0: Existing Project Protection (2026-11-28) — REAL
- **GitHub auth & repo:** REAL (`gh auth status`: logged in as `amar175329-web`, scopes `gist, read:org, repo`; repo `amar175329-web/ReelReminder`, public).
- **Known-good rollback point:** Tag `pre-v6` created at commit `21e3e81` on `main`. Active work branch: `v6/work`.
- **Working tree before Step 0:** Clean on `main` (`21e3e81`).
- **Build config inspected:** AGP `8.7.3`, Kotlin `2.0.21`, Compose BOM `2024.10.01`, Room `2.6.1`, KSP `2.0.21-1.0.28`, `compileSdk = 35`, `minSdk = 26`, `targetSdk = 36`, `versionCode = 1`, `versionName = "3.0"`, ARM64/host AAPT2 override `../tools/aapt2/aapt2`.
- **Existing tests inspected:** `UrlExtractorTest.kt`, `NextTriggerCalculatorTest.kt` (unit tests); `androidTest` = none yet.
- **Signing config:** Debug-signed only (no `.jks`/`.keystore` in repo; ADR-7). Release keystore not yet created.
- **Current Vercel project:** NOT LINKED (no `.vercel/` directory; `vercel login` required before deploy).
- **Existing GitHub Release:** `v3.0.0` ("ReelReminder v3.0.0") with `app-debug.apk`.
- **Tools verified:** `gitleaks` `8.30.1` installed at `/root/.local/bin/gitleaks`.
- **Preserved:** All existing `main` history, `v3.0.0` release, Room schema v2 (`Reel`, `Category`), alarm scheduling, and existing tests.

## Owner Device Observations (Previous Build)
- **REAL (owner device test of previous build):** app installs, share-to-save works via "Share to...", reels list shows, notifications fire, design concept approved.
- **REAL bugs:** Play and notification open a different reel; UI overlaps status bar; theme too dark; blank date field; picker dark; back shows white screen; custom repeat odd; no thumbnails; no edit/delete/cancel; streak chip inactive; logo too dark.
- **NOT RUN:** Issues 3 to 24 in `docs/ISSUES.md` (not yet implemented/tested for v6).
- **Issue 1 (PARTIAL):** `ShortcodeParser`, `MediaIdCodec`, `ReelOpener.open(reelId)`, `PlayerScreen(reelId)`, and `Settings > Debug > Open method` implemented and wired to all Play buttons. `compileSdk` upgraded to `36` to match `targetSdk = 36` (with `android.suppressUnsupportedCompileSdk=36` for AGP 8.7.3). `./gradlew lintDebug testDebugUnitTest assembleDebug` = **REAL** (0 lint errors, 100% unit tests pass, local debug APK builds with same `/root/.android/debug.keystore`). Device checks A3/A4 = **NOT RUN** (Milestone A gate).
- **Issue 2 (PARTIAL):** `PlayerActivity` created (loads reel fresh from Room by ID), `NotificationHelper` uses isolated `PendingIntent` (`requestCode = reelId.toInt()`, `data = reelreminder://reel/<id>`, `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE`) with Snooze 10 min + Done actions. `./gradlew lintDebug testDebugUnitTest assembleDebug` = **REAL** (0 lint errors, unit tests pass). Instrumented test + Device check A5 = **NOT RUN**.

## Next Step
- Issue 3: Play on a reel with no saved link shows "Not saved: this reel has no link" with 0 launcher calls + Debug button "Create reel without link".
