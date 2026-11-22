# ReelReminder

A local-only Android app that saves shared Instagram reel links and reminds you
about them with exact `AlarmManager` notifications. No backend, no accounts, no
network calls — everything lives in a Room database on the device.

## APK

```
builds/reelreminder-debug.apk
```

Built with `./gradlew assembleDebug`. The same artifact is produced at
`app/build/outputs/apk/debug/app-debug.apk`.

- Package: `com.reelreminder.app`
- Version: 1.0 (versionCode 1)
- minSdk 26, targetSdk 35, compileSdk 35
- Kotlin 2.0.21, AGP 8.7.3, Gradle 8.9, Compose BOM 2024.10.01, Room 2.6.1

## Build

Requirements: JDK 17 and an Android SDK with platform 35 + build-tools 35.

```bash
cd /root/work/reelreminder
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

`local.properties` must point at your SDK (`sdk.dir=/root/android-sdk` on this
machine).

### ARM64 build hosts

Google's Maven AAPT2 artifact is x86_64-only, so on ARM64 (Termux/PRoot, Apple
Silicon Linux VMs, etc.) Gradle fails with `Illegal instruction`. This project
ships a wrapper at `tools/aapt2/aapt2` and sets:

```properties
android.aapt2FromMavenOverride=../tools/aapt2/aapt2
```

The wrapper uses the bundled aarch64 AAPT2 on ARM64 and delegates to the SDK
build-tools binary on x86_64. On a normal x86_64 machine you can remove that
line from `gradle.properties` if you prefer the Maven artifact.

## How it works

| Piece | Responsibility |
|---|---|
| `MainActivity` | Handles `ACTION_SEND` / `text/plain`, requests `POST_NOTIFICATIONS`, opens exact-alarm settings when needed. |
| `ReelReminderApp` | Home ↔ Schedule state navigation, share prefill, snackbars. |
| `HomeScreen` | List of reminders with pause/resume, edit, delete, and empty-state share instructions. |
| `ScheduleScreen` / `ScheduleViewModel` | Once (date + time), Daily (time), Weekly (weekday chips + time), live "next reminder" preview and validation. |
| `Reel`, `ReelDao`, `AppDatabase`, `ReelRepository` | Room storage: `id, url, note, scheduleType, time, daysOfWeek, nextTriggerAt, done, createdAt` plus `paused` for the pause toggle. |
| `UrlExtractor` | Pulls the first `http(s)` URL out of shared text. |
| `NextTriggerCalculator` | Computes the next occurrence for once/daily/weekly schedules (unit tested). |
| `AlarmScheduler` | `setExactAndAllowWhileIdle`, falling back to an inexact alarm when exact alarms are not permitted. |
| `AlarmReceiver` | Shows the notification, marks Once reminders done, and reschedules Daily/Weekly. |
| `NotificationActionReceiver` | Handles **Snooze 10 min** and **Done**. |
| `BootReceiver` | Reschedules active reminders after reboot, app update, time change, or timezone change. |
| `NotificationHelper` | High-importance channel, content intent that opens the link, snooze/done actions. |

### Scheduling behavior

- **Once** fires at the chosen date/time, then is marked done.
- **Daily** fires at the chosen time and reschedules for the next day.
- **Weekly** fires on each selected weekday at the chosen time and reschedules
  for the next selected day.
- If a one-time alarm arrives late (device asleep, exact alarms disabled), it is
  shown rather than dropped; a missed occurrence is parked a few seconds out so
  it still surfaces.
- Paused and completed reminders are skipped by the alarm receiver.
- Invalid weekly data (no days) parks the reminder one year out instead of
  re-firing in a loop.

### Permissions

- `POST_NOTIFICATIONS` (Android 13+): requested at launch; a banner links to
  settings if denied.
- `SCHEDULE_EXACT_ALARM` (Android 12+): checked before scheduling; a banner
  offers a settings redirect. Without it, reminders still fire but may be
  delayed by the system.
- `RECEIVE_BOOT_COMPLETED`: reschedule after reboot.

## Manual test checklist

### 1. Share from Instagram
1. Install `builds/reelreminder-debug.apk` (`adb install -r builds/reelreminder-debug.apk`).
2. Open Instagram → a reel → Share → **ReelReminder**.
3. The Schedule screen opens with the reel URL pre-filled.
4. Add a note, choose **Once**, pick a time ~2 minutes ahead, save.
5. Confirm the reminder appears on Home with the correct next time.
6. Lock the screen and wait. A high-importance notification appears with the
   note as title and the link as body.
7. Tap the notification → the reel opens in Instagram/browser.
8. Confirm the reminder is now marked **Completed**.

### 2. Once
- Schedule a once reminder in the past → validation blocks it ("Choose a future
  date and time").
- After it fires, it does not fire again.

### 3. Daily
1. Create a daily reminder a couple of minutes ahead.
2. Let it fire; tap **Done** to dismiss it for today (or leave it).
3. Confirm the next trigger moves to the same time tomorrow.
4. Temporarily change the device clock/timezone and confirm the reminder still
   has a sane next time (BootReceiver also reschedules on time changes).

### 4. Weekly
1. Create a weekly reminder with two or three weekdays selected.
2. Confirm the "Next reminder" preview shows the nearest selected day.
3. Save, then verify the next trigger lands on a selected weekday.
4. Deselect all days → validation shows "Choose at least one day".

### 5. Reboot
1. Create a daily or weekly reminder a few minutes ahead.
2. Reboot the device (or `adb reboot`).
3. Do not open the app. Wait for the trigger time.
4. The notification still appears, proving `BOOT_COMPLETED` rescheduling works.

### 6. Snooze 10 min
1. Let a reminder fire.
2. Tap **Snooze 10 min**.
3. The notification disappears and a toast confirms the snooze.
4. It reappears roughly 10 minutes later.
5. Snooze on a daily/weekly reminder does not disturb its regular schedule.

### 7. Done action
1. Let a reminder fire and tap **Done**.
2. The notification is dismissed and the reminder is marked completed/paused
   from further alarms.

### 8. Pause / resume, edit, delete
- Pause a reminder → status shows **Paused** and no alarm fires.
- Resume it → the next trigger is recalculated.
- Edit a reminder → changes are saved and the alarm is rescheduled.
- Delete → confirmation dialog, then the row and its alarm are removed.

### 9. Permissions
- Fresh install on Android 13+: the notification permission prompt appears.
- Deny it → the "Notifications are off" banner appears; **Enable** opens
  settings.
- On Android 12+, disable exact alarms in system settings → the "Exact alarms
  are off" banner appears; saving still works with a warning and reminders may
  be delayed.

### 10. Localization and theme
- Switch the system language to Hindi → all UI strings and notification text
  appear in Hindi.
- Toggle system dark mode → the app follows it with a Material 3 dark palette.
- Rotate the device → schedule form state survives configuration changes.

### 11. No network / privacy
- Enable airplane mode. All features above still work; no data leaves the
  device.

## Tests

```bash
./gradlew testDebugUnitTest
```

Covers URL extraction (Instagram share text, punctuation, multiple URLs) and
next-trigger math for once/daily/weekly schedules, including DST-safe
`java.time` calculations.
