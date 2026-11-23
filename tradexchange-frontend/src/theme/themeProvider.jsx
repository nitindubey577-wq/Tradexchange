import { useEffect } from "react";
import { useThemeStore } from "./themeStore";

const ThemeProvider = ({ children }) => {
  const { theme, setTheme } = useThemeStore();

  // 🔹 Apply theme to <html>
  useEffect(() => {
    const root = document.documentElement;

    root.classList.remove("light", "dark");
    root.classList.add(theme);

    // ✅ Persist theme
    localStorage.setItem("theme", theme);
  }, [theme]);

  // 🔹 Initial theme setup (on first load)
  useEffect(() => {
    const savedTheme = localStorage.getItem("theme");

    if (savedTheme) {
      setTheme(savedTheme);
    } else {
      const prefersDark = window.matchMedia(
        "(prefers-color-scheme: dark)"
      ).matches;

      setTheme(prefersDark ? "dark" : "light");
    }
  }, [setTheme]);

  // 🔹 Listen to system theme changes (PRO FEATURE)
  useEffect(() => {
    const media = window.matchMedia("(prefers-color-scheme: dark)");

    const handleChange = (e) => {
      const savedTheme = localStorage.getItem("theme");

      // Only auto-update if user hasn't manually selected
      if (!savedTheme) {
        setTheme(e.matches ? "dark" : "light");
      }
    };

    media.addEventListener("change", handleChange);

    return () => {
      media.removeEventListener("change", handleChange);
    };
  }, [setTheme]);

  return children;
};

export default ThemeProvider;