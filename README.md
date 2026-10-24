# Presence

Presence is an offline focus-cycle timer and time tracker for Android. It is the time you give to one piece of work. Projects, tasks, and sessions stay on the phone. There is no account, cloud service, analytics, or Android cloud backup. The network permission is used only for local Wi-Fi/Ethernet sync.

Package: `app.presence`. Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).

## Install

1. Download `presence.apk` from the GitHub Release.
2. On the phone, allow install from your browser or file app.
3. Open the APK and install it.
4. The debug signature is the one this build uses, so a later install must be the same signature or the previous copy has to be removed first.

The APK is signed and can be installed without Play Store billing.

For the 2.1.1 signing change and migration steps, read the [release notes](docs/releases/v2.1.1.md) before replacing an existing install.

## Features

Everything in Presence is free. There is no premium tier and no tester switch.

- Focus, short rest, and long rest, with lengths, cycle count, and count up or count down
- Pause, resume, stop, and skip
- Optional auto-continue
- One-tap tracking
- Projects, tasks, tags, and groups, including Work, Study, and Personal
- A todo list: add, complete, and delete
- A project goal and the daily target that shrinks as you log time
- Today, week, month, and year, with a bar chart, a pie chart, and a month calendar
- Sharing a stats summary and adding a past stretch by hand
- The minimized timer bar
- The ongoing notification with the time left
- A daily reminder and a streak reminder, each with a time you set
- The chime when a stretch ends
- Rain, brook, and hearth, plus bell, pulse, and an alarm sound copied from the phone
- Lengths saved on one project
- The timer keeps going in the background, on the lock screen, and after the process is killed
- Follow system, light, or dark, plus text size and the hour the day starts
- English, Turkish, Spanish, Portuguese, French, Japanese, and Korean
- Wi-Fi pairing and sync
- Phone layout and the wider layout
- Home-screen widgets: session (start or pause), stats, streak, and todos
- A backup file of projects, tasks, and sessions, a CSV of sessions, and bringing a backup back in

Source Sans 3 is bundled for the app and the widgets. Its license is in `third_party/source-sans-3/LICENSE.md`.

## Focus plans and tracking

Work time is the total focus budget for one plan, not a project estimate or one interval. The plan has one more focus stretch than the number of breaks; break time is added separately. For example, 60 minutes and two short breaks gives three 20-minute focus stretches.

Use **4 × 25 min · 5 min breaks** in global or project timer settings for a familiar Pomodoro plan: four 25-minute focus stretches and three 5-minute breaks. The plan finishes after the fourth stretch. Custom long breaks are distributed within a plan.

Breaks start when a focus stretch ends. The next focus stretch waits for Play unless **Continue on its own** is enabled. Skip moves past the current phase while keeping only actual elapsed work. Stop cancels the whole plan, including a plan waiting after a break. Track time is a stopwatch with no automatic breaks. Count up changes the display, not the focus deadline.

Settings changes apply to the next plan; an active plan retains its lengths and selected project/task. Only focus and tracking count toward goals and statistics; rests and pauses do not. Delayed alarms never count extra focus time.

Tap a task title in project details to rename it or assign tags. Groups can be selected or created in project details.

## Pair two phones

Both phones need Presence and the same Wi-Fi. Mobile data is not used. On Android 13 and later, showing or entering a code requests nearby-Wi-Fi access. Presence does not request location access.

1. On one phone, open Settings → Pair on Wi-Fi → **Show code**.
2. On the other, choose **Enter code** and type the four digits.
3. The code lasts 10 minutes, or until the pair succeeds. A wrong code and an expired code each get a plain explanation.
4. After that, the phones remember each other and sync on the same Wi-Fi without a new code while Presence is running. Discovery and a periodic pass pick up changes automatically. **Sync now** runs a pass immediately.

What syncs: groups, tags, projects, tasks, completed sessions, goals, and timer settings. What stays on each phone: appearance, text size, language, reminders, todos, and a custom alarm file.

If the same record changes on both phones, the later edit wins. If a timer is running, that open session is not overwritten and it is not counted twice. Incoming timer settings are saved for the next plan without changing an active timer. Discovery advertises that pairing is available, never the PIN itself. Sync sockets bind to Wi-Fi/Ethernet and reject public peer addresses.

## Background

The phase keeps going when you leave Presence, lock the screen, or the process is killed. The notification shows the time left. When the length is reached, the alarm plays and a notification appears even if Presence is not in front. A paused phase stays paused with the same time left.

## Build

Install JDK 17 or newer and Android SDK 35 (platform and build-tools 35). Point `ANDROID_HOME` at the SDK, or put `sdk.dir` in `local.properties`.

```bash
./gradlew :core:test
./gradlew assembleDebug
```

The installable APK is `app/build/outputs/apk/debug/app-debug.apk`. `assembleRelease` uses the same debug signature so it is installable too.

## Layout

A narrow window keeps a bottom bar and one column. A landscape phone, a tablet, or any other wide window uses a navigation rail, a project list beside its detail, the timer ring beside its controls, and stats in a grid.

## Languages

Settings → Language. The first launch follows the phone when it is Turkish, Spanish, Portuguese, French, Japanese, or Korean. Otherwise it starts in English.

## License

Copyright 2026 Sumit Kumar.

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license in [LICENSE](LICENSE), or at http://www.apache.org/licenses/LICENSE-2.0.
