# Presence Desktop

Desktop module in the same repository as Android. Kotlin/JVM and Compose Desktop reuse `:core` for timing, statistics, backup format, pairing and merge rules. `Theme.kt` is adapted from Android's palette and typography source; preserve parity when changing themes.

Design: the Android Grove palette (#0E5C42 primary, #F3F6F4 background, #FFFFFF surface, #10241C text, #C9EBDD selection), Source Sans 3, a left navigation rail, centered timer ring, and left-aligned project/statistics views. All eleven original palettes are included. Desktop controls use the same Material shapes and sizing system; wide windows provide reading space without stretching the timer.

## Build

Use JDK 17. Android SDK is unnecessary with `-PdesktopOnly`.

```sh
./gradlew -PdesktopOnly :core:test :desktop:test
./gradlew -PdesktopOnly :desktop:run
./gradlew -PdesktopOnly :desktop:packageDeb :desktop:createDistributable
# On Windows with JDK 17 and WiX 3 installed:
./gradlew -PdesktopOnly :desktop:packageExe
```

Compose uses jpackage; build Windows installers on Windows and Debian packages on Linux. Outputs are in `desktop/build/compose/binaries/main/`. The Desktop release workflow builds both platforms and Flatpak, tests the Linux launcher, and publishes assets with checksums for `desktop-v*` tags. Manual workflow runs produce downloadable artifacts without publishing.

## Flatpak

```sh
flatpak remote-add --user --if-not-exists flathub https://flathub.org/repo/flathub.flatpakrepo
flatpak install --user flathub org.freedesktop.Platform//25.08 org.freedesktop.Sdk//25.08
./gradlew -PdesktopOnly :desktop:createDistributable
flatpak-builder --user --force-clean --disable-rofiles-fuse --repo=flatpak-repo flatpak-build desktop/packaging/app.presence.Presence.yml
flatpak build-bundle flatpak-repo Presence-2.2.1-linux-x86_64.flatpak app.presence.Presence
```

Flatpak has LAN access for mDNS and TCP sync, display/GPU and sound access. Data stays in its private XDG data directory. Backups use the native file chooser. The Documents folder is accessible for backup import/export; other host folders stay private.

## Data and sync

Records persist using atomic replacement in `$XDG_DATA_HOME/presence` (default `~/.local/share/presence`), `%APPDATA%/presence` on Windows, or Flatpak's private XDG directory. Keep the entire data file private: it contains pairing tokens. A process lock prevents simultaneous instances. A corrupt data file is reported and never silently reset.

Desktop advertises `_presence._tcp.local.` with Android-compatible `id`, `nm`, and optional `code=offered` TXT fields. The secret four-digit code is never advertised. Pairing codes are valid for ten minutes, then retire. Paired peers exchange records every thirty seconds while the app is open. Completed sessions, projects, tasks, tags and timer preferences sync. Local settings and todos do not. Network traffic uses the existing Android LAN protocol; this is intended for trusted local networks.

Automated tests use the actual `Node` server/client with Android's in-memory Catalog and a persistent desktop Catalog. Physical device testing remains necessary for firewall, router, interface and suspend behavior.

See [release notes](RELEASE_NOTES.md) for preview limitations. Font licenses remain in `third_party/source-sans-3`.
