import { useEffect, useRef, useState } from "react";
import { useSocket } from "../../hooks/useSocket";
import api from "../../services/api";

const MAX_TRADES = 60;

const TradeFeed = ({ symbol }) => {
  const [trades, setTrades] = useState([]);
  const frameRef = useRef(null);
  const containerRef = useRef(null);

  /* ===============================
     📥 INITIAL REST FETCH
  =============================== */
  useEffect(() => {
    if (!symbol) return;
    let cancelled = false;

    api
      .get(`/trades?symbol=${symbol}`)
      .then((res) => {
        if (cancelled || !res.data?.data) return;
        const rawTrades = Array.isArray(res.data.data) ? res.data.data : [];
        setTrades(
          rawTrades.slice(0, MAX_TRADES).map((t) => ({
            ...t,
            id: t.tradeId || t.id || crypto.randomUUID(),
            side: t.side || "BUY",
            timestamp: t.timestamp ? new Date(t.timestamp).getTime() : Date.now(),
            flash: false,
          }))
        );
      })
      .catch((err) => console.warn("Trades fetch error:", err.message));

    return () => {
      cancelled = true;
    };
  }, [symbol]);

  /* ===============================
     📡 SOCKET (FIXED)
  =============================== */
  useSocket({
    symbol,
    event: "trade",
    handler: (trade) => {
      if (!trade) return;

      // 🔥 safety: normalize symbol
      const incomingSymbol = trade.symbol?.toUpperCase();
      if (incomingSymbol && incomingSymbol !== symbol?.toUpperCase())
        return;

      if (frameRef.current) cancelAnimationFrame(frameRef.current);

      frameRef.current = requestAnimationFrame(() => {
        setTrades((prev) => {
          const updated = [
            {
              ...trade,
              id: crypto.randomUUID(),

              // ✅ FIXED SIDE (backend should send this)
              side: trade.side || "BUY",

              // ✅ SAFE TIMESTAMP
              timestamp:
                typeof trade.timestamp === "number"
                  ? trade.timestamp
                  : Date.now(),

              flash: true,
            },
            ...prev,
          ].slice(0, MAX_TRADES);

          return updated;
        });
      });
    },
  });

  /* ===============================
     ⚡ FLASH RESET
  =============================== */
  useEffect(() => {
    if (trades.length === 0) return;

    const timer = setTimeout(() => {
      setTrades((prev) =>
        prev.map((t, i) =>
          i < 5 ? { ...t, flash: false } : t
        )
      );
    }, 120);

    return () => clearTimeout(timer);
  }, [trades]);

  /* ===============================
     🔝 AUTO SCROLL
  =============================== */
  useEffect(() => {
    if (!containerRef.current) return;
    containerRef.current.scrollTop = 0;
  }, [trades]);

  /* ===============================
     🧠 FORMATTERS
  =============================== */
  const format = (n) =>
    Number(n || 0).toLocaleString("en-IN", {
      maximumFractionDigits: 4,
    });

  // ✅ FIXED TIME FORMATTER
  const formatTime = (ts) => {
    const time = typeof ts === "number" ? ts : Date.now();
    const d = new Date(time);

    return d.toLocaleTimeString("en-IN", {
      hour12: false,
      minute: "2-digit",
      second: "2-digit",
    });
  };

  /* ===============================
     🧱 UI
  =============================== */
  return (
    <div
      className="
      h-full flex flex-col text-[11px] font-mono rounded-2xl overflow-hidden
      bg-white border border-gray-200
      dark:bg-black/30 dark:border-white/10 dark:backdrop-blur-xl
    "
    >
      {/* HEADER */}
      <div
        className="
        flex justify-between items-center px-3 py-2 text-xs
        text-gray-500 border-b border-gray-200
        dark:text-gray-400 dark:border-white/10
      "
      >
        <span>Recent Trades</span>
        <span className="text-[10px]">{symbol}</span>
      </div>

      {/* COLUMN HEADER */}
      <div
        className="
        grid grid-cols-3 px-3 py-1 text-[10px]
        text-gray-500 border-b border-gray-200
        dark:text-gray-400 dark:border-white/10
      "
      >
        <span>Price</span>
        <span className="text-center">Qty</span>
        <span className="text-right">Time</span>
      </div>

      {/* LIST */}
      <div ref={containerRef} className="flex-1 overflow-y-auto">
        {trades.length === 0 ? (
          <div className="text-center text-gray-500 py-4">
            Waiting for trades...
          </div>
        ) : (
          trades.map((t) => {
            const isBuy = t.side === "BUY";

            return (
              <div
                key={t.id}
                className={`
                  grid grid-cols-3 px-3 py-[5px]
                  transition-all duration-150
                  ${
                    t.flash
                      ? isBuy
                        ? "bg-green-100 dark:bg-green-500/10"
                        : "bg-red-100 dark:bg-red-500/10"
                      : "hover:bg-gray-100 dark:hover:bg-white/5"
                  }
                `}
              >
                {/* PRICE */}
                <span
                  className={`font-semibold ${
                    isBuy
                      ? "text-green-500 dark:text-green-400"
                      : "text-red-500 dark:text-red-400"
                  }`}
                >
                  ₹{format(t.price)}
                </span>

                {/* QTY */}
                <span className="text-center text-gray-500 dark:text-gray-400">
                  {format(t.quantity)}
                </span>

                {/* TIME */}
                <span className="text-right text-gray-500 dark:text-gray-400">
                  {formatTime(t.timestamp)}
                </span>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default TradeFeed;