// ===============================
// 📁 src/store/marketStore.js (FINAL CLEAN + SAFE)
// ===============================

import { create } from "zustand";
import { persist } from "zustand/middleware";
import { MARKETS } from "../constants/markets";

/* ================= HELPERS ================= */

// Normalize symbol (BTC/INR → BTC-INR)
const normalize = (s) => s?.replace("/", "-").toUpperCase();

/* Build map */
const buildMap = (markets) => {
  const map = {};
  for (const m of markets) {
    const key = normalize(m.symbol || m.name);
    map[key] = m;
  }
  return map;
};

/* Group markets */
const group = (markets) => {
  const g = {
    all: markets,
    crypto: [],
    stock: [],
    commodity: [],
    other: [],
  };

  for (const m of markets) {
    const cat = (m.category || "other").toLowerCase();
    (g[cat] || g.other).push(m);
  }

  return g;
};

/* ================= DEFAULT ================= */

const DEFAULT_MARKETS = Array.isArray(MARKETS)
  ? MARKETS.map((m, i) => ({
      id: i + 1,
      symbol: normalize(m.name),
      price: Number(m.price) || 0,
      change: Number(m.change) || 0,
      category: (m.category || "other").toLowerCase(),
      lastPrice: Number(m.price) || 0,
    }))
  : [];

/* ================= STORE ================= */

export const useMarketStore = create(
  persist(
    (set, get) => ({
      /* ---------- STATE ---------- */
      markets: DEFAULT_MARKETS,
      marketMap: buildMap(DEFAULT_MARKETS),
      groupedMarkets: group(DEFAULT_MARKETS),

      selectedSymbol: "BTC-INR",
      watchlist: [],

      /* ================= SET MARKETS ================= */
      setMarkets: (list) => {
        if (!Array.isArray(list)) return;

        const normalized = list.map((m, i) => ({
          id: m.id || i + 1,
          symbol: normalize(m.symbol || m.name),
          price: Number(m.price) || 0,
          change: Number(m.change) || 0,
          category: (m.category || "other").toLowerCase(),
          lastPrice: Number(m.price) || 0,
        }));

        const map = buildMap(normalized);
        const arr = Object.values(map);

        set({
          markets: arr,
          marketMap: map,
          groupedMarkets: group(arr),
        });
      },

      /* ================= REAL-TIME UPDATE ================= */
      updateMarket: (symbol, price) => {
        if (!symbol || price === undefined) return;

        set((state) => {
          const key = normalize(symbol);
          const existing = state.marketMap[key];

          if (!existing) return state;

          const newPrice = Number(price);
          if (isNaN(newPrice)) return state;

          const prevPrice =
            existing.lastPrice ?? existing.price ?? newPrice;

          const change =
            prevPrice > 0
              ? Number(
                  (((newPrice - prevPrice) / prevPrice) * 100).toFixed(2)
                )
              : 0;

          const updated = {
            ...existing,
            lastPrice: newPrice,
            price: newPrice,
            prevPrice,
            change,
          };

          const newMap = { ...state.marketMap, [key]: updated };
          const newMarkets = Object.values(newMap);

          return {
            marketMap: newMap,
            markets: newMarkets,
            groupedMarkets: group(newMarkets),
          };
        });
      },

      /* ================= SELECT SYMBOL ================= */
      setSymbol: (s) => {
        if (!s) return;

        const key = normalize(s);

        // ✅ prevent unnecessary re-render
        if (get().selectedSymbol === key) return;

        set({ selectedSymbol: key });
      },

      /* ================= WATCHLIST ================= */
      setWatchlist: (list) => {
        if (!Array.isArray(list)) {
          set({ watchlist: [] });
          return;
        }

        set({
          watchlist: list.map(normalize),
        });
      },

      toggleWatch: (symbol) => {
        if (!symbol) return;

        set((state) => {
          const key = normalize(symbol);
          const exists = state.watchlist.includes(key);

          return {
            watchlist: exists
              ? state.watchlist.filter((s) => s !== key)
              : [...state.watchlist, key],
          };
        });
      },
    }),
    {
      name: "market-store",

      // ✅ persist only lightweight state
      partialize: (state) => ({
        watchlist: state.watchlist,
        selectedSymbol: state.selectedSymbol,
      }),
    }
  )
);