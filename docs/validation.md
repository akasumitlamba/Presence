# Offline reliability and timer improvements

## Behavior

- All application data remains local. Android cloud backup/device transfer are explicitly excluded. Manual backup export/import remains available.
- Sync uses sockets bound to Wi-Fi/Ethernet, private/link-local peer addresses, and remembered peer tokens. Discovery advertises pairing availability instead of exposing the four-digit PIN. Automatic sync runs on discovery and every 30 seconds while the application process is alive.
- Device appearance, reminders, sounds and todos stay local. Only completed sessions and shared timer settings travel between phones. Active timer plans retain their configuration while incoming settings are saved for the next plan.
- Simultaneous sync exchanges no longer hold a lock while waiting for another device. Malformed messages fail without applying partial records. An unreachable advertisement does not prevent trying another pairing offer.
- Startup preserves project groups. Project details support group selection/creation, task renaming, and task tag assignment.
- Timer controls include stopwatch tracking, skip, count up and automatic continuation. Stop cancels a waiting plan. Break continuation preserves project/task attribution. A one-tap preset creates four 25-minute focus stretches with three 5-minute rests.
- Late alarms cannot inflate focus totals. The final focus stretch produces a completion notification. Stopped timers remove their foreground notification. Background sound changes apply immediately; generated ambient sounds no longer have a large DC offset.
- Manual entries have a start-time picker, respect the logical day boundary, and reject future time and overlapping logged work. Calendar focus markers exclude rests.
- Timer progress has a lighter unfinished ring and a phase counter. Duration fields share available width. Pairing uses a numeric keyboard and requires four digits. The initial timer offers a visible New project action.
- File operations run away from the UI thread and report inaccessible files. Custom sound replacement uses a temporary file to preserve the previous sound if copying fails.

## Automated validation

JDK 17, Android SDK platform 35, Gradle 8.11.1:

```sh
./gradlew :core:test :app:assembleDebug :app:lintDebug
./gradlew :core:test :app:assembleRelease :app:lintDebug
```

38 core tests pass: 27 existing tests and 11 regression tests. Regression coverage includes late completion with pauses, automatic continuation, stopping while waiting, count-up semantics, sync payload boundaries, address restrictions, simultaneous socket exchanges, malformed records, ambient sound balance, and manual overlap checks.

Android lint passes with zero errors. Existing warnings include dependency update notices, older-API resource attributes, unused resources and launcher icon shape suggestions.

## Emulator validation

Android 11 emulator, tablet layout and a 1080 × 1920 phone viewport:

- Fresh install, project creation and navigation.
- Focus pause/resume with a visible progress ring and foreground notification.
- A shortened test plan (15-second focus / 3-second rest / 15-second focus) continued in the background and reached the waiting state with exactly 15 seconds logged.
- Stop cancelled the waiting plan; stopwatch start/pause/stop returned to idle.
- Project group selection, task creation/tag assignment, and the Pomodoro preset. A database check verified all five preset fields, the selected Study group, preserved default groups, task tag persistence, and exactly 15,000 ms of focus with pauses excluded.
- No AndroidRuntime crash entries during these checks.

The short durations were emulator-only test fixtures; production defaults are unchanged.

## Remaining device validation

Physical two-phone mDNS discovery, Wi-Fi reconnection and manufacturer-specific battery restrictions have not been verified here. Socket pairing and bidirectional/simultaneous sync are covered by loopback integration tests. LAN transport retains the existing unencrypted protocol; pair on a trusted local network. This APK uses the local debug signing key and may require a backup/matching signature to replace an APK built elsewhere.

## Design references

The preset and separation between timed focus and open-ended tracking were informed by [PomodoroFocus](https://www.pomodorofocus.com/) and [Forest](https://www.forestapp.cc/). Android network discovery and permission behavior were checked against [Android's NSD guide](https://developer.android.com/develop/connectivity/wifi/use-nsd) and [local network permission documentation](https://developer.android.com/privacy-and-security/local-network-permission).
