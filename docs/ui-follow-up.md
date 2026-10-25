# Presence 2.2.1 usability follow-up

The timer now uses a thicker 12dp ring, larger fitted digits and concentric stretch arc segments along its lower edge. The active controls are one row: Pause/Continue, Stop and Skip. Idle and paused actions say Continue throughout the app, notifications and timer widget. Tracking retains its valid controls; Skip is shown only for timed stretches.

Streak counters are visible in Timer and Stats and use the same saved-time/day-start calculation as the streak widget. Section-card headings have subtle rules. Project/default editors explain automatic saving. Empty timer controls explain the missing project and can open project creation. Timer failures show an inline message, shared primary/secondary buttons report failed actions, missing SD storage gets a visible banner, and startup failures offer retry instead of an indefinite loading screen. Existing pairing, file and manual-entry validation remains.

First launch shows a local, persistent introduction explaining projects, work/break plans, actions, streaks and offline sync. It can be reopened in Settings > About. A native logo launch screen covers startup; Android 12+ splash attributes are included. No artificial startup delay is added.

All four widgets now resolve the app's actual shared color scheme. Rounded backgrounds, icons, text, timer numbers and the action button follow all 11 palettes and light/dark/system preferences. Appearance changes refresh widgets; system configuration changes refresh resident widgets. The idle widget opens the timer to select a project rather than silently starting unassigned work.

## Validation

```sh
bash ./gradlew :core:test :app:assembleDebug :app:lintDebug :app:assembleRelease --console=plain
```

41 core tests passed with zero failures. Debug/release assembly passed, and debug lint passed with zero errors (32 warnings). Native Android 10 emulator checks cover first-launch guidance, completion across process recreation, empty-project guidance, focus/pause/continue/skip/break/stop, portrait/short landscape, tablet settings and French with Android font scale 1.3 plus large app text. Native RemoteViews hosting exercised all four widgets in light/dark Grove, light/dark Harbor, dark Berry and system day/night switching. Widget button contrast and the Todos icon tint were corrected after screenshot inspection. The temporary widget QA host is not included in the release or repository.

Actual local screenshots are in `artifacts/ui-redesign/2.2.1/` (ignored artifacts). Fixture projects and sessions exist only in the emulator, not production defaults. The final release APK also passed a fresh Android 10 install and persisted introduction completion after process recreation. Splash behavior on Android 12+, physical launcher-specific resizing, injected storage-failure recovery and TalkBack remain unverified in this pass. Two-device LAN testing is unchanged from the previous report.

## Build 14 refinement

Portrait windows, including tablets, use a horizontally centered bottom navigation group capped at 640dp. Landscape windows use a vertically centered side rail that scrolls if its destinations exceed the available height. Orientation is resolved from the current safe window dimensions. The straight stretch indicators are replaced with a 120-degree segmented arc concentric with the lower part of the timer ring. Segment count, completion and current-position colors still come from the existing timer state; all palettes remain supported.

The timer project selector now uses a prominent centered project name with its dropdown arrow adjacent. Logged, Pauses and Streak use equally sized, centered columns. Stats charts appear immediately after the period/date controls, before streak, actions and summary cards.
