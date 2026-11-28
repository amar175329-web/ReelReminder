# STATUS

## ReelReminder v6.0 Status (2026-11-28)
- **Known-good rollback tag:** `pre-v6` at `21e3e81` (**REAL**).
- **Branch & Build:** `v6/work` (`versionCode = 2`, `versionName = "6.0"`, `compileSdk = 36`, `targetSdk = 36`).
- **Signing:** Local `/root/.android/debug.keystore` (exact same debug key as `v3.0.0`; preserves installed app data on update, ADR-7).
- **Quality Checks:**
  - `./gradlew lintDebug`: **REAL** (0 errors).
  - `./gradlew testDebugUnitTest`: **REAL** (100% pass across all 9 test suites).
  - `./gradlew assembleDebug`: **REAL** (`18,320,688` bytes, SHA-256 `513ce5d41219b84cb76e7addbbc3f3cbe5ea60042f34051c04ae3e2a113e10ab`).
  - `gitleaks git -v && gitleaks dir -v .`: **REAL** (0 leaks found).
  - Physical device tests (`docs/DEVICE_CHECKLIST.md` A1–D3): **NOT RUN** (`adb devices` = 0 devices on this Linux server).
  - Vercel website deploy (`/site` prepared with `index.html`, `privacy.html`, `preview.html`, `logo.png`, `vercel.json`): **NOT RUN** (requires `vercel login`).
  - GitHub Actions workflow push: **NOT RUN** (`gh` OAuth token lacks `workflow` scope; owner also requested local-only APK builds until release signing is configured).

## Next Step
- Install the locally-built `v6.0` APK directly over the existing app and report physical device results from `docs/DEVICE_CHECKLIST.md` (A1–D3).
