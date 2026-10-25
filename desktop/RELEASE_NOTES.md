First desktop preview of Presence for Windows and Linux.

- Windows .exe installer, Debian/Ubuntu .deb package, and Linux .flatpak bundle; Java is included.
- Android's eleven light/dark palettes and Source Sans 3 typography, with desktop navigation and resizable layouts.
- Focus plans, free tracking, pause/resume/stop/skip, projects, tasks, goals/deadlines, local todos, statistics, CSV export and JSON backup import/export.
- Local Wi-Fi pairing and automatic sync using the same Kotlin protocol and merge engine as Android 2.2.1. DNS-SD discovery supports Wi-Fi and Ethernet. Appearance and todos stay local; active sessions are protected.

Install one package for your platform. For Flatpak use `flatpak install --user ./Presence-2.2.1-linux-x86_64.flatpak`.

This is a prerelease. Automated tests cover pairing, bidirectional sync, deletion, timer protection and persistence. Physical Android/Windows/Linux Wi-Fi interoperability still needs device testing. Desktop currently uses English and a system completion beep; Android widgets, reminders and custom ambient audio are not included. Presence must remain open for automatic sync and completion alerts; timer state recovers after reopening.

On Windows allow Presence on private networks if prompted. Discovery uses UDP 5353, and sync uses a dynamically allocated TCP port. Both devices must be on the same LAN; guest-network isolation can prevent discovery. Desktop-to-desktop pairing also supports entering the displayed IPv4 address and port.

Installers are unsigned. Verify downloads against SHA256SUMS.
