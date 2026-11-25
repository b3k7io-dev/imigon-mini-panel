// Floating image search mini-panel.
// All user-facing labels are in Russian; comments are in English.

// Runtime configuration. config.js may provide a key for file:// usage.
let UNSPLASH_KEY = (window.APP_CONFIG && window.APP_CONFIG.UNSPLASH_KEY) || "";

const FILTERS = [
  { key: "all", label: "Все" },
  { key: "photos", label: "Фото" },
  { key: "illustrations", label: "Иллюстрации" },
  { key: "textures", label: "Текстуры" }
];

// Built-in mock data. Tags make browser-like search more forgiving.
const MOCK_DATA = [
  { id: "m1", title: "Горный пейзаж", category: "photos", tags: ["горы", "природа", "пейзаж", "фото"], url: "https://picsum.photos/seed/mountain/900/600", thumb: "https://picsum.photos/seed/mountain/420/280" },
  { id: "m2", title: "Городская неоновая улица", category: "photos", tags: ["город", "неон", "ночь", "улица", "фото"], url: "https://picsum.photos/seed/neoncity/900/600", thumb: "https://picsum.photos/seed/neoncity/420/280" },
  { id: "m3", title: "Спокойное озеро", category: "photos", tags: ["озеро", "вода", "природа", "пейзаж", "фото"], url: "https://picsum.photos/seed/lake/900/600", thumb: "https://picsum.photos/seed/lake/420/280" },
  { id: "m4", title: "Абстрактные формы", category: "illustrations", tags: ["абстракция", "формы", "цвет", "иллюстрация"], url: "https://picsum.photos/seed/abstract/900/600", thumb: "https://picsum.photos/seed/abstract/420/280" },
  { id: "m5", title: "Иллюстрация космоса", category: "illustrations", tags: ["космос", "звезды", "галактика", "иллюстрация"], url: "https://picsum.photos/seed/space/900/600", thumb: "https://picsum.photos/seed/space/420/280" },
  { id: "m6", title: "Цветочные узоры", category: "illustrations", tags: ["цветы", "узор", "растения", "иллюстрация"], url: "https://picsum.photos/seed/floral/900/600", thumb: "https://picsum.photos/seed/floral/420/280" },
  { id: "m7", title: "Бетонная текстура", category: "textures", tags: ["бетон", "текстура", "материал", "серый"], url: "https://picsum.photos/seed/concrete/900/600", thumb: "https://picsum.photos/seed/concrete/420/280" },
  { id: "m8", title: "Деревянная поверхность", category: "textures", tags: ["дерево", "текстура", "материал", "коричневый"], url: "https://picsum.photos/seed/wood/900/600", thumb: "https://picsum.photos/seed/wood/420/280" },
  { id: "m9", title: "Тканевое плетение", category: "textures", tags: ["ткань", "плетение", "текстура", "материал"], url: "https://picsum.photos/seed/fabric/900/600", thumb: "https://picsum.photos/seed/fabric/420/280" },
  { id: "m10", title: "Морской закат", category: "photos", tags: ["море", "закат", "небо", "пейзаж", "фото"], url: "https://picsum.photos/seed/sunset/900/600", thumb: "https://picsum.photos/seed/sunset/420/280" },
  { id: "m11", title: "Геометрический паттерн", category: "illustrations", tags: ["геометрия", "паттерн", "формы", "иллюстрация"], url: "https://picsum.photos/seed/geometry/900/600", thumb: "https://picsum.photos/seed/geometry/420/280" },
  { id: "m12", title: "Мраморная текстура", category: "textures", tags: ["мрамор", "текстура", "камень", "материал"], url: "https://picsum.photos/seed/marble/900/600", thumb: "https://picsum.photos/seed/marble/420/280" },
  { id: "m13", title: "Лесная тропа", category: "photos", tags: ["лес", "тропа", "природа", "фото"], url: "https://picsum.photos/seed/forest/900/600", thumb: "https://picsum.photos/seed/forest/420/280" },
  { id: "m14", title: "Снежные вершины", category: "photos", tags: ["снег", "горы", "зима", "пейзаж", "фото"], url: "https://picsum.photos/seed/snowpeaks/900/600", thumb: "https://picsum.photos/seed/snowpeaks/420/280" },
  { id: "m15", title: "Акварельный градиент", category: "illustrations", tags: ["акварель", "градиент", "цвет", "иллюстрация"], url: "https://picsum.photos/seed/watercolor/900/600", thumb: "https://picsum.photos/seed/watercolor/420/280" },
  { id: "m16", title: "Металлическая поверхность", category: "textures", tags: ["металл", "текстура", "материал", "блеск"], url: "https://picsum.photos/seed/metal/900/600", thumb: "https://picsum.photos/seed/metal/420/280" }
];

