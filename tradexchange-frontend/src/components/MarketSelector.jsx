import { useEffect, useMemo, useRef, useState } from "react";
import { socket } from "../services/socket";
import { useMarketStore } from "../store/marketStore";
import { motion } from "framer-motion";

const TABS = ["ALL", "WATCHLIST", "CRYPTO", "STOCK", "COMMODITY"];

const MarketSelector = () => {
  const {
    groupedMarkets,
    selectedSymbol,
    setSymbol,
    watchlist,
    toggleWatch,
    updateMarket,
  } = useMarketStore();

  const [search, setSearch] = useState("");
  const [tab, setTab] = useState("ALL");

  const [flashMap, setFlashMap] = useState({});
  const [priceMap, setPriceMap] = useState({});

  const tabRef = useRef(null);

  /* ===============================
     🔥 SOCKET (REALTIME LTP)
  =============================== */
  useEffect(() => {

    
    const handler = (data) => {
    if (!data?.symbol) return;

    const lastPrice = data.lastPrice ?? data.price;
    if (lastPrice == null) return;
      

      // 🔥 update ticker
      updateMarket(data.symbol, lastPrice);
    // ✅ get previous price SAFELY (no stale closure)
    setPriceMap((prevMap) => {
      const prev = prevMap[data.symbol];

      let type = "neutral";
      if (prev != null) {
        if (lastPrice > prev) type = "up";
        else if (lastPrice < prev) type = "down";
      }

      // 🔥 flash update
      setFlashMap((f) => ({
        ...f,
        [data.symbol]: type,
      }));

      setTimeout(() => {
        setFlashMap((f) => ({
          ...f,
          [data.symbol]: null,
        }));
      }, 200);

      return {
        ...prevMap,
        [data.symbol]: lastPrice,
      };
    });
  };

  socket.on("ticker", handler);

  return () => {
    socket.off("ticker", handler);
  };
}, [updateMarket]);
  /* ===============================
     📊 BASE DATA
  =============================== */
  const base = useMemo(() => {
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

  

  /* ===============================
     🔍 FILTER
  =============================== */
  const filtered = useMemo(() => {
    const q = search.toLowerCase();

    let list = q
      ? base.filter((m) =>
          m.symbol.toLowerCase().includes(q)
        )
      : base;

    return list.sort((a, b) => {
      const aFav = watchlist.includes(a.symbol);
      const bFav = watchlist.includes(b.symbol);
      return bFav - aFav;
    });
  }, [base, search, watchlist]);

  /* ===============================
     🔢 FORMAT
  =============================== */
  const format = (n) =>
    Number(n || 0).toLocaleString("en-IN", {
      maximumFractionDigits: 2,
    });

  /* ===============================
     👉 TAB SLIDER CONTROLS
  =============================== */
  const scrollTabs = (dir) => {
    if (!tabRef.current) return;

    tabRef.current.scrollBy({
      left: dir === "left" ? -100 : 100,
      behavior: "smooth",
    });
  };

  /* ===============================
     🎨 UI
  =============================== */
  return (
    <div className="flex flex-col h-full">

      {/* 🔍 SEARCH */}
      <div className="p-2">
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search markets..."
          className="w-full px-2 py-1 text-sm bg-gray-100 rounded"
        />
      </div>

{/* 📊 MODERN TAB SLIDER */}
<div className="px-2 mt-1 border-b relative">

  <div
    ref={tabRef}
    className="flex gap-4 overflow-x-auto no-scrollbar text-xs font-medium"
  >
    {TABS.map((t) => (
      <button
        key={t}
        onClick={(e) => {
          setTab(t);

          // 🔥 AUTO SCROLL TO CENTER
          e.currentTarget.scrollIntoView({
            behavior: "smooth",
            inline: "center",
            block: "nearest",
          });
        }}
        className={`relative pb-2 whitespace-nowrap transition-colors
          ${tab === t ? "text-blue-600" : "text-gray-500"}
        `}
      >
        {t}

        {/* 🔥 ACTIVE UNDERLINE */}
        {tab === t && (
          <motion.div
            layoutId="tab-indicator"
            className="absolute left-0 right-0 -bottom-[1px] h-[2px] bg-blue-600 rounded-full"
          />
        )}
      </button>
    ))}
  </div>
</div>

      {/* 📈 MARKET LIST */}
      <div className="flex-1 overflow-y-auto mt-2">
        {filtered.map((m) => {
          const active = selectedSymbol === m.symbol;
          const flash = flashMap[m.symbol];

          const price = m.lastPrice ?? m.price;

          return (
            <motion.div
              key={m.symbol}
              layout
              onClick={() => setSymbol(m.symbol)}
              className={`flex justify-between px-3 py-2 cursor-pointer transition-all
                ${active ? "bg-blue-200" : ""}
                ${flash === "up" ? "bg-green-100" : ""}
                ${flash === "down" ? "bg-red-100" : ""}
              `}
            >
              {/* LEFT */}
              <div className="flex items-center gap-2">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    toggleWatch(m.symbol);
                  }}
                >
                  {watchlist.includes(m.symbol) ? "★" : "☆"}
                </button>

                <div className="flex flex-col">
                  <span className="font-medium text-sm">
                    {m.symbol}
                  </span>

                  <span
                    className={`text-xs ${
                      m.change > 0
                        ? "text-green-600"
                        : m.change < 0
                        ? "text-red-600"
                        : "text-gray-400"
                    }`}
                  >
                    {m.change > 0 ? "+" : ""}
                    {m.change || 0}%
                  </span>
                </div>
              </div>

              {/* RIGHT */}
              <div className="text-right">
                <div className="text-[10px] text-gray-400">
                  LTP
                </div>

                <div
                  className={`text-sm font-medium ${
                    flash === "up"
                      ? "text-green-600"
                      : flash === "down"
                      ? "text-red-600"
                      : ""
                  }`}
                >
                  ₹ {format(price)}
                </div>
              </div>
            </motion.div>
          );
        })}
      </div>
    </div>
  );
};

export default MarketSelector;