Presence Desktop 2.2.1 for Windows and Linux. Every feature is free; there are no paid plans.

- Balanced task choices and period selectors, full-width project creation, equal Stats columns/actions, and tag scrolling buttons shared with Android.
- Rounded icons with an alpha-only mask to remove white edge fringes.
- Windows `.exe` installer, Debian/Ubuntu `.deb`, and Linux `.flatpak` bundle, with Java included.
- UI and timer controls built directly from Android's source, including its eleven palettes, seven languages, text sizes and introduction.
- Projects, ordering, tasks, tags and filters, goals/deadlines and daily targets; local todos.
- Focus timers and time tracking, per-project work/break settings, pause/resume/stop/skip, count-up and automatic continuation.
- Calendar statistics, day/week/month/year periods, charts, streaks, validated manual entries and sharing.
- Same alarm tones, rain/brook/hearth ambience, volume control and custom audio.
- Daily and streak reminders, close-to-tray background operation and four floating desktop widgets.
- Android-compatible Wi-Fi pairing and automatic sync; JSON import/export, CSV export and optional storage on a mounted removable drive.

Install one package for your platform. For Flatpak:

`flatpak install --user ./Presence-2.2.1-linux-x86_64.flatpak`

Connect devices to the same local network. Allow Presence on private networks in Windows Firewall if prompted. Discovery uses UDP 5353 and sync uses a dynamically allocated TCP port. Guest-network isolation can block communication.

Closing to the tray keeps timers, reminders and sync running. Explicitly quitting stops background activity until reopening; saved timers recover. Home-screen widgets are provided as floating desktop windows, and platform file/date/share dialogs use desktop equivalents.

This first desktop release is a prerelease. Automated protocol/storage/timer/audio tests, local mDNS pairing and packaged launch checks cover the implementation. Physical Android-to-PC Wi-Fi, suspend/resume and hardware audio/notification testing remain to be completed. Installers are unsigned; checksums are in `SHA256SUMS`.
