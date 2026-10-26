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
Short windows keep scrollable single-pane content. Window size, rather than a
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
