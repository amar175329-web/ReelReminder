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
- **NOT RUN:** Issues 1 to 24 in `docs/ISSUES.md` (not yet implemented/tested for v6).

## Next Step
- Confirm Step 0 + Issue 1 plan, then implement Issue 1 (exact reel opening via `ReelOpener`, `ShortcodeParser`, `MediaIdCodec`, `PlayerScreen`, and Debug open-method selector).
