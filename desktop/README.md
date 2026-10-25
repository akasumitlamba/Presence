# Presence Desktop

Windows and Linux versions of the Android app, in the same repository. Every feature is free.

## Android parity

The build adapts the **actual Android source**, rather than maintaining separate desktop screens:

- `Screens.kt`: timer, projects, tasks, tags, goals, todos, statistics, calendar, manual entries, sharing, settings and Wi-Fi pairing.
- `Design.kt`, `Theme.kt`: the same Material controls, eleven palettes, light/dark/system appearance, spacing, typography and text sizes.
- `L10n.kt`, `Introduction.kt`: the same seven languages and introduction.
- `timer/Center.kt`: the same timer commands, plans, pause accounting, transitions and active-plan protection.
- `:core`: identical timing, statistics, backup records, LAN protocol and merge logic.
- Fonts and icon are loaded from Android/repository assets at build time.

`tools/adapt_android_ui.py` only replaces platform integrations: Android permissions, file/share pickers, date/time dialogs, notifications and storage access. Its source anchors fail the build if an Android integration changes unexpectedly. Generated Kotlin is under `desktop/build/generated/androidUi` and is not committed.

Desktop equivalents of Android system features:

| Android | Desktop |
| --- | --- |
| Foreground timer service | Independent background timer worker; close to system tray |
| Notifications and reminders | Tray notifications; daily and streak reminders |
| Four home-screen widgets | Four floating widgets from Settings → Storage or tray → Widgets |
| Android document/share chooser | Desktop file chooser; copy or save shared summaries |
| Removable-card database | Choose an empty data folder on a mounted removable drive |
| AudioTrack and MediaPlayer | Same generated PCM tones and ambient loops via Java Sound; custom audio decoders |

Quitting the app explicitly stops background work until it is opened again. Timers recover their saved state after reopening. A system tray is required for closing the main window while leaving the app running; floating widgets can also be opened from Settings. Hardware sleep/wake, notifications and router/firewall behavior still require testing on physical devices.

## Build

Use JDK 17 and Python 3. No Android SDK is needed with `-PdesktopOnly`. Python is a build dependency; installed apps bundle Java and do not require Python or Java to be installed separately.

```sh
./gradlew -PdesktopOnly :core:test :desktop:test
./gradlew -PdesktopOnly :desktop:run
./gradlew -PdesktopOnly :desktop:packageDeb :desktop:createDistributable
# On Windows with WiX 3 installed:
./gradlew -PdesktopOnly :desktop:packageExe
```

Build Windows installers on Windows and Debian packages on Linux. Outputs are under `desktop/build/compose/binaries/main/`.

## Flatpak

```sh
flatpak remote-add --user --if-not-exists flathub https://flathub.org/repo/flathub.flatpakrepo
flatpak install --user flathub org.freedesktop.Platform//25.08 org.freedesktop.Sdk//25.08
./gradlew -PdesktopOnly :desktop:createDistributable
flatpak-builder --user --force-clean --disable-rofiles-fuse --repo=flatpak-repo flatpak-build desktop/packaging/app.presence.Presence.yml
flatpak build-bundle flatpak-repo Presence-2.2.1-linux-x86_64.flatpak app.presence.Presence
```

The manifest composes metadata with `appstreamcli`, which the supported SDK provides. Flatpak has LAN access for Wi-Fi sync, display/GPU/sound access, and home/mounted-drive access for native file selection, backup and removable storage. App data defaults to its private XDG data directory.

## Data and sync

Data persists by atomic replacement in `$XDG_DATA_HOME/presence` (default `~/.local/share/presence`) or `%APPDATA%/presence` on Windows. A process lock prevents concurrent instances. A corrupt file is reported rather than reset. Storage relocation verifies the new copy before switching and retains the previous file for recovery. Reconnect a selected removable drive before reopening the app.

Desktop advertises `_presence._tcp.local.` with the same `id`, `nm`, and optional `code=offered` TXT records as Android. The secret code is never advertised. Codes expire after ten minutes or successful pairing. Paired devices sync every thirty seconds while Presence is running. Local appearance, language, reminders, todos and audio stay local; active sessions are protected. JSON backups use the Android format. The existing protocol is intended for trusted local networks.

The Desktop release workflow tests the shared core and desktop storage, builds both installers, launches the packaged apps, and builds Flatpak. Manual workflow runs save artifacts; `desktop-v*` tags publish a prerelease after successful builds. Checksums accompany releases.
