Build a mobile-first floating mini-panel (HTML/CSS/JS, Tailwind via CDN) that appears as a compact overlay window on top of other apps — like the Gemini/Google Assistant overlay that shows when you long-press the power button. It must NOT be full-screen; it is a small draggable window.

Visual style (Gemini / Google Material You):
- Compact floating window: width ~340-400px, height by content (max ~70vh), centered or bottom-anchored, with rounded corners (28-32px), frosted glass ("liquid glass"): backdrop-blur, semi-transparent dark surface, soft inner highlight border.
- Dark theme: gray-900/950 + black base, RGB accent gradients (blue→purple→pink) used sparingly (search focus ring, selected chip, copy button).
- Spring-like animations: panel scales+ fades in from center (cubic-bezier(0.32,0.72,0,1)), images stagger in, buttons ripple/scale. Respect prefers-reduced-motion.
- Soft shadows, glassmorphism, no harsh borders. Background behind panel is dimmed/blurred but the panel itself is the only interactive surface.

Layout (compact):
- Top: pill-shaped search input (auto-focus) with search/mic icons.
- Row of filter chips (All / Photos / Illustrations / Textures), horizontal scroll.
- Scrollable grid of image cards (2 columns) with rounded corners and a "Copy" button overlay.
- Bottom toast: "Скопировано в буфер".

Behavior:
- Typing filters a mock dataset {id, url, thumb, title}. Optional Unsplash API via UNSPLASH_KEY constant (empty → mock data).
- "Copy" button: download image → copy to clipboard. Use navigator.clipboard with ClipboardItem; fallback to copying URL as text + toast. Provide window.AndroidBridge.copyImage(url) hook for native override.
- Loading skeletons, empty/error states.
- All UI labels in Russian; code comments in English.

Technical constraints:
- Single index.html (+ styles.css, app.js). No build step; runs in WebView.
- Responsive down to 360px; touch targets ≥44px.
- The panel must be draggable by its header.
- Provide README.md: how to wrap into APK (Capacitor or minimal WebView Activity), how to make it a floating overlay window (WindowManager TYPE_APPLICATION_OVERLAY or Activity with windowIsFloating=true + transparent background), and how to add a Quick Settings tile (TileService) that opens this mini-window. Note: the tile and clipboard must be implemented natively in Kotlin/Java.

Deliverables: index.html, styles.css, app.js, README.md. Ответь на русском, скинь ссылку