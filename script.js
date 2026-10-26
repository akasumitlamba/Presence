(() => {
  'use strict';
  // Palette families and base colors follow the Android app's bundled Theme.kt.
  const palettes = {
  "grove": {
    "light": {
      "accent": "#0e5c42",
      "bg": "#f3f6f4",
      "text": "#10241c",
      "surface": "#eef4f1",
      "muted": "#24382e"
    },
    "dark": {
      "accent": "#8fe0c4",
      "bg": "#111315",
      "text": "#f3f4f3",
      "surface": "#1b1e21",
      "muted": "#b0b7b3"
    }
  },
  "harbor": {
    "light": {
      "accent": "#1e518e",
      "bg": "#f3f5f8",
      "text": "#102030",
      "surface": "#eef2f6",
      "muted": "#243038"
    },
    "dark": {
      "accent": "#9ccaff",
      "bg": "#101418",
      "text": "#f4f7fa",
      "surface": "#1a222c",
      "muted": "#d5dee6"
    }
  },
  "dusk": {
    "light": {
      "accent": "#683996",
      "bg": "#f5f3f8",
      "text": "#1a1020",
      "surface": "#f2eef6",
      "muted": "#2c2438"
    },
    "dark": {
      "accent": "#d8b4fe",
      "bg": "#141018",
      "text": "#f6f4fa",
      "surface": "#241c2c",
      "muted": "#e0d5e8"
    }
  },
  "slate": {
    "light": {
      "accent": "#455260",
      "bg": "#f3f4f6",
      "text": "#14181c",
      "surface": "#f0f2f4",
      "muted": "#2a3036"
    },
    "dark": {
      "accent": "#bccbdd",
      "bg": "#121416",
      "text": "#f4f6f8",
      "surface": "#1e2226",
      "muted": "#d8dee4"
    }
  },
  "berry": {
    "light": {
      "accent": "#912c3d",
      "bg": "#f8f3f4",
      "text": "#241014",
      "surface": "#f6eeef",
      "muted": "#3a2830"
    },
    "dark": {
      "accent": "#ffb1c1",
      "bg": "#181012",
      "text": "#faf4f6",
      "surface": "#22181c",
      "muted": "#e8d5da"
    }
  },
  "amber": {
    "light": {
      "accent": "#6e480c",
      "bg": "#f8f5f0",
      "text": "#241c12",
      "surface": "#f6f1ea",
      "muted": "#3a3024"
    },
    "dark": {
      "accent": "#f4ca80",
      "bg": "#16140f",
      "text": "#f8f4ee",
      "surface": "#1e1a14",
      "muted": "#e8dcc8"
    }
  },
  "moss": {
    "light": {
      "accent": "#3e5c14",
      "bg": "#f5f7f1",
      "text": "#161c10",
      "surface": "#f1f4ea",
      "muted": "#2c3820"
    },
    "dark": {
      "accent": "#bedd8b",
      "bg": "#121610",
      "text": "#f4f7f0",
      "surface": "#161c12",
      "muted": "#dce8cc"
    }
  },
  "iris": {
    "light": {
      "accent": "#3a3e8e",
      "bg": "#f4f4f8",
      "text": "#141420",
      "surface": "#f0f0f6",
      "muted": "#282838"
    },
    "dark": {
      "accent": "#bec2ff",
      "bg": "#101016",
      "text": "#f4f4fa",
      "surface": "#14161e",
      "muted": "#d6dae8"
    }
  },
  "copper": {
    "light": {
      "accent": "#7a3818",
      "bg": "#f8f4f1",
      "text": "#241610",
      "surface": "#f6eeea",
      "muted": "#3a2820"
    },
    "dark": {
      "accent": "#ffb692",
      "bg": "#161210",
      "text": "#faf4f0",
      "surface": "#1c1612",
      "muted": "#e8d4c8"
    }
  },
  "tide": {
    "light": {
      "accent": "#0e5c68",
      "bg": "#f2f6f7",
      "text": "#102024",
      "surface": "#eef4f5",
      "muted": "#24383c"
    },
    "dark": {
      "accent": "#89d5e3",
      "bg": "#101416",
      "text": "#f2f7f8",
      "surface": "#141a1c",
      "muted": "#d0e4e8"
    }
  },
  "ink": {
    "light": {
      "accent": "#243056",
      "bg": "#f3f4f7",
      "text": "#12141c",
      "surface": "#f0f1f5",
      "muted": "#242834"
    },
    "dark": {
      "accent": "#b6c8f4",
      "bg": "#101216",
      "text": "#f4f6f8",
      "surface": "#14181e",
      "muted": "#d6dce4"
    }
  }
};
  const root = document.documentElement;
  const systemAppearance = window.matchMedia('(prefers-color-scheme: dark)');
  const storageKey = 'presence.website.preferences';
  let palette = 'grove';
  let appearance = 'dark';
  try {
    const saved = JSON.parse(localStorage.getItem(storageKey) || 'null');
    if (saved && Object.hasOwn(palettes, saved.palette)) palette = saved.palette;
    if (saved && ['light', 'dark', 'system'].includes(saved.appearance)) appearance = saved.appearance;
  } catch { /* The page works when browser storage is unavailable. */ }

  function luminance(hex) {
    const channels = hex.match(/[a-f\d]{2}/gi).map(part => parseInt(part, 16) / 255)
      .map(value => value <= 0.04045 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4);
    return channels[0] * 0.2126 + channels[1] * 0.7152 + channels[2] * 0.0722;
  }
  function applyAppearance(persist = true) {
    const resolved = appearance === 'system' ? (systemAppearance.matches ? 'dark' : 'light') : appearance;
    const colors = palettes[palette][resolved];
    root.dataset.theme = resolved;
    root.dataset.palette = palette;
    root.style.colorScheme = resolved;
    for (const [key, value] of Object.entries(colors)) root.style.setProperty(`--${key}`, value);
    const whiteContrast = 1.05 / (luminance(colors.accent) + 0.05);
    const darkContrast = (luminance(colors.accent) + 0.05) / (luminance('#111315') + 0.05);
    root.style.setProperty('--on-accent', whiteContrast > darkContrast ? '#ffffff' : '#111315');
    document.querySelector('meta[name="theme-color"]').content = colors.bg;
    document.querySelectorAll('button[data-palette]').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.palette === palette)));
    document.querySelectorAll('button[data-appearance]').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.appearance === appearance)));
    document.getElementById('palette-description').textContent = `Viewing ${palette[0].toUpperCase() + palette.slice(1)} in ${resolved}`;
    const toggle = document.getElementById('appearance-toggle');
    toggle.setAttribute('aria-label', `Switch to ${resolved === 'dark' ? 'light' : 'dark'} appearance`);
    toggle.querySelector('use').setAttribute('href', resolved === 'dark' ? '#i-sun' : '#i-moon');
    if (persist) {
      try { localStorage.setItem(storageKey, JSON.stringify({ palette, appearance })); } catch { /* Optional preference only. */ }
    }
  }
  document.querySelectorAll('button[data-palette]').forEach(button => button.addEventListener('click', () => {
    palette = button.dataset.palette;
    applyAppearance();
  }));
  document.querySelectorAll('button[data-appearance]').forEach(button => button.addEventListener('click', () => {
    appearance = button.dataset.appearance;
    applyAppearance();
  }));
  document.getElementById('appearance-toggle').addEventListener('click', () => {
    appearance = root.dataset.theme === 'dark' ? 'light' : 'dark';
    applyAppearance();
  });
  systemAppearance.addEventListener('change', () => { if (appearance === 'system') applyAppearance(false); });
  applyAppearance(false);

  // This short demonstration never records or transmits sessions.
  const durations = [20000, 5000, 20000, 5000, 20000];
  let phaseIndex = 0;
  let remaining = durations[0];
  let status = 'idle';
  let deadline = 0;
  let interval;
  const continueButton = document.getElementById('preview-continue');
  const stopButton = document.getElementById('preview-stop');
  const skipButton = document.getElementById('preview-skip');
  const timeLabel = document.getElementById('preview-time');
  const phaseLabel = document.getElementById('preview-phase');
  const cycleLabel = document.getElementById('preview-cycle');
  const statusLabel = document.getElementById('preview-status');
  const progress = document.getElementById('dial-progress');
  const segments = [...document.querySelectorAll('#session-arcs path')];
  const circumference = 2 * Math.PI * 148;

  function render() {
    const seconds = Math.ceil(remaining / 1000);
    timeLabel.textContent = `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`;
    const complete = status === 'complete';
    const isBreak = phaseIndex % 2 === 1;
    phaseLabel.textContent = complete ? 'Complete' : isBreak ? 'Short break' : 'Focus';
    timeLabel.setAttribute('aria-label', `${complete ? 'Preview complete' : isBreak ? 'Break' : 'Focus'} time remaining`);
    cycleLabel.textContent = complete ? '3 stretches completed' : `Stretch ${Math.floor(phaseIndex / 2) + 1} of 3${status === 'paused' ? ' · paused' : ''}`;
    continueButton.textContent = status === 'running' ? 'Pause' : 'Continue';
    stopButton.disabled = status === 'idle';
    skipButton.disabled = !['running', 'paused'].includes(status);
    const fraction = complete ? 1 : 1 - remaining / durations[phaseIndex];
    progress.style.strokeDashoffset = String(circumference * (1 - Math.max(0, Math.min(1, fraction))));
    segments.forEach((segment, index) => {
      segment.classList.toggle('done', complete || index < Math.floor(phaseIndex / 2));
      segment.classList.toggle('current', !complete && index === Math.floor(phaseIndex / 2));
    });
  }
  function run() {
    clearInterval(interval);
    status = 'running';
    deadline = Date.now() + remaining;
    interval = setInterval(() => {
      remaining = Math.max(0, deadline - Date.now());
      if (remaining === 0) advance();
      else render();
    }, 200);
    statusLabel.textContent = phaseIndex % 2 ? 'Take a breath. This preview break lasts 5 seconds.' : 'A short focus preview. Real sessions use the plan you choose in the app.';
    render();
  }
  function advance() {
    clearInterval(interval);
    phaseIndex++;
    if (phaseIndex === durations.length) {
      status = 'complete'; remaining = 0;
      statusLabel.textContent = 'Preview complete. Start again whenever you’re ready.';
      render(); return;
    }
    remaining = durations[phaseIndex];
    if (phaseIndex % 2) run();
    else {
      status = 'waiting';
      statusLabel.textContent = 'Break finished. Choose Continue for your next stretch.';
      render();
    }
  }
  continueButton.addEventListener('click', () => {
    if (status === 'running') {
      remaining = Math.max(0, deadline - Date.now());
      clearInterval(interval); status = 'paused';
      statusLabel.textContent = 'Paused. Continue when you’re ready.';
      render();
    } else {
      if (status === 'complete') { phaseIndex = 0; remaining = durations[0]; }
      run();
    }
  });
  stopButton.addEventListener('click', () => {
    clearInterval(interval); phaseIndex = 0; remaining = durations[0]; status = 'idle';
    statusLabel.textContent = 'Preview reset. Choose Continue to begin.';
    render();
  });
  skipButton.addEventListener('click', advance);
  render();
  ['appearance-toggle', 'appearance-options', 'palette-options', 'preview-controls'].forEach(id => { document.getElementById(id).hidden = false; });
})();

// Reveal only on entry, with a no-JavaScript and reduced-motion-safe baseline.
if ('IntersectionObserver' in window && !matchMedia('(prefers-reduced-motion: reduce)').matches) {
  const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('material-reveal');
        observer.unobserve(entry.target);
      }
    });
  }, {threshold: 0.12});
  document.querySelectorAll('.rhythm-grid article,.plan-preview,.download-section').forEach(node => observer.observe(node));
}
