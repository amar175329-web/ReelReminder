# DEVICE CHECKLIST (owner runs on the real phone; report OK / NOT OK + screenshot). The agent fills the expected result column from the build.
## Milestone A
| # | Step | Report |
|---|---|---|
| A1 | Install the APK. If "App not installed", STOP and tell the agent (signing mismatch, ADR-7). Do not uninstall without a backup | |
| A2 | In Instagram save 3 DIFFERENT reels via Share > "Share to..." > ReelReminder with notes A, B, C | |
| A3 | Settings > Debug > Open method = M1. Today: Play A, B, C. Does the SAME reel open each time? (Y/N x3). Repeat for M2, M3 and the in-app player (Reel ID shown matches?) | |
| A4 | Vault > collection > Play A, B, C: same reel each time? | |
| A5 | Schedule A, B, C for +2, +3, +4 minutes. Lock phone. Tap each notification: correct reel each time? (also after force-stopping the app) | |
| A6 | Debug > "Create reel without link" > Play: message "Not saved", Instagram does NOT open | |
| A7 | Debug > Last shared: screenshot (shows text and extra keys) | |
| A8 | In Instagram's own Send row, is ReelReminder listed? (expected NO) Is it under "Share to..."? | |
## Milestone B
| B1 | Screenshots on gesture and 3-button navigation: nothing under the clock or nav bar; last item scrolls above the nav | |
| B2 | Theme is light by default; background glow is soft and clearly behind the content | |
| B3 | Logo lighter in header and as launcher icon; notification icon is a clean white shape | |
| B4 | Add > Date > pick year 2030 > Cancel: value unchanged. Pick again > OK: applied. Never blank | |
| B5 | Date or time picker > Back: form is still there, same values, no white screen. Rotate: values kept | |
| B6 | Repeat > Custom > every 5 years: summary line correct; Weekly days; Monthly; Yearly; Ends options | |
| B7 | Reboot phone: reminders still fire. Update the app over the old one: saved reels still there | |
## Milestone C
| C1 | Choose a cover image: shows on carousel, plan row, Vault fan, collection grid; Remove falls back | |
| C2 | Edit a reel and Cancel; Edit and Save; Pause; Mark watched; Delete then Undo | |
| C3 | Rename a collection; delete one with "move reels"; delete one with reels; last one is protected | |
| C4 | Mark a reel watched: streak chip opens Stats; numbers are real (new install shows 0) | |
| C5 | Coming up shows only tomorrow or later, nearest first, with Tomorrow / date labels | |
## Milestone D
| D1 | Website URL opens; Download APK works; SHA-256 matches; privacy page opens | |
| D2 | Settings > Delete all my data works; Export then Import restores | |
| D3 | Notifications denied: banner appears and the app does not crash | |
