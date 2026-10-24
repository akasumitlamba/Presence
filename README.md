# Presence

Presence is a focus-cycle timer and time tracker for Android. It is the time you give to one piece of work. Projects, tasks, and sessions stay on the phone. There is no account.

Package: `app.presence`. Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).

## Install

1. Download `presence.apk` from the GitHub Release.
2. On the phone, allow install from your browser or file app.
3. Open the APK and install it.
4. The debug signature is the one this build uses, so a later install must be the same signature or the previous copy has to be removed first.

The APK is signed and can be installed without Play Store billing.

## Try free and premium

Settings opens with **Tester switch**. It turns premium on or off so both modes can be tried. It does not buy anything and it does not contact a store.

### Free

- Focus, short rest, and long rest, with lengths, cycle count, and count up or count down
- Pause, resume, stop, and skip
- Optional auto-continue
- One-tap tracking
- Up to 3 projects and 8 tasks on each
- Work, Study, and Personal, including rename
- Today’s total and today’s time per project
- The minimized timer bar
- The ongoing notification with the time left
- The chime when a stretch ends
- The timer keeps going in the background, on the lock screen, and after the process is killed
- Light, dark, and system appearance, text size, and the hour the day starts
- English, Turkish, Spanish, Portuguese, French, Japanese, and Korean
- Wi-Fi pairing and sync
- Phone layout and the wider layout
- A home-screen widget that shows today’s minutes and opens Presence

### Premium, with the tester switch

- Projects beyond 3, and tasks beyond 8
- Tags and filtering
- More groups
- A project goal and the daily target that shrinks as you log time
- Week, month, and year ranges
- Bar chart, pie chart, and month calendar
- Sharing a stats summary
- Adding a past stretch by hand
- Rain, brook, and hearth
- Bell, pulse, and an alarm sound copied from the phone
- Lengths saved on one project
- A widget that shows the live phase and can pause it
- A backup file of projects, tasks, and sessions, a CSV of sessions, and bringing a backup back in

Turning the switch off keeps the data and locks the extra tools. The core timer still runs.

## Pair two phones

Both phones need Presence and the same Wi-Fi. Mobile data is not used. The first time you show or enter a code, Android asks for nearby-Wi-Fi access so the phones can find each other. That prompt does not turn on location.

1. On one phone, open Settings → Pair on Wi-Fi → **Show code**.
2. On the other, choose **Enter code** and type the four digits.
3. The code lasts 10 minutes, or until the pair succeeds. A wrong code and an expired code each get a plain explanation.
4. After that, the phones remember each other and sync on the same Wi-Fi without a new code. **Sync now** runs a pass immediately.

What syncs: groups, tags, projects, tasks, sessions, goals, and the timer lengths. What stays on each phone: the tester switch, appearance, text size, language, and a custom alarm file.

If the same record changes on both phones, the later edit wins. If a timer is running, that open session is not overwritten and it is not counted twice. Length changes wait until the phase is over.

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
