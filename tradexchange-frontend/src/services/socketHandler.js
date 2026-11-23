// ===============================
// 📁 src/services/socketHandler.js (FINAL)
// ===============================

import { socket } from "./socket";
import { useMarketStore } from "../store/marketStore";

let initialized = false;

export const initSocketHandlers = () => {
  if (initialized) return; // 🔥 prevent duplicate listeners
  initialized = true;

  const store = useMarketStore.getState();

  console.log("🚀 Initializing socket handlers...");

  /* ===============================
     📈 TRADE EVENT
  =============================== */
  socket.on("trade", (data) => {
    console.log("📈 TRADE:", data); // 🔥 DEBUG

    if (!data?.symbol) return;

    store.updateMarket(data.symbol, {
      price: Number(data.price),
    });
  });

  /* ===============================
     🕯️ CANDLE EVENT
  =============================== */
  socket.on("candle", (data) => {
    console.log("🕯️ CANDLE:", data); // 🔥 DEBUG

    const c = data?.candle;
    if (!c || !data.symbol) return;

    store.updateMarket(data.symbol, {
      price: Number(c.close),
    });
  });

  /* ===============================
     📊 DEPTH EVENT (optional)
  =============================== */
  socket.on("depth", (data) => {
    console.log("📊 DEPTH:", data); // optional debug
  });

  console.log("✅ Socket handlers ready");
};