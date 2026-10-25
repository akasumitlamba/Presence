# Presence

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="128" height="128" alt="Presence app icon">

Presence is a focus timer for Android. It is the time you give to one piece of work. Projects, tasks, and sessions stay on the phone. There is no account.

[Download Presence](https://github.com/akasumitlamba/Presence/releases/download/v2.2.1/presence.apk)

The current release is version 2.2.1, versionCode 14. Package name: `app.presence`.

## Install

1. Download the APK from the link above.
2. On the phone, allow the browser or the file app to install it.
3. Open the file and install Presence.

A later copy installs over this one when it is signed with the same key. The app does not use Continue Store billing. Every feature is free.

## Timer

Work time is one duration for a project. The default is one hour. That duration is split into equal stretches. Stretch count is the short-break count plus the long-break count plus one. One hour with two short breaks is three stretches of twenty minutes. Any other duration splits the same way.

Continue starts a sitting. When a stretch ends, the next break starts by itself. When the break ends, the next stretch waits until you press Continue. After the last stretch, the sitting is idle. Stop ends the sitting, including one that is waiting after a break.

Pause is on the timer. Time spent paused is not work and is not a break. It is not written into stats. While a sitting is open, the bottom of the timer shows how many pauses happened and how long they added up to. That note disappears when the sitting ends.

The main ring shows the current phase. A separate segmented arc follows its lower edge, with one segment per computed focus stretch. The center shows the time left in the current phase. Under that, smaller, is the time already spent working on this project.

The project control is one dropdown. It lists the projects you already have, and the last entry is New project. A project is a name. You can delete it. Projects are not grouped.

Duration fields are hours, minutes, and seconds. Work time, short breaks, and long breaks sit in their own sections. Edits are saved as you make them.

## Also on the phone

Appearance can follow the system, or stay light or dark. The color themes are Grove, Harbor, Dusk, Slate, Berry, Amber, Moss, Iris, Copper, Tide, and Ink. Text size is a setting, and so is the hour the day starts.

Todos can be added, completed, and deleted.

A daily reminder and a streak reminder each have a time you set.

Home-screen widgets cover the session, stats, the streak, and todos. The session widget can continue or pause; when idle, it opens the timer to choose a project. All four widgets follow the app’s palette and light/dark/system mode.

The timer keeps going when you leave Presence, lock the screen, or the process is killed. The notification shows the time left. When a phase ends, the alarm plays even if Presence is not in front.

Stats cover today, the week, the month, and the year, with a bar chart, a pie chart, and a month calendar. You can share a summary and add a past stretch by hand.

Settings has a row named SD card. If the phone has a removable card, Presence can keep its database in the app’s own folder on that card. It does not ask for access to every file on the card.

Languages are English, Turkish, Spanish, Portuguese, French, Japanese, and Korean. The first launch follows the phone when that language is one of these. Otherwise it starts in English.

Every portrait window uses a centered bottom navigation bar. Landscape windows use a vertically centered side rail. Wider windows can place the timer beside its controls and a project list beside its detail. The timer project name is prominent, with an adjacent dropdown arrow; Logged, Pauses and Streak have equally sized, centered columns. Statistics charts appear directly below the period controls.

Background sounds are rain, brook, and hearth. The alarm can be a bell, a pulse, or a sound copied from the phone.

## On the device

There is nothing to sign in to, and there is no cloud account. Android cloud backup is off. You can save a backup of projects, tasks, and sessions, save a CSV of sessions, and bring a backup back in.

Two phones on the same Wi-Fi can pair. One shows a four-digit code. The other enters it. The code lasts ten minutes, or until the pair succeeds. After that, the phones remember each other and sync while Presence is running. No server is involved.

What syncs: projects, tasks, tags, completed sessions, goals, and timer settings. What stays on each phone: appearance, text size, language, reminders, todos, and a custom alarm file. If the same record changes on both phones, the later edit wins. A running timer is not overwritten and is not counted twice.

## Build

Install JDK 17 or newer and Android SDK 35, including the platform and build-tools 35. Point `ANDROID_HOME` at the SDK, or put `sdk.dir` in `local.properties`.

```bash
./gradlew :core:test
./gradlew assembleDebug
```

The APK is `app/build/outputs/apk/debug/app-debug.apk`.

Source Sans 3 is bundled for the app and the widgets. Its license is in `third_party/source-sans-3/LICENSE.md`.

## License

Copyright 2026 Sumit Kumar.

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE), or http://www.apache.org/licenses/LICENSE-2.0.

The app retains all 11 palettes. A first-launch guide can be reopened in Settings > About, and startup uses the app logo. Streak counters are visible in Timer and Stats.
