# Presence

Presence is an offline focus-cycle timer and time tracker for Android. It is the time you give to one piece of work. Projects, tasks, and sessions stay on the phone. There is no account, cloud service, analytics, or Android cloud backup. The network permission is used only for local Wi-Fi/Ethernet sync.

Package: `app.presence`. Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).

## Install

1. Download `presence.apk` from the GitHub Release.
2. On the phone, allow install from your browser or file app.
3. Open the APK and install it.
4. The debug signature is the one this build uses, so a later install must be the same signature or the previous copy has to be removed first.

The APK is signed and can be installed without Play Store billing.

## Features

Everything in Presence is free. There is no premium tier and no tester switch.

- Work time split into equal stretches, with short and long breaks between them
- Play, pause, resume, stop, and skip, plus free tracking, count-up display, and optional auto-continue
- Projects, tasks, and tags. A project is a name, and it can be deleted
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

## Focus plans

Work time is one project duration. The default is one hour. Stretch count is the short-break count plus the long-break count plus one, and each stretch is that duration divided evenly. One hour with two short breaks is three 20-minute stretches. A different duration splits the same way.

Breaks start when a stretch ends. The next stretch waits until Play unless auto-continue is enabled. Stop ends the sitting, including one that is waiting after a break. Pauses are not work and are not written into stats. The ring shows progress through the current phase, with markers and a computed count for the sitting’s focus stretches. Countdown and count-up displays share the same deadline; free tracking has no automatic deadline.

Settings changes apply to the next sitting. An open sitting keeps its lengths and its project. Only focus and tracking count toward goals and statistics. A late alarm does not add the delay as work or as a pause.

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

## Layout and appearance

The interface responds to available width and height after system bars and cutouts. Portrait phones use bottom navigation; short landscape phones use a compact rail and scrollable single-pane forms. Wide tablets use a rail, project master-detail, and bounded chart/settings grids. Todos use one bounded list at every size. Drafts and navigation survive resize and rotation.

System insets are owned by the root; keyboard avoidance belongs to the content. Text preferences change typography rather than dp density and retain Android font scaling. Duration editors retain hours, minutes and seconds. Work stretch count is computed from the break counts and is never independently editable.

All eleven palettes remain available: Grove, Harbor, Dusk, Slate, Berry, Amber, Moss, Iris, Copper, Tide and Ink. Every palette supports Light, Dark and Follow system. Appearance, palettes and language remain device-local preferences.

Statistics use calendar days, Monday-first weeks, calendar months and years. The weekly overview has an explicit date label. Zero activity displays zero totals, no positive bars and an unfilled data ring. Manual sessions have real date/time pickers and reject future or overlapping work.

## Languages

Settings → Language. The first launch follows the phone when it is Turkish, Spanish, Portuguese, French, Japanese, or Korean. Otherwise it starts in English.

## License

Copyright 2026 Sumit Kumar.

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license in [LICENSE](LICENSE), or at http://www.apache.org/licenses/LICENSE-2.0.
