import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css";

/* 🔥 APPLY THEME BEFORE RENDER */
const setInitialTheme = () => {
  try {
    const saved = localStorage.getItem("theme");

    if (saved === "dark" || saved === "light") {
      document.documentElement.classList.add(saved);
    } else {
      const prefersDark = window.matchMedia(
        "(prefers-color-scheme: dark)"
      ).matches;

      document.documentElement.classList.add(
        prefersDark ? "dark" : "light"
      );
    }
  } catch (err) {
    console.warn("Theme init failed:", err);
    document.documentElement.classList.add("dark");
  }
};

setInitialTheme();

/* 🔥 RENDER APP */
ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);