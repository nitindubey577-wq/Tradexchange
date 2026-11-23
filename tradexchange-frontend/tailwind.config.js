/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
       colors: {
      bg: "var(--bg)",
      card: "var(--card)",
      border: "var(--border)",
      text: "var(--text)",
      muted: "var(--muted)",
      primary: "var(--primary)",
    },
    },
  },
  plugins: [],
};