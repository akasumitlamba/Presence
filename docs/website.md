# Presence website

The public site is a single static page in the repository root: `index.html`, `styles.css`, `script.js`, and `assets/`. GitHub Pages serves `main` → `/ (root)` with `.nojekyll`; no build process is required.

## Design

The implementation follows [Anthropic’s frontend-design skill](https://github.com/anthropics/skills/blob/main/skills/frontend-design/SKILL.md). Presence’s charcoal surfaces, sage accents, typography, thick timer ring, and curved session indicators establish the visual direction. The page includes the eleven application palette families and light, dark, and system appearance previews. Website accent variants prioritize readable contrast.

Fonts and the application icon are local assets. The font subsets have been renamed Presence Web Sans in accordance with the original font license; see `assets/fonts/README.md`.

The interactive timer is explicitly a 20-second demonstration. It uses elapsed wall-clock time, supports pause/resume, stop, skip, breaks, completion, and restart, and does not record application sessions. Optional local storage remembers only website appearance preferences. No analytics, external scripts, or network services are used by the page itself.

## Local preview

```sh
python3 -m http.server 8000
```

Open `http://localhost:8000/`. To reproduce the project URL prefix, serve the parent directory and open `/Presence/`. All asset links are relative.

## Verification

Browser checks cover:

- Widths 320, 360, 390, 768, 1024, 1440, and 1920 pixels, with no horizontal overflow.
- All eleven palettes in light and dark appearance, including text and button contrast.
- Timer elapsed time, pause/resume, automatic break transitions, skipping, completion, restart, and stop.
- System appearance changes, saved preferences, and blocked browser storage.
- Keyboard skip navigation, reduced motion, valid section anchors, and loaded local fonts.
- Download links and native FAQ interaction with JavaScript disabled.
- Asset loading from the `/Presence/` project path and absence of browser JavaScript errors.

Desktop and mobile layouts are also visually inspected. These checks apply to the website; the Android application has its own validation notes.
