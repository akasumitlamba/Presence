# Material You and Material 3 Expressive

This is a visual and interaction update. Existing screens, timer behavior, projects,
tasks, statistics, sync, all eleven palettes, and the original Presence mark remain.
The original transparent mark is tinted with the selected primary color in the apps
and website; launcher/tray asset silhouettes are unchanged.

Android adds **System colors**: wallpaper-derived Material colors on Android 12+,
with Grove fallback on older versions. Explicit palettes remain selectable and
existing saved selections are preserved. Widgets use matching colors and 8dp
transparent outer insets, separate from their internal content padding. In system
appearance, Android 12+ widgets retain resource color references so launcher
reinflation picks up wallpaper and night-mode changes. Explicit app light/dark
preferences are resolved with the same color-scheme function as the app.

The shared Compose UI uses MaterialExpressiveTheme and MotionScheme.expressive.
Spatial press feedback uses the scheme's spring; color changes use its effects
specification, which avoids overshooting colors. Native expressive buttons morph
on press. Compose respects Android's system animator duration scale. Website
motion is disabled under prefers-reduced-motion, and keyboard focus stays visible.

Tablet navigation uses a rail at 600dp; expanded content uses two panes at 840dp.
Short windows keep scrollable single-pane content. Tall Android tablet windows stack the timer above its controls, while wide windows keep the side-by-side arrangement. Window size, rather than a
fixed device identity or orientation lock, controls layout. The desktop remains
frameless with its existing integrated window controls and no timer fullscreen
button. Android retains its existing immersive timer control.

## Sources checked on 2026-10-26

- [Material motion](https://m3.material.io/styles/motion): spatial and effects motion.
- [Material 3 for Compose](https://developer.android.com/develop/ui/compose/designsystems/material3): dynamic colors and component theming.
- [Widget enhancements](https://developer.android.com/develop/ui/views/appwidgets/enhance): dynamic resources and background transition identity.
- [Android 17 resizability](https://developer.android.com/about/versions/17/changes/ff-restrictions-ignored): adaptive large-screen requirements.
- [Compose Multiplatform Expressive support](https://kotlinlang.org/docs/multiplatform/whats-new-compose-190.html): Expressive library opt-in and separately versioned Material dependency.

Expressive uses the currently published Material prerelease libraries, explicitly
pinned in Gradle. Compilation uses API 37 tooling; target SDK behavior is retained
rather than silently changing timer, notification, and network permissions in a
visual update. Physical Android 10 tablet checks and Android 17 emulator checks
cover different capabilities; an older tablet cannot demonstrate wallpaper colors.

## Design references

Three concept boards were generated first with the built-in imagegen tool:
1. Material You Android/desktop/widget board, with tonal surfaces and widget insets.
2. Website board, with pill controls, equal download cards, and reduced-motion behavior.
3. Revised Material 3 Expressive tablet board, showing adaptive portrait/landscape layouts and spring motion.

The generated boards are design references only. Their invented logos, example
content, decorative elements, and extra settings are not product requirements.
Actual previews use the original Presence asset and the implemented screens.

## Validation

- 53 JVM tests passed, including timer/sync regressions, desktop menu placement,
  and adaptive-layout cases.
- Four Android instrumentation cases passed on Android 17 in light and dark
  modes: wallpaper palette/fixed palette resolution, widget inflation/insets,
  single-line timer actions, and tall/wide timer layout decisions. Widget grids
  were rendered from the actual XML for visual inspection.
- Windows and Linux packages and launcher smoke checks passed in
  [run 40322779988](https://github.com/akasumitlamba/Presence/actions/runs/40322779988).
  The subsequent Android-only portrait/widget fixes were checked to leave the
  generated desktop Kotlin byte-for-byte unchanged.
- Website phone and desktop widths were checked for horizontal overflow.
- Release APK uses versionCode 25 and the verified existing tablet signing key.


## Adaptive layout follow-up (1.1.1)

The shared stats screen has one manual-entry action, inside the selected day's
card. The duplicate share/manual-action row was removed. Wide timer controls use
their full column width with 24dp spacing and 56dp primary actions; stacked phone
and portrait-tablet controls use compact spacing and retain scrolling. The clock
size is unchanged.

All eleven fixed dark palettes now have brighter accents and subtly tinted card
surfaces. Light palettes, Android wallpaper colors, and the original logo shape
remain intact. Website dark accents match the app; website cards use tonal
surfaces and the focus timeline uses equal-sized cells with internal padding.

Validation: 53 JVM tests passed; all four Android instrumentation cases passed
in both light and dark on Android 17. Actual Android phone and portrait/landscape
tablet layouts and desktop renders from 320px (large text) to 1920px were reviewed.
Website overflow checks passed at 320, 375, 768, 1024, 1440, and 1920px; all eleven
dark palettes meet 4.5:1 for primary button, body, and secondary text colors.
Native package and launcher checks passed in
[run 40329582839](https://github.com/akasumitlamba/Presence/actions/runs/40329582839).
The physical tablet was not connected to ADB during this follow-up.
