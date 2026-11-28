# PRD: ReelReminder (Android)
## Summary and vision
People watch a motivating reel, feel inspired, then the feed scrolls on and the motivation is lost. ReelReminder lets the user save a reel and choose exactly when it comes back, so motivation is used when needed.
## Users
Primary: the owner (Hindi/English speaker, phone-first). Later: anyone who watches reels for motivation, study or routines. No accounts (all data stays on the phone).
## User stories
1. As a user I share a reel from Instagram to the app, pick a collection and a time, and get a reminder that opens that exact reel.
2. I can group reels in collections (Morning, Evening, Motivation, Study, my own).
3. I can repeat: once, daily, weekly, monthly, yearly, or every N days/weeks/months/years, with an end rule.
4. I can edit, pause, delete (with Undo) a reel, and rename/delete a collection.
5. I can see today's plan, what is coming up, and my watching streak.
6. I can play any saved reel from inside the app.
## Functional requirements and acceptance
FR1 exact reel on Play/notification (Issues 1-3). FR2 share capture (4). FR3 scheduling (9-11). FR4 repeat/ends (10-11). FR5 management (13-14). FR6 stats (15). FR7 Today/Coming up (16). FR8 covers (12). FR9 privacy and data deletion (17). Each FR's acceptance criteria are the acceptance lines in docs/ISSUES.md.
## Non-functional
Offline-first (everything except the embed player works offline); alarm fires within the exact-alarm tolerance of the OS (UNKNOWN until measured on device); cold start under 2 s on a mid-range phone (target, measure); no secrets in the app; minimum permissions; accessibility target WCAG 2.2 AA principles (touch >= 48dp, contrast 4.5:1, labels).
## Edge cases and error states
No link saved -> "Not saved". Non-Instagram text shared -> rejected with message. Notifications denied -> persistent banner with a button to settings. Exact alarms denied -> explain and fall back honestly. Offline -> player shows offline state plus "Open in Instagram". Instagram app missing -> browser fallback. Past one-time time -> rejected. Month-end/leap-year repeats handled. Device reboot or app update -> alarms restored.
## Roles and permissions
Single local user; no authentication or authorization layer (ADR-2). Android permissions: POST_NOTIFICATIONS, exact alarms, RECEIVE_BOOT_COMPLETED, INTERNET (embed player only).
## Data requirements
Reel, Category, WatchLog, settings (see docs/TRD.md). Deleted on request ("Delete all my data").
## Integrations
Instagram: opening the saved link and the public embed only. No Instagram API, no login, no scraping.
## Notifications
Local exact notifications; actions Snooze 10 min and Done; tap opens the exact reel.
## Analytics
None (no tracking). Local Debug screen only.
## Pricing
None.
## MVP / Phase 2 / roadmap
MVP: issues 1-24. Phase 2: backup/restore polish, home-screen widget, official oEmbed thumbnails through a proxy (needs Meta app + review), iOS/PWA decision. Roadmap: sync (would need accounts and a backend: new ADR).
## Risks
Instagram changes deep-link or embed behaviour (mitigate: 3 open methods + embed + browser). Alarm reliability on aggressive battery managers (test, document). Signing-key mismatch blocks in-place updates (ADR-7). Terms for embed use (verify).
## Assumptions
Android 10+ phones; owner distributes the APK directly (not Play Store) for now; light theme default.
## Open questions
Does Instagram's Share send any image/stream? (Issue 4 diagnostics). Which open method works on the owner's phone? (Issue 1 table). Release keystore: who generates it? (ADR-7).
## Non-goals
Downloading or re-hosting reels; scraping; accounts/sync; ads; analytics; iOS.
