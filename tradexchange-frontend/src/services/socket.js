// ===============================
// 📁 src/services/socket.js (PRO FIXED)
// ===============================

import { io } from "socket.io-client";

const URL = import.meta.env.VITE_API_URL || "http://localhost:5000";

/* ---------------- SOCKET ---------------- */

export const socket = io(URL, {
  transports: ["websocket"],
  reconnection: true,
  reconnectionAttempts: Infinity,
  reconnectionDelay: 1000, // ✅ FIXED TYPO
  reconnectionDelayMax: 5000,
});

/* ---------------- STATE ---------------- */

const activeSymbols = new Set();

/* ---------------- CONNECTION ---------------- */

socket.on("connect", () => {
  console.log("✅ SOCKET CONNECTED:", socket.id);

  // 🔥 re-subscribe after reconnect
  activeSymbols.forEach((symbol) => {
    socket.emit("subscribe", symbol); // ✅ FIXED
  });
});

socket.on("disconnect", (reason) => {
  console.log("❌ SOCKET DISCONNECTED:", reason);
});

socket.on("connect_error", (err) => {
  console.log("❌ SOCKET ERROR:", err.message);
});

/* ---------------- SUBSCRIPTION MANAGER ---------------- */

export const subscribeSymbol = (symbol) => {
  if (!symbol || activeSymbols.has(symbol)) return;

  const sym = symbol.toUpperCase();

  activeSymbols.add(sym);
  socket.emit("subscribe", sym); // ✅ FIXED

  console.log("📡 SUBSCRIBED:", sym);
};

export const unsubscribeSymbol = (symbol) => {
  if (!symbol || !activeSymbols.has(symbol)) return;

  const sym = symbol.toUpperCase();

  activeSymbols.delete(sym);
  socket.emit("unsubscribe", sym); // ✅ FIXED

  console.log("📴 UNSUBSCRIBED:", sym);
};