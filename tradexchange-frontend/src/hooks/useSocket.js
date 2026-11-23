// ===============================
// 📁 src/hooks/useSocket.js (PRO MAX)
// ===============================

import { useEffect, useRef } from "react";
import {
  socket,
  subscribeSymbol,
  unsubscribeSymbol,
} from "../services/socket";

/* ===============================
   🧠 GLOBAL SUBSCRIPTION TRACKER
=============================== */
const activeSubscriptions = new Map();

/* ===============================
   🔧 NORMALIZER
=============================== */
const normalize = (s) => s?.toUpperCase();

/* ===============================
   🚀 HOOK
=============================== */
export const useSocket = ({
  symbol,
  event = "trade",
  handler,
  enabled = true,
  debug = false,
}) => {
  const handlerRef = useRef(handler);

  /* 🔄 ALWAYS KEEP LATEST HANDLER */
  useEffect(() => {
    handlerRef.current = handler;
  }, [handler]);

  useEffect(() => {
    if (!enabled || !symbol || !event) return;

    const sym = normalize(symbol);
    if (!sym) return;

    /* ===============================
       🧠 WRAPPED HANDLER
    =============================== */
    const wrappedHandler = (data) => {
      if (!data || data.symbol !== sym) return;

      if (debug) {
        console.log(`📡 ${event} ->`, data);
      }

      try {
        handlerRef.current?.(data);
      } catch (err) {
        console.error("❌ Socket handler error:", err);
      }
    };

    /* ===============================
       🔁 SUBSCRIPTION COUNTING
    =============================== */
    const count = activeSubscriptions.get(sym) || 0;

    if (count === 0) {
      subscribeSymbol(sym);
      if (debug) console.log("📡 Subscribed:", sym);
    }

    activeSubscriptions.set(sym, count + 1);

    /* ===============================
       🎧 LISTEN
    =============================== */
    socket.on(event, wrappedHandler);

    /* ===============================
       🧹 CLEANUP
    =============================== */
    return () => {
      socket.off(event, wrappedHandler);

      const current = activeSubscriptions.get(sym) || 1;

      if (current <= 1) {
        unsubscribeSymbol(sym);
        activeSubscriptions.delete(sym);

        if (debug) console.log("❌ Unsubscribed:", sym);
      } else {
        activeSubscriptions.set(sym, current - 1);
      }
    };
  }, [symbol, event, enabled, debug]);
};