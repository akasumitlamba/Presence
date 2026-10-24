# Presence 2.2.0 UI implementation and verification

Implemented against `ec6cbb9c80561cd9f2ce8217636cf1ac7b9a3e70`, using the supplied Presence UI Redesign Handoff as the layout brief. Reference sample data was not added to production defaults.

## Changes

- Shared inset-aware shell, headers, flat cards, typography, session strip and scrolling cues. System bars and cutouts have one owner; keyboard avoidance is separate. Density stays unchanged when changing text size.
- Bottom navigation on compact portrait windows; 72dp rail on short landscape windows and 80dp rail on larger windows. Useful width and height determine pane count. The timer is bounded by both dimensions; long forms scroll.
- Timer progress arc, computed stretch markers, phase-specific controls, count-up display and free tracking. Notification and widget displays follow the count-up preference too. Existing engine accounting and open-sitting plans are retained.
- Project list/detail, name autosave, goals/deadlines, total work and break editors, computed stretch summary, tasks and tags. Removed destructive startup deletion of historical group records; no replacement group UI was invented.
- Todos use one bounded list with stable keys, wrapping titles, keyboard submission, completion and delete confirmation.
- Statistics use actual calendar days, Monday-based weeks, months and years. Navigation, chart labels, project legend, calendar and empty states reflect stored records. Manual-entry validation and sharing are preserved.
- Settings cards retain reminders, sounds/custom audio, backup/CSV import/export, local Wi-Fi sync and native storage actions. Pairing shows actual codes, expiry, status and device actions.
- Bounded scrolling dialogs retain native date/time pickers. Navigation, selected project, filters and drafts survive recreation. Stable composition across bottom-navigation/rail and single/detail pane transitions fixes draft loss during resize.

All 11 existing palette IDs remain available: Grove, Harbor, Dusk, Slate, Berry, Amber, Moss, Iris, Copper, Tide and Ink. Light, dark and follow-system modes remain. Grove dark uses the supplied colors; the other palettes remain distinct. All seven languages remain available, with new labels translated. App text sizes remain compact, standard and large and also respect Android font size.

## Changed source areas

`MainActivity.kt`, `AndroidManifest.xml`, `ui/Design.kt`, `ui/Screens.kt`, `ui/Theme.kt`, `ui/L10n.kt`, `data/Repo.kt`, `timer/Notifier.kt`, `widget/PresenceWidget.kt`, and the new core `Statistics.kt`/`StatisticsTest.kt`. Version is 2.2.0 (12). README and the download link are updated.

## Automated validation

With JDK 17 and Android SDK 35 configured:

```sh
bash ./gradlew :core:test :app:assembleDebug :app:lintDebug --console=plain
bash ./gradlew :app:assembleRelease --console=plain
```

Core: 41 tests, zero failures, including calendar/leap-month/year-crossing week and logical day-start checks. Debug assembly and debug lint passed (lint: zero errors, 32 warnings). Release assembly passed. The release APK certificate matches v2.1.1, allowing an in-place upgrade.

## Emulator validation

Native Android API 30, using real Compose screens and emulator-only database fixtures. These are screenshots of the app, not the DOCX references.

| Window (dp) | Pixel size / density | Checked |
| --- | --- | --- |
| Phone 360 × 800 | 720 × 1600 / 320 | Five main destinations, empty statistics |
| Phone 412 × 915 | 824 × 1830 / 320 | Five destinations, project detail, dialogs and pairing |
| Short landscape 800 × 360 | 1600 × 720 / 320 | Compact rail, timer, scrolling forms, active strip and keyboard |
| Tablet landscape 1024 × 640 | 1536 × 960 / 240 | Master/detail, timer columns, settings/chart grids and dialogs |
| Tablet portrait 800 × 1280 | 1200 × 1920 / 240 | All main destinations |
| Resized/split-sized window 700 × 800 | 1050 × 1200 / 240 | All main destinations |

All 22 light/dark palette combinations rendered; system day/night changes rendered. The release APK installed over the existing app; a palette selected through the actual settings picker persisted after process recreation. English and French were exercised. All three app text preferences were exercised with Android font scale 1.3. A todo draft survived native rotation with the keyboard open. A goal draft survived a single-pane to master/detail resize. Goal/manual-entry dialogs, project tags/tasks, lower settings/calendar regions and actual pairing-code generation/cancellation were exercised.

Actual timer controls exercised idle, focus, paused/resumed, short break, waiting, long break, stopped and tracking. The timer ran with Wi-Fi and mobile data disabled and recovered after Home, screen lock and return. Three-button navigation and a gesture-navigation/corner-cutout configuration were captured. No AndroidRuntime crash was observed during these checks.

Local evidence and APK are in `artifacts/ui-redesign/` (ignored generated files): portrait/landscape overview, window contact sheets, palette contact sheets, dialogs, keyboard/rotation and pairing screenshots. Top/lower captures are scroll positions of the same pages.

## Device checks still needed

Physical two-device LAN pairing/sync, OEM background restrictions/process eviction, Android 13+ notification permission denial, actual removable SD storage moves, custom audio/file picker round trips and a manual TalkBack audit were not verified in this emulator pass. Core regression tests cover engine/merge rules; emulator code generation is not evidence of a successful physical two-device sync.
