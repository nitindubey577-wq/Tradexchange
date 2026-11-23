import { useState, useMemo } from "react";
import { motion } from "framer-motion";
import { Search, Star } from "lucide-react";
import { useMarketStore } from "../store/marketStore";
import { subscribeSymbol, unsubscribeSymbol } from "../services/socket";
const TABS = ["ALL", "CRYPTO", "STOCK", "COMMODITY", "WATCHLIST"];

const Markets = () => {
  const {
    groupedMarkets,
    watchlist,
    setSymbol,
    toggleWatch, // 🔥 add this in store if missing
  } = useMarketStore();

  const [search, setSearch] = useState("");
  const [tab, setTab] = useState("ALL");

  /* ---------------- BASE DATA ---------------- */
  const baseMarkets = useMemo(() => {
    switch (tab) {
      case "CRYPTO":
        return groupedMarkets.crypto;
      case "STOCK":
        return groupedMarkets.stock;
      case "COMMODITY":
        return groupedMarkets.commodity;
      case "WATCHLIST":
        return groupedMarkets.all.filter((m) =>
          watchlist.includes(m.symbol)
        );
      default:
        return groupedMarkets.all;
    }
  }, [tab, groupedMarkets, watchlist]);

  /* ---------------- FILTER (FAST) ---------------- */
  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();

    let list = q
      ? baseMarkets.filter((m) =>
          m.symbol.toLowerCase().includes(q)
        )
      : baseMarkets;

    // ⭐ watchlist priority
    return list.sort((a, b) => {
      const aFav = watchlist.includes(a.symbol);
      const bFav = watchlist.includes(b.symbol);
      return bFav - aFav;
    });
  }, [baseMarkets, search, watchlist]);

  const formatPrice = (n) =>
    Number(n || 0).toLocaleString("en-IN", {
      maximumFractionDigits: 2,
    });

  /* ---------------- UI ---------------- */

  return (
    <div className="h-full flex flex-col gap-4 p-4 bg-white text-gray-900 dark:bg-[#0b1220] dark:text-white">

      {/* 🔝 HEADER */}
      <div>
        <h1 className="text-xl font-semibold">Markets</h1>
        <p className="text-gray-500 text-sm">
          100+ Assets • Crypto • Stocks • Commodities
        </p>
      </div>

      {/* 📂 TABS */}
      <div className="flex gap-2 overflow-x-auto">
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-3 py-1 text-xs rounded-md transition ${
              tab === t
                ? "bg-blue-600 text-white"
                : "bg-gray-100 dark:bg-white/10 text-gray-500"
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {/* 🔍 SEARCH */}
      <div className="relative max-w-md">
        <Search size={16} className="absolute left-3 top-2.5 text-gray-400" />
        <input
          placeholder="Search BTC, RELIANCE, GOLD..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-9 pr-3 py-2 rounded-xl bg-gray-100 dark:bg-white/5 border text-sm"
        />
      </div>

      {/* 📊 TABLE */}
      <div className="flex-1 rounded-2xl bg-white dark:bg-black/30 border overflow-hidden">

        {/* HEADER */}
        <div className="hidden md:grid grid-cols-4 px-4 py-2 text-[11px] text-gray-500 border-b">
          <span>Symbol</span>
          <span>Price</span>
          <span className="text-right">Change</span>
          <span></span>
        </div>

        {/* ROWS */}
        <div className="divide-y overflow-y-auto h-full">
          {filtered.map((m, i) => {
            const isUp = (m.change || 0) >= 0;
            const isFav = watchlist.includes(m.symbol);

            return (
              <motion.div
                key={m.symbol}
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: Math.min(i * 0.005, 0.2) }}
                className="grid grid-cols-3 md:grid-cols-4 px-4 py-3 items-center hover:bg-gray-100 dark:hover:bg-white/5"
              >
                {/* SYMBOL */}
                <div
                  onClick={() => setSymbol(m.symbol)}
                  className="font-medium cursor-pointer"
                >
                  {m.symbol}
                </div>

                {/* PRICE */}
                <div className="font-mono text-sm">
                  {m.price ? `₹ ${formatPrice(m.price)}` : "--"}
                </div>

                {/* CHANGE */}
                <div
                  className={`text-right text-sm font-semibold ${
                    isUp ? "text-green-500" : "text-red-500"
                  }`}
                >
                  {m.change ? `${m.change.toFixed(2)}%` : ""}
                </div>

                {/* ⭐ WATCHLIST */}
                <div className="text-right">
                  <button
                    onClick={() => toggleWatch(m.symbol)}
                    className="p-1"
                  >
                    <Star
                      size={14}
                      className={
                        isFav
                          ? "text-yellow-400 fill-yellow-400"
                          : "text-gray-400"
                      }
                    />
                  </button>
                </div>
              </motion.div>
            );
          })}
        </div>
      </div>

      {/* EMPTY */}
      {filtered.length === 0 && (
        <div className="text-center text-gray-500 text-sm py-6">
          No markets found
        </div>
      )}
    </div>
  );
};

export default Markets;