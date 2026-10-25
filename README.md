# Presence

<img src="assets/presence.png" width="88" height="88" alt="Presence app icon">

**Make time for one thing.** Presence is an offline focus timer and time tracker for Android. Plan a project, work in focused stretches, and take proper breaks. Your projects and sessions stay on your devices.

[Website](https://akasumitlamba.github.io/Presence/) · [Download the Android APK](https://github.com/akasumitlamba/Presence/releases/latest/download/presence.apk) · [Release notes](https://github.com/akasumitlamba/Presence/releases/latest)

Android **8.0+** · Package `app.presence` · Free and open source

## What’s inside

- Focus cycles and free time tracking, with Continue, Pause, Stop, and Skip controls.
- Total work time divided into equal stretches, configurable short/long breaks, countdown or count-up display, and optional automatic continuation.
- Projects, tasks, tags, goals and deadlines, plus an independent todo list.
- Calendar-based statistics, project charts, streaks, manual entries, and sharing.
- Background timing, notifications, alarms, reminders, and ambient/custom sounds.
- Four home-screen widgets: timer, statistics, streak, and todos.
- Eleven palettes, light/dark/system appearance, adjustable text, and seven languages. Widgets follow your appearance settings.
- Local Wi-Fi pairing and sync, JSON backup import/export, CSV session export, and optional removable-card database storage.

There are no accounts, subscriptions, analytics, or cloud services. Android cloud backup is disabled. Network access is used for local-device sync.

## Install and start

1. Download `presence.apk` from the [latest release](https://github.com/akasumitlamba/Presence/releases/latest).
2. Open it on your Android device. If Android asks, allow installation from the browser or file app you’re using.
3. Open Presence and follow the introduction. It is also available in **Settings → About → How to use Presence**.
4. Create a project, select it on Timer, and choose **Continue**.

Install updates over the existing app using the same signing certificate to preserve local data. Releases currently use the project’s existing Android debug signing certificate; every feature is available without Play Store billing. Release assets include `SHA256SUMS` for verification.

### Focus plans

Set the total work duration and the number of short and long breaks. The number of focus stretches is the number of breaks plus one. For example, 60 minutes of work with two short breaks produces three 20-minute focus stretches. Break time is additional to that work duration.

**Continue** starts or resumes; **Pause** temporarily pauses; **Stop** saves elapsed work and ends the session; **Skip** moves to the next phase. Work, pauses, and breaks are accounted for separately. Open sessions retain the plan they started with. Only focus and tracking time count toward statistics and goals.

The thick ring shows the current phase, and the curved segments beneath it show your focus stretches. Portrait windows use centered bottom navigation; landscape windows use a centered side rail.

### Pair your devices

1. Connect both devices to the same local Wi-Fi network.
2. On one device, open **Settings → Pair on Wi-Fi → Show code**.
3. On the other, enter that four-digit code. It expires after ten minutes or when pairing succeeds.

Internet access is not required. Android 13+ may ask for nearby Wi-Fi permission. Presence does not request location access.

Projects, tasks, tags, completed sessions, goals, and timer settings sync. Appearance, language, reminders, todos, and custom audio remain local to each device. Active timers are not overwritten by sync.

## Build the Android app

Use **JDK 17** and **Android SDK 35**, including build-tools 35. Set `ANDROID_HOME` to the SDK, or set `sdk.dir` in a local `local.properties` file.

```sh
./gradlew :core:test :app:assembleDebug :app:lintDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

```sh
./gradlew :app:assembleRelease
```

Release APK: `app/build/outputs/apk/release/app-release.apk`. The version is defined in [app/build.gradle.kts](app/build.gradle.kts).

## Website and GitHub Pages

The one-page site is served **directly from the repository root**. It uses plain HTML, CSS, and JavaScript, with no build step, framework, third-party scripts, or analytics.

| File | Purpose |
| --- | --- |
| `index.html` | Accessible page content, download links, installation FAQ |
| `styles.css` | Responsive layouts and Presence-inspired visual system |
| `script.js` | Timer demo and palette/appearance previews |
| `assets/` | Local app icon and bundled fonts with their license |
| `.nojekyll` | Keeps GitHub Pages serving these static files directly |

In repository **Settings → Pages**, the publishing source is **Deploy from a branch**, branch **main**, folder **/ (root)**. The public address is [akasumitlamba.github.io/Presence](https://akasumitlamba.github.io/Presence/).

Preview locally from this directory:

```sh
python3 -m http.server 8000
```

Then open `http://localhost:8000/`. All site asset URLs are relative so they also work under GitHub Pages’ `/Presence/` path.

The browser timer is an explicitly labeled **20-second demonstration**, not a recorder for your Android sessions. It does not save or transmit work. The website can remember palette/appearance preferences in local browser storage; it still works when storage is blocked. Downloads, navigation, and the FAQ work without JavaScript.

See [website design and verification](docs/website.md) and [Android UI verification](docs/ui-follow-up.md).

## Repository structure

- `app/` — native Android application, Compose UI, storage, timer runtime, sync, and widgets.
- `core/` — timer rules, statistics, sync/merge logic, and core tests.
- `docs/` — implementation and validation notes.
- Root HTML/CSS/JS and `assets/` — public GitHub Pages website.

## License

Copyright 2026 Sumit Kumar. App and website code are licensed under [Apache License 2.0](LICENSE).

The application bundles Source Sans 3 by Adobe under the SIL Open Font License. The website uses renamed, subsetted copies of that typeface. See [font credits](assets/fonts/README.md), [website font license](assets/fonts/LICENSE.md), and [original font license](third_party/source-sans-3/LICENSE.md).