// Application state.
const state = {
  query: "",
  category: "all",
  items: [],
  loading: false,
  error: null
};

// DOM references.
const panel = document.getElementById("panel");
const backdrop = document.getElementById("backdrop");
const dragHandle = document.getElementById("dragHandle");
const searchInput = document.getElementById("searchInput");
const clearButton = document.getElementById("clearButton");
const closeButton = document.getElementById("closeButton");
const reopenButton = document.getElementById("reopenButton");
const chipsContainer = document.getElementById("chips");
const grid = document.getElementById("grid");
const skeletons = document.getElementById("skeletons");
const status = document.getElementById("status");
const toast = document.getElementById("toast");

let searchTimer = null;
let toastTimer = null;

const STORAGE_KEY = "floatingMiniPanel.state";

// Persist the last query, filter and dragged position for overlay reopen.
function readStoredState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    console.warn("Could not read stored panel state", error);
    return null;
  }
}

function writeStoredState(partial) {
  try {
    const current = readStoredState() || {};
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...current, ...partial }));
  } catch (error) {
    console.warn("Could not store panel state", error);
  }
}

// Keep a restored panel position inside the current viewport.
function isPositionVisible(left, top) {
  const margin = 8;
  const width = panel.offsetWidth || 320;
  const height = panel.offsetHeight || 320;
  const x = parseFloat(left);
  const y = parseFloat(top);
  if (!Number.isFinite(x) || !Number.isFinite(y)) {
    return false;
  }
  return (
    x >= margin &&
    y >= margin &&
    x + width <= window.innerWidth - margin &&
    y + height <= window.innerHeight - margin
  );
}

function applyStoredPosition(savedState) {
  if (
    savedState &&
    savedState.position &&
    isPositionVisible(savedState.position.left, savedState.position.top)
  ) {
    panel.style.left = savedState.position.left;
    panel.style.top = savedState.position.top;
    panel.style.transform = "none";
    return true;
  }

  // Reset to the centered CSS position when the saved one is unusable.
  panel.style.left = "";
  panel.style.top = "";
  panel.style.transform = "";
  return false;
}

// Load .env when served over HTTP. This keeps API keys out of the UI bundle.
async function loadEnv() {
  try {
    const response = await fetch("./.env", { cache: "no-store" });
    if (!response.ok) {
      return;
    }

    const text = await response.text();
    const values = {};

    text.split(/\r?\n/).forEach((line) => {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith("#")) {
        return;
      }

      const separatorIndex = trimmed.indexOf("=");
      if (separatorIndex === -1) {
        return;
      }

      const key = trimmed.slice(0, separatorIndex).trim();
      const value = trimmed.slice(separatorIndex + 1).trim().replace(/^["']|["']$/g, "");
      values[key] = value;
    });

    if (values.UNSPLASH_KEY) {
      UNSPLASH_KEY = values.UNSPLASH_KEY;
    } else if (values.UNSPLASH_ACCESS_KEY) {
      UNSPLASH_KEY = values.UNSPLASH_ACCESS_KEY;
    }
  } catch (error) {
    // file:// and some WebView contexts cannot fetch .env; config.js still works.
    console.info("Environment file is not available", error);
  }
}

// Utility: debounce typing so filtering does not run on every keystroke.
function debounce(callback, delay) {
  return function debounced(...args) {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => callback.apply(this, args), delay);
  };
}

// Utility: escape text before inserting it into innerHTML.
function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

// Toast helper.
function showToast(message) {
  toast.textContent = message;
  toast.classList.add("is-visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    toast.classList.remove("is-visible");
  }, 2200);
}

// Render filter chips.
function renderChips() {
  chipsContainer.innerHTML = FILTERS.map((filter) => {
    const active = filter.key === state.category ? "is-active" : "";
    return `
      <button
        type="button"
        class="chip ripple ${active}"
        data-category="${filter.key}"
        role="tab"
        aria-selected="${filter.key === state.category}"
      >
        ${escapeHtml(filter.label)}
      </button>
    `;
  }).join("");

  chipsContainer.querySelectorAll(".chip").forEach((chip) => {
    chip.addEventListener("click", () => {
      state.category = chip.dataset.category;
      renderChips();
      writeStoredState({ category: state.category });
      loadResults();
    });
  });
}

// Render image cards.
function renderGrid(items) {
  if (!items.length) {
    grid.innerHTML = "";
    return;
  }

  grid.innerHTML = items.map((item, index) => `
    <article class="image-card" style="animation-delay: ${Math.min(index, 12) * 45}ms">
      <img
        src="${escapeHtml(item.thumb || item.url)}"
        alt="${escapeHtml(item.title || "Изображение")}"
        loading="lazy"
        decoding="async"
      />
      <span class="card-title">${escapeHtml(item.title || "Без названия")}</span>
      <button
        type="button"
        class="copy-btn ripple"
        data-id="${escapeHtml(item.id)}"
        aria-label="Скопировать изображение"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M8 8a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-8a2 2 0 0 1-2-2V8Zm-4 0h2v10a2 2 0 0 0 2 2h10v2a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V10a2 2 0 0 1 2-2Z" />
        </svg>
      </button>
    </article>
  `).join("");

  // Attach copy handlers after rendering.
  grid.querySelectorAll(".copy-btn").forEach((button) => {
    button.addEventListener("click", (event) => {
      event.stopPropagation();
      const item = items.find((entry) => String(entry.id) === button.dataset.id);
      if (item) {
        copyImage(item);
      }
    });
  });
}

// Render status text, empty state or error state.
function renderStatus() {
  if (state.loading) {
    status.hidden = true;
    return;
  }

  if (state.error) {
    status.hidden = false;
    status.innerHTML = `
      <div>Не удалось загрузить изображения.</div>
      <div style="opacity:.72;margin-top:4px">${escapeHtml(state.error)}</div>
      <button type="button" class="status-action ripple" id="retryButton">Повторить</button>
    `;
    document.getElementById("retryButton")?.addEventListener("click", loadResults);
    return;
  }

  if (!state.items.length) {
    status.hidden = false;
    status.innerHTML = `
      <div>Ничего не найдено.</div>
      <div style="opacity:.72;margin-top:4px">Попробуйте другой запрос, тег или фильтр.</div>
      <button type="button" class="status-action ripple" id="resetFiltersButton">Показать всё</button>
    `;
    document.getElementById("resetFiltersButton")?.addEventListener("click", () => {
      state.query = "";
      state.category = "all";
      searchInput.value = "";
      updateClearButton();
      renderChips();
      loadResults();
    });
    return;
  }

  status.hidden = true;
  status.innerHTML = "";
}

// Loading state.
function setLoading(loading) {
  state.loading = loading;
  skeletons.hidden = !loading;
  grid.hidden = loading;
  if (loading) {
    status.hidden = true;
  }
}

// Map an Unsplash photo to the app's item shape.
function mapUnsplashPhoto(photo) {
  return {
    id: photo.id,
    title: photo.alt_description || photo.description || "Изображение Unsplash",
    category: state.category === "all" ? "photos" : state.category,
    url: photo.urls?.regular || photo.urls?.full || photo.urls?.small,
    thumb: photo.urls?.small || photo.urls?.thumb
  };
}

// Load results from Unsplash or mock data.
async function loadResults() {
  setLoading(true);
  state.error = null;

  try {
    if (UNSPLASH_KEY) {
      const categoryWord = {
        all: "",
        photos: "photo",
        illustrations: "illustration",
        textures: "texture"
      }[state.category] || "";

      const queryParts = [state.query.trim(), categoryWord].filter(Boolean);
      const query = queryParts.join(" ") || "wallpaper";
      const endpoint = `https://api.unsplash.com/search/photos?query=${encodeURIComponent(query)}&per_page=18&orientation=landscape`;

      const response = await fetch(endpoint, {
        headers: {
          Authorization: `Client-ID ${UNSPLASH_KEY}`
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();
      state.items = Array.isArray(data.results) ? data.results.map(mapUnsplashPhoto) : [];
    } else {
      // Small artificial delay keeps the skeleton state visible and testable.
      await new Promise((resolve) => setTimeout(resolve, 180));

      const query = state.query.trim().toLowerCase();
      state.items = MOCK_DATA.filter((item) => {
        const categoryMatch = state.category === "all" || item.category === state.category;
        const haystack = [item.title, item.category, ...(item.tags || [])].join(" ").toLowerCase();
        const queryMatch = !query || haystack.includes(query);
        return categoryMatch && queryMatch;
      });
    }
  } catch (error) {
    // Keep search usable when the optional Unsplash key is invalid or the
    // network is unavailable: fall back to the filtered local mock data.
    if (UNSPLASH_KEY) {
      // Preserve the last successful result set during transient failures so the
      // grid is not wiped by a temporary network or API error.
      if (state.items && state.items.length) {
        state.error = null;
        showToast("Не удалось обновить результаты. Показаны предыдущие.");
      } else {
        const query = state.query.trim().toLowerCase();
        state.items = MOCK_DATA.filter((item) => {
          const categoryMatch = state.category === "all" || item.category === state.category;
          const haystack = [item.title, item.category, ...(item.tags || [])].join(" ").toLowerCase();
          const queryMatch = !query || haystack.includes(query);
          return categoryMatch && queryMatch;
        });
        state.error = null;
        showToast("Unsplash недоступен, показаны локальные результаты");
      }
    } else {
      state.items = [];
      state.error = error?.message || "Неизвестная ошибка";
    }
  } finally {
    setLoading(false);
    renderGrid(state.items);
    renderStatus();
  }
}

// Copy an image to the clipboard.
async function copyImage(item) {
  // Native Android override, if the WebView host provides one.
  if (window.AndroidBridge && typeof window.AndroidBridge.copyImage === "function") {
    try {
      window.AndroidBridge.copyImage(item.url);
      showToast("Скопировано в буфер");
      return;
    } catch (error) {
      console.error("AndroidBridge.copyImage failed", error);
    }
  }

  // Try to copy the actual image bytes.
  if (navigator.clipboard && typeof ClipboardItem !== "undefined") {
    try {
      const response = await fetch(item.url, { mode: "cors" });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const blob = await response.blob();
      const mimeType = blob.type || "image/png";
      await navigator.clipboard.write([
        new ClipboardItem({ [mimeType]: blob })
      ]);
      showToast("Скопировано в буфер");
      return;
    } catch (error) {
      console.warn("Clipboard image copy failed, falling back to URL", error);
    }
  }

  // Fallback: copy the image URL as text.
  let textCopied = false;
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(item.url);
      textCopied = true;
    }
  } catch (error) {
    console.warn("Clipboard text copy failed", error);
  }

  if (!textCopied) {
    try {
      const textarea = document.createElement("textarea");
      textarea.value = item.url;
      textarea.setAttribute("readonly", "");
      textarea.style.position = "fixed";
      textarea.style.opacity = "0";
      document.body.appendChild(textarea);
      textarea.select();
      textCopied = document.execCommand("copy");
      document.body.removeChild(textarea);
    } catch (error) {
      console.warn("execCommand copy failed", error);
    }
  }

  if (textCopied) {
    showToast("Ссылка скопирована");
    return;
  }

  showToast("Не удалось скопировать");
}

// Show or hide the clear button like a browser search field.
function updateClearButton() {
  clearButton.hidden = !searchInput.value.trim();
}

// Dragging the panel by its header.
function setupDragging() {
  let dragging = false;
  let startX = 0;
  let startY = 0;
  let startLeft = 0;
  let startTop = 0;
  let hasMoved = false;

  function beginDrag(event) {
    // Do not start dragging when interacting with form controls.
    if (event.target.closest("button, input, textarea, select")) {
      return;
    }

    const rect = panel.getBoundingClientRect();
    dragging = true;
    hasMoved = false;
    startX = event.clientX;
    startY = event.clientY;
    startLeft = rect.left;
    startTop = rect.top;
    panel.classList.add("is-dragging");

    // Pin the current visual position so the panel does not jump.
    panel.style.left = `${rect.left}px`;
    panel.style.top = `${rect.top}px`;
    panel.style.transform = "none";

    // Pointer capture is an optimization. Window listeners below provide the
    // fallback when capture is unavailable or fails on a mobile WebView.
    try {
      dragHandle.setPointerCapture(event.pointerId);
    } catch (error) {
      console.info("Pointer capture unavailable, using window listeners", error);
    }
  }

  function moveDrag(event) {
    if (!dragging) {
      return;
    }

    const dx = event.clientX - startX;
    const dy = event.clientY - startY;

    // Ignore tiny movements to avoid accidental drags on taps.
    if (!hasMoved && Math.hypot(dx, dy) < 4) {
      return;
    }
    hasMoved = true;

    const margin = 8;
    const maxLeft = window.innerWidth - panel.offsetWidth - margin;
    const maxTop = window.innerHeight - panel.offsetHeight - margin;

    const newLeft = Math.max(margin, Math.min(startLeft + dx, maxLeft));
    const newTop = Math.max(margin, Math.min(startTop + dy, maxTop));

    panel.style.left = `${newLeft}px`;
    panel.style.top = `${newTop}px`;

    // Prevent the page behind the overlay from scrolling while dragging.
    if (typeof event.preventDefault === "function") {
      event.preventDefault();
    }
  }

  function stopDragging() {
    if (!dragging) {
      return;
    }
    const wasMoved = hasMoved;
    dragging = false;
    panel.classList.remove("is-dragging");

    // Persist the dragged position so the overlay can reopen where the user left it.
    if (wasMoved && panel.style.left && panel.style.top) {
      writeStoredState({
        position: {
          left: panel.style.left,
          top: panel.style.top
        }
      });
    }
  }

  // Use both the handle and window listeners. This keeps dragging alive when
  // pointer capture is denied by the WebView or the finger leaves the handle.
  dragHandle.addEventListener("pointerdown", beginDrag);
  window.addEventListener("pointermove", moveDrag, { passive: false });
  window.addEventListener("pointerup", stopDragging);
  window.addEventListener("pointercancel", stopDragging);
  window.addEventListener("blur", stopDragging);
}

// Close and reopen behavior for browser previews.
function closePanel() {
  panel.classList.remove("is-open");
  panel.setAttribute("aria-hidden", "true");
  setTimeout(() => {
    panel.hidden = true;
    reopenButton.hidden = false;
  }, 220);
}

function openPanel() {
  panel.hidden = false;
  reopenButton.hidden = true;

  const savedState = readStoredState();
  if (savedState) {
    if (typeof savedState.query === "string") {
      state.query = savedState.query;
      searchInput.value = savedState.query;
    }
    if (savedState.category && FILTERS.some((filter) => filter.key === savedState.category)) {
      state.category = savedState.category;
      renderChips();
    }
    updateClearButton();
  }

  applyStoredPosition(savedState);

  requestAnimationFrame(() => {
    panel.classList.add("is-open");
    panel.setAttribute("aria-hidden", "false");
    searchInput.focus();
  });

  loadResults();
}

// Initialize the panel.
async function init() {
  await loadEnv();

  const savedState = readStoredState();
  if (savedState && FILTERS.some((filter) => filter.key === savedState.category)) {
    state.category = savedState.category;
  }
  if (savedState && typeof savedState.query === "string") {
    state.query = savedState.query;
    searchInput.value = savedState.query;
  }

  renderChips();

  const handleSearch = debounce(() => {
    state.query = searchInput.value;
    updateClearButton();
    writeStoredState({ query: state.query });
    loadResults();
  }, 150);

  searchInput.addEventListener("input", handleSearch);
  searchInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      clearTimeout(searchTimer);
      state.query = searchInput.value;
      updateClearButton();
      writeStoredState({ query: state.query });
      loadResults();
    } else if (event.key === "Escape") {
      searchInput.value = "";
      state.query = "";
      updateClearButton();
      writeStoredState({ query: "" });
      loadResults();
    }
  });

  clearButton.addEventListener("click", () => {
    searchInput.value = "";
    state.query = "";
    updateClearButton();
    writeStoredState({ query: "" });
    searchInput.focus();
    loadResults();
  });

  closeButton.addEventListener("click", closePanel);
  reopenButton.addEventListener("click", openPanel);

  // Close on Escape when focus is outside the search field.
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !panel.hidden && document.activeElement !== searchInput) {
      closePanel();
    }
  });

  setupDragging();
  updateClearButton();

  // Keep the floating panel usable after rotation or viewport resize.
  window.addEventListener("resize", () => {
    if (panel.style.left && panel.style.top && !isPositionVisible(panel.style.left, panel.style.top)) {
      panel.style.left = "";
      panel.style.top = "";
      panel.style.transform = "";
    }
  });

  applyStoredPosition(savedState);

  // Initial entrance.
  requestAnimationFrame(() => {
    panel.classList.add("is-open");
  });

  // Focus search shortly after the entrance animation.
  setTimeout(() => {
    searchInput.focus();
  }, 120);

  loadResults();
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", init);
} else {
  init();
}
