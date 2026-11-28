# DECISIONS (ADRs). Status: ACCEPTED / PROPOSED (owner to confirm) / OPEN
- ADR-1 ACCEPTED Native Kotlin + Compose, not PWA/Expo. Why: share target, exact alarms and reliable notifications are core; the owner already has a native app. Risk: Android only. Section 49.1.
- ADR-2 ACCEPTED Local-only data, no backend, no accounts. Why: smaller real product, no secrets, no server cost. Consequence: no sync; provide Export/Import. No auth tables needed.
- ADR-3 PROPOSED Reel opening: ReelOpener (load by id) with in-app embed player + three Instagram open methods behind a Debug selector. Default = the first method that passes 3 of 3 on the owner's phone. Instagram's `instagram://media?id=` is undocumented: UNKNOWN until tested.
- ADR-4 ACCEPTED Thumbnails: no scraping (Section 54). Owner-picked cover via Photo Picker, else gradient. Official oEmbed proxy is a later option.
- ADR-5 ACCEPTED Light theme by default (owner feedback: too dark); dark only as a Setting.
- ADR-6 ACCEPTED Vercel hosts the static website only; the APK is a GitHub Release asset.
- ADR-7 OPEN Signing: in-place updates need the SAME signing key every time. Until the owner has a release keystore (generated once, backed up offline, stored as a CI secret, never in git) builds are debug-signed and installing over an app signed with a different key will fail. Agent must state this clearly and never uninstall the owner's app without warning about data loss.
- ADR-8 ACCEPTED AGENTS.md is the single instruction source; CLAUDE.md and GEMINI.md are pointers (Section 38.3, FRESHNESS).
