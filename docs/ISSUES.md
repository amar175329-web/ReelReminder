# ISSUES (ordered work list). Status values: TODO / IN PROGRESS / DONE (evidence in EVIDENCE.md) / PARTIAL / BLOCKED.
Source: real-device testing by the owner (labels: REAL = observed by owner). Do them in this order. Acceptance = every line must have evidence.

| # | Milestone | Issue | Status |
|---|---|---|---|
| 1 | A | Play opens a different reel (Today, Vault, collection) | PARTIAL |
| 2 | A | Notification tap opens a different reel | TODO |
| 3 | A | Play on a reel with no saved link must say "Not saved" | TODO |
| 4 | A | Share capture, share-sheet visibility, diagnostics | TODO |
| 5 | B | Content overlaps status bar / nav bar; screens must scroll | TODO |
| 6 | B | Theme too dark; big circle looks like foreground | TODO |
| 7 | B | Logo too dark; app icon + notification icon | TODO |
| 8 | B | Back from picker/sheet shows a white screen | TODO |
| 9 | B | Date/time fields blank; pickers dark; cancel must keep old value | TODO |
| 10 | B | Repeat UI: Custom looks odd; more repeat types; Ends | TODO |
| 11 | B | Scheduler engine, alarms reliability, DB migration (no data loss) | TODO |
| 12 | C | Thumbnails / covers everywhere | TODO |
| 13 | C | Edit, delete (Undo), cancel, pause, mark watched | TODO |
| 14 | C | Collections: create, rename, delete (move or delete reels) | TODO |
| 15 | C | Streak chip does nothing; real stats | TODO |
| 16 | C | Today screen and "Coming up" meaning | TODO |
| 17 | D | Permissions, privacy, delete-my-data, backup | TODO |
| 18 | D | Security checks (Section 49.2, M04) | TODO |
| 19 | D | UI parity with prototype + accessibility | TODO |
| 20 | D | Tests, coverage, lint, quality-gate report (Section 48) | TODO |
| 21 | D | CI pipeline (GitHub Actions) | TODO |
| 22 | D | Website on Vercel (landing, download, privacy, design preview) | TODO |
| 23 | D | APK release with checksum; install-over-existing check | TODO |
| 24 | D | Handover docs + final report (A to K) | TODO |

## Milestone A: exact reel (CRITICAL, do first)
### 1. Play opens a different reel (REAL, owner: "a new reel I never saved opens, Instagram just opens")
Hypotheses (verify with logs, do not guess): wrong/overwritten URL stored; shared text parsed wrongly; Instagram ignores the deep link and opens its feed; stale PendingIntent.
Fix: one `ReelOpener.open(reelId)` used by EVERY Play button and the notification. It loads the reel from Room BY ID at tap time and logs `id, shortcode, uri` (no personal data). Shortcode regex `instagram\.com/(?:reel|reels|p|tv)/([A-Za-z0-9_-]{5,})`; store shortcode + canonical `https://www.instagram.com/reel/<code>/` (drop tracking params). Three open methods behind Settings > Debug > Open method: M1 `instagram://media?id=<mediaId>` (mediaId = shortcode decoded with base64url alphabet; UNKNOWN/undocumented, test on device), M2 ACTION_VIEW canonical https URL with package com.instagram.android, M3 ACTION_VIEW https in browser. Plus in-app `PlayerScreen(reelId)`: WebView with Instagram's public embed for that shortcode, showing note and "Reel ID: <code>" (confirm terms first, record source and date in docs/FRESHNESS_CHECK.md; if unclear, make "Open in Instagram" the primary button and label the embed UNKNOWN).
Acceptance: (a) unit tests for ShortcodeParser (valid, invalid, tracking params, reel/p/tv, other domains rejected) and MediaIdCodec (round trip); (b) instrumented or unit test proving ReelOpener reads by id and passes the stored code; (c) DEVICE: with 3 different saved reels, Play from Today carousel, Today's plan row and Vault > collection shows/opens the SAME reel as saved, 3 of 3 each, for each method; owner fills the method table in DEVICE_CHECKLIST; default method = first that passes 3 of 3 (ADR-3).
Evidence: test output; device table.
### 2. Notification tap opens a different reel (REAL)
Fix: PendingIntent to PlayerActivity; requestCode = reelId.toInt(); intent.data = `reelreminder://reel/<id>`; extra reelId; FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE. PlayerActivity loads the reel by id (never from a cached variable). Notification actions: Snooze 10 min, Done.
Acceptance: instrumented test creating 3 notifications whose PendingIntents are distinct (different data URI, requestCode); DEVICE: reels scheduled +2/+3/+4 min, tapping each shows its own Reel ID (3 of 3), also with phone locked and app killed.
### 3. "Not saved" (REAL)
Fix: if url empty or no shortcode, show "Not saved: this reel has no link" and do NOT launch Instagram or the browser.
Acceptance: unit test (launcher mock receives 0 calls); Debug > "Create reel without link" on device shows the message.
### 4. Share capture, visibility, diagnostics
Fix: intent-filter ACTION_SEND text/plain; extract the first Instagram URL from EXTRA_TEXT; reject others ("Only Instagram reel links work"). Settings > Debug > "Last shared": raw text, mime type and extra KEYS (no content beyond the link) so we learn whether Instagram ever sends a thumbnail/stream. Sharing Shortcuts so the app ranks in the system chooser. Clipboard fallback: on app open, if the clipboard holds an Instagram reel link not yet saved, show "Save the reel you copied?". In-app tip: Share > "Share to..." > ReelReminder.
Acceptance: unit tests on parsing real-world strings; DEVICE: app appears under Share to...; owner records whether it appears in Instagram's own Send row (expected NO, UNKNOWN until checked); clipboard banner works.

