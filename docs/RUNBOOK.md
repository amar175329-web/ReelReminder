# RUNBOOK
Build: `./gradlew assembleDebug` (APK path printed). Release: push tag `v6.x`, CI attaches the APK and SHA256SUMS to a GitHub Release. Verify: `curl -sIL <apk url>`; compare `sha256sum`.
Roll back app: install the previous release APK (same signing key needed, ADR-7). Roll back code: `git checkout pre-v6` / revert.
Website: `vercel --prod` from /site (after `vercel login` by the owner); rollback = promote the previous deployment.
Rotate/replace keystore: new key means users must reinstall (data loss warning; use Export/Import first).
Backups: Settings > Export; restore with Import.
Incident: if wrong-reel bug returns, open Settings > Debug, copy "Last shared" and the open-method log into an issue.
