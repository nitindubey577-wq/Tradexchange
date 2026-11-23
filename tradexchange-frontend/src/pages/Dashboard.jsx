// ===============================
// 📁 src/pages/Dashboard.jsx (FINAL PRO)
// ===============================

import { useMemo } from "react";

import CandleChart from "../components/Chart/CandleChart";
import OrderBook from "../components/OrderBook/OrderBook";
import TradeFeed from "../components/TradeFeed/TradeFeed";
import OrderForm from "../components/OrderForm/OrderForm";
import OrdersTable from "../components/OrdersTable/OrdersTable";
import DepthChart from "../components/DepthChart/DepthChart";
import MarketSelector from "../components/MarketSelector";
import Card from "../components/common/Card";

import { useMarketStore } from "../store/marketStore";

const Dashboard = () => {
  const { selectedSymbol, getSelectedMarket } = useMarketStore();

  /* ===============================
     🔥 SAFE SYMBOL
  =============================== */
  const symbol = useMemo(
    () => selectedSymbol || "BTC-INR",
    [selectedSymbol]
  );

  /* ===============================
     📊 SELECTED MARKET DATA
  =============================== */
  const market = useMemo(() => getSelectedMarket(), [
    selectedSymbol,
    getSelectedMarket,
  ]);

  const price = market?.price || 0;
  const change = market?.change || 0;
  const isUp = change >= 0;

  const formatPrice = (n) =>
    Number(n).toLocaleString("en-IN", {
      maximumFractionDigits: 2,
    });

  /* ===============================
     🧱 UI
  =============================== */

  return (
    <div className="h-full flex overflow-hidden bg-white text-gray-900 dark:bg-[#0b1220] dark:text-white">

      {/* ================= LEFT SIDEBAR ================= */}
      <aside className="hidden lg:flex w-[260px] p-3 min-h-0 border-r border-gray-200 dark:border-white/10">
        <div className="flex flex-col gap-3 w-full min-h-0">
          <h2 className="text-sm text-gray-500 dark:text-gray-400 shrink-0">
            Watchlist
          </h2>

          <div className="flex-1 min-h-0 overflow-auto">
            <MarketSelector />
          </div>
        </div>
      </aside>

      {/* ================= MAIN ================= */}
      <main className="flex-1 flex flex-col min-w-0 min-h-0">

        {/* ================= HEADER ================= */}
        <header className="h-[56px] flex items-center justify-between px-4 shrink-0 border-b border-gray-200 dark:border-white/10">

          {/* LEFT */}
          <div className="flex items-center gap-4">

            <div>
              <h1 className="text-lg font-semibold">{symbol}</h1>
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Live Market
              </p>
            </div>

            {/* 🔥 PRICE PANEL */}
            <div className="hidden sm:flex flex-col">
              <span className="text-sm font-semibold">
                ₹ {formatPrice(price)}
              </span>

              <span
                className={`text-xs ${
                  isUp ? "text-green-500" : "text-red-500"
                }`}
              >
                {change.toFixed(2)}%
              </span>
            </div>
          </div>

          {/* RIGHT */}
          <div className="text-xs text-gray-500 dark:text-gray-400">
            Spot • Real-time
          </div>
        </header>

        {/* ================= GRID ================= */}
        <section
          className="
          flex-1 min-h-0 grid
          grid-cols-1 
          lg:grid-cols-[minmax(0,1fr)_minmax(360px,420px)]
          grid-rows-[minmax(0,1fr)_minmax(0,320px)]
          gap-[1px]
          bg-gray-200 dark:bg-white/5
        "
        >

          {/* ================= CHART ================= */}
          <div className="p-2 min-w-0 min-h-0">
            <Card className="h-full min-h-0 overflow-hidden">
              <CandleChart symbol={symbol} />
            </Card>
          </div>

          {/* ================= RIGHT PANEL ================= */}
          <div className="flex flex-col min-h-0">

            {/* ORDER BOOK */}
            <div className="flex-1 min-h-0 p-2 border-b border-gray-200 dark:border-white/10">
              <Card className="h-full min-h-0 overflow-auto">
                <OrderBook symbol={symbol} />
              </Card>
            </div>

            {/* TRADE FEED */}
            <div className="flex-1 min-h-0 p-2">
              <Card className="h-full min-h-0 overflow-auto">
                <TradeFeed symbol={symbol} />
              </Card>
            </div>
          </div>

          {/* ================= DEPTH ================= */}
          <div className="p-2 min-h-0">
            <Card className="h-full min-h-0 overflow-hidden">
              <DepthChart symbol={symbol} />
            </Card>
          </div>

          {/* ================= ORDER FORM + TABLE ================= */}
          <div className="flex flex-col min-h-0">

            {/* ORDER FORM */}
            <div className="min-h-0 p-2 flex-[0_0_55%] border-b border-gray-200 dark:border-white/10">
              <Card className="h-full min-h-0 overflow-auto">
                <OrderForm symbol={symbol} />
              </Card>
            </div>

            {/* ORDERS TABLE */}
            <div className="flex-1 min-h-0 p-2">
              <Card className="h-full min-h-0 overflow-auto">
                <OrdersTable />
              </Card>
            </div>

          </div>

        </section>
      </main>
    </div>
  );
};

export default Dashboard;