import { create } from "zustand";

// ✅ Get initial theme (SSR safe)
const getInitialTheme = () => {
  if (typeof window === "undefined") return "dark";

  const saved = localStorage.getItem("theme");
  if (saved === "dark" || saved === "light") return saved;

  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
  return prefersDark ? "dark" : "light";
};

// ✅ Apply theme to DOM
const applyTheme = (theme) => {
  if (typeof document === "undefined") return;

  const root = document.documentElement;

  // prevent unnecessary repaint
  if (root.classList.contains(theme)) return;

  root.classList.remove("light", "dark");
  root.classList.add(theme);
};

// ✅ Initialize theme immediately (prevents flash)
const initialTheme = getInitialTheme();
if (typeof document !== "undefined") {
  applyTheme(initialTheme);
}

export const useThemeStore = create((set, get) => ({
  theme: initialTheme,

  setTheme: (theme) => {
    if (theme === get().theme) return;

    localStorage.setItem("theme", theme);
    applyTheme(theme);

    set({ theme });
  },

  toggleTheme: () => {
    const newTheme = get().theme === "dark" ? "light" : "dark";

    localStorage.setItem("theme", newTheme);
    applyTheme(newTheme);

    set({ theme: newTheme });
  },
}));

// ✅ OPTIONAL: Sync theme across tabs (pro UX)
if (typeof window !== "undefined") {
  window.addEventListener("storage", (e) => {
    if (e.key === "theme" && e.newValue) {
      applyTheme(e.newValue);
    }
  });
}