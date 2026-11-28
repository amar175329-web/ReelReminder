# APP FLOW (entry -> screens -> actions -> system response -> success -> failures)
FLOW 1 Save by sharing: Instagram reel > Share > "Share to..." > ReelReminder -> Save sheet prefilled (link, default next full hour) -> pick collection, date/time, repeat -> Save -> alarm set, toast with time. FAILS: not an Instagram link (message), past one-time time, Weekly with no day, notifications denied (banner), exact alarms denied (explain + settings).
FLOW 2 Save by clipboard: open app with a copied reel link -> banner "Save the reel you copied?" -> Save sheet. FAILS: clipboard empty/other text (no banner).
FLOW 3 Reminder fires: notification -> tap -> PlayerActivity(reelId) -> exact reel shown/opened; actions Snooze 10 min / Done. FAILS: reel deleted (message), offline (offline state + Open in Instagram), Instagram missing (browser).
FLOW 4 Play from app: Today carousel/plan row or Vault > collection > Play -> same as flow 3. FAILS: no link -> "Not saved".
FLOW 5 Edit: poster "..." or row tap > Edit -> same sheet -> Save reschedules / Cancel changes nothing.
FLOW 6 Delete: "..." > Delete -> reel gone, alarms cancelled, snackbar Undo restores for 4.5 s.
FLOW 7 Collections: Vault > New collection / collection "..." > Rename / Delete (move reels or delete them; last collection protected).
FLOW 8 Stats: tap streak chip -> Stats sheet.
FLOW 9 First launch empty state: honest empty Today with instructions (how to share). No demo data.
FLOW 10 Permission denied / offline / reboot: banners, offline player state, alarms restored after reboot and app update.
FLOW 11 Delete all my data: Settings > confirm > everything wiped, alarms cancelled.