## Milestone B: foundation UI and scheduling
### 5. Insets and scrolling (REAL: header overlapped the clock)
Fix: edge-to-edge with insets: top = statusBarsPadding + 16dp, bottom = navigationBarsPadding; each screen one LazyColumn with bottom contentPadding 160dp (floating nav 84dp + gap) so content scrolls under the nav; status-bar icon colour matches theme.
Acceptance: DEVICE screenshots on 3-button and gesture navigation: no overlap with clock/battery, last item reachable above nav.
### 6. Theme and background (REAL: too dark; circle looked like foreground)
Fix: LIGHT by default regardless of system theme; Settings > Theme (Light default / Dark / System). Background glow = Brush.radialGradient aqua alpha .30 and sky alpha .26 drawn BEHIND content with no hard edge. Text contrast >= 4.5:1 (body) in both themes.
Acceptance: screenshot compare with prototype; contrast check recorded.
### 7. Logo, app icon, notification icon (REAL: logo too dark)
Fix: use assets/logo.png (light version) as the in-app header logo (42dp, 12dp corners) and adaptive icon foreground (background #CFF6F0, keep artwork inside the safe zone). Notification small icon must be a white monochrome silhouette (Android requirement): create `ic_stat_reel` (clock ring + play).
Acceptance: assets present; launcher icon and notification icon visible in device screenshot.
### 8. Back navigation never shows a white screen (REAL)
Fix: pickers/sheets are dialogs over the form; form state lives in a ViewModel (SavedStateHandle); BackHandler closes the top-most layer only; every screen root draws the app background.
Acceptance: DEVICE: open Add > date picker > Back: form is still there with the same values; Back from a sheet closes the sheet; Back on Today exits. Rotation keeps entered values. Unit test for form state holder.
### 9. Date and time (REAL: field showed empty "Pick a date and time"; calendar dark)
Fix: Date and Time are always-filled fields (default = next full hour), shown like "Fri, 27 Nov 2026" and "8:00 pm" (locale). Quick chips: In 1 hour, Tonight 8 pm, Tomorrow 7 am. Material3 DatePicker (with year selection) and TimePicker themed LIGHT with a white glass container. OK applies; Cancel, tapping outside or system Back changes nothing. A one-time reminder in the past is rejected with a clear message. Store local date + time + zone id.
Acceptance: unit tests (default value, validation, cancel keeps value); DEVICE: pick a year in 2030, press Cancel, value unchanged; pick again, OK, value applied.
### 10. Repeat UI (REAL: Custom looked odd; owner wants yearly, monthly, 5 and 10 years)
Fix per prototype: Once, Daily, Weekly (day circles), Monthly, Yearly, Custom = "Repeat every" stepper (- N +) + unit chips Days/Weeks/Months/Years + quick picks (Every 2 weeks, 3 months, 5 years, 10 years). Ends: Never / On date / After N times. Live summary line ("Starts Fri, 27 Nov 2026 at 8:00 pm, repeats every 5 years. Never ends."). N >= 1.
Acceptance: UI test or screenshots; summary strings unit tested.
### 11. Scheduler engine, alarms, migration
Fix: `NextTrigger.compute(reel, now)` with java.time: Once, Daily, Weekly(days), Monthly (31st clamps to month end), Yearly (29 Feb -> 28 Feb in non-leap years), Custom N units, Ends by date/count, DST-safe. Exact alarms (setExactAndAllowWhileIdle); if exact alarms are not allowed (Android 12+), explain and open the system setting; fall back honestly. Reschedule after fire, edit, BOOT_COMPLETED, MY_PACKAGE_REPLACED; cancel on delete/pause. Room schema bump with a tested Migration from the installed version: no saved reel is lost. Old rows get defaults.
Acceptance: unit tests for each repeat type incl. month-end, leap year, DST, end conditions, count; MigrationTestHelper test (NOT RUN if no emulator: say so); DEVICE: reboot, alarms still fire; install over the existing app keeps reels (if "App not installed" = signing mismatch: report UNKNOWN->MISSING, do NOT uninstall without telling me).

## Milestone C: content and management
### 12. Thumbnails / covers (owner wants the saved reel's picture visible)
Facts: Instagram's share sends text only (UNKNOWN: verify with issue 4 diagnostics). Scraping Instagram pages is NOT allowed (Section 54).
Fix: Cover = image the owner picks with the Android Photo Picker (no storage permission), copied to app storage, loaded with Coil; fallback = category gradient poster. Show covers on Today carousel posters, Today's plan rows (40x52dp), Vault collection fan, collection grid, Add/Edit sheet. Optional later (NOT IMPLEMENTED, out of scope): official Instagram oEmbed thumbnail through a small server proxy that holds the Meta token; needs a Meta developer app and review; research first.
Acceptance: DEVICE: choose cover, appears in all listed places; remove cover falls back; no network request for thumbnails (verify none made).
### 13. Edit, delete, cancel, pause, watched
Fix: poster "..." menu: Edit, Mark watched/not, Pause/Resume, Delete (snackbar Undo 4.5 s), Cancel. Row tap opens Edit. Add/Edit sheet has Save + Cancel. Editing reschedules; delete cancels alarms; Undo restores and reschedules; pause cancels alarms.
Acceptance: unit/UI tests for each action and alarm side effects; DEVICE walkthrough.
### 14. Collections
Fix: seeded Morning, Motivation, Evening, Study; create, rename, delete: delete asks "move reels to <other>" or "delete reels too" or plain delete if empty; the last remaining collection cannot be deleted. Vault row shows count and next reminder; collection screen lists its reels.
Acceptance: unit tests; DEVICE walkthrough.
### 15. Streak chip and stats (REAL: tapping did nothing)
Fix: Room table WatchLog(reelId, watchedAt). Streak = consecutive local days (ending today or yesterday) with at least one reel marked watched. Tap on the chip opens Stats sheet: streak, 7-day dots, watched today N of M, per-collection counts. New install shows 0 (no demo numbers).
Acceptance: unit tests for streak edge cases (gap, midnight, timezone); DEVICE.
### 16. Today screen and Coming up
Fix: carousel = next 6 pending by next trigger; Today's plan = reels whose next trigger is today (with done state + progress); Coming up = next trigger tomorrow or later, nearest first, today excluded, labels "Tomorrow" or "Sat 28 Nov" + time, subtitle "Reels scheduled for tomorrow and later, nearest first.", empty text "Nothing scheduled after today."
Acceptance: unit tests for the three groupings; DEVICE.

## Milestone D: quality and delivery
### 17. Permissions, privacy, data deletion, backup
Fix: POST_NOTIFICATIONS and exact-alarm permission requested at the moment of need with a clear reason; INTERNET only used by the embed player; Settings > "Delete all my data" (confirm, wipes Room, covers, alarms); optional Export/Import backup (JSON) so updates never lose reels. Privacy policy page on the site matching real behaviour (Section 37).
Acceptance: permission-denied paths tested (banner, no crash); delete-all verified on device; privacy page URL opens.
### 18. Security
Fix/check: intents and shared text validated; WebView: JavaScript only for the embed, no file/content access, no JS bridge, navigation allowlist (instagram.com), clear cache on delete-all; no secrets in app; release keystore never in git (offline backup; CI secret); `gitleaks git -v` and `gitleaks dir -v .` = 0 findings; dependency vulnerability check on production dependencies (tool UNKNOWN: pick, run, record); targetSdk 36 (verify on the Play Console page; UNKNOWN until checked).
### 19. UI parity and accessibility
Check each screen against prototype/index.html (screens, states, tokens). Touch targets >= 48dp, contrast, content descriptions on Play/Done/menu, 200% font scale with no clipping, reduced-motion respected, TalkBack labels. Record gaps honestly.
### 20. Tests and quality gates (Section 48)
Run lint, unit tests, coverage (>= 70% on logic modules: parser, codec, NextTrigger, repository, streak), build. Report in the Section 48.5 table. Unrunnable items = NOT RUN + exact command.
### 21. CI
GitHub Actions on push/PR: lint, unit tests, assembleDebug, gitleaks, dependency check; tag push builds the APK and attaches it to a Release. A failing step blocks the merge.
### 22. Website (Vercel)
/site static: landing (honest: Android app, what it does), Download APK button (GitHub Release URL) with SHA-256 and install steps, privacy.html, preview.html (prototype). Security headers in vercel.json. Vercel hosts the site only (the app is not hosted there). Deploy, then verify the PUBLIC URL with real requests; Lighthouse mobile median of 3 >= 70/90/90/90 (run from CI or a machine with Chrome; otherwise NOT RUN + command).
### 23. APK release
GitHub Release (tag v6.x) with the APK and SHA256SUMS. Be honest: debug-signed unless a release keystore exists (ADR-7). Verify: `curl -sIL <url>` returns 200 and a plausible size; downloaded sha256 matches. Install-over-existing check (see 11).
### 24. Handover
Update STATUS, DECISIONS, RUNBOOK; final report in the 00_CORE_PROMPT A to K format; ONE next step.
