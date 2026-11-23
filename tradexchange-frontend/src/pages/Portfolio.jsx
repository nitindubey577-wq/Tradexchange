import { motion } from "framer-motion";
import { useMarketStore } from "../store/marketStore";
import { useMemo } from "react";

const Portfolio = () => {
  const { markets } = useMarketStore();

  const holdings = [
    { symbol: "BTC-INR", qty: 1.2, avg: 90000 },
    { symbol: "ETH-INR", qty: 5, avg: 5000 },
  ];

  const enrichedHoldings = useMemo(() => {
    return holdings.map((h) => {
      const market = markets.find(
        (m) => m.symbol === h.symbol
      );

      const current = market?.price || 0;
      const pnl = (current - h.avg) * h.qty;

      const pnlPercent =
        h.avg > 0 ? ((current - h.avg) / h.avg) * 100 : 0;

      return { ...h, current, pnl, pnlPercent };
    });
  }, [markets]);

  const totalValue = enrichedHoldings.reduce(
    (acc, h) => acc + h.current * h.qty,
    0
  );

  const totalInvestment = enrichedHoldings.reduce(
    (acc, h) => acc + h.avg * h.qty,
    0
  );

  const totalPnL = totalValue - totalInvestment;

  const format = (n) =>
    Number(n).toLocaleString("en-IN", {
      maximumFractionDigits: 2,
    });

  return (
    <div className="h-full flex flex-col gap-4 p-4 bg-white text-gray-900 dark:bg-[#0b1220] dark:text-white">

      {/* 🔝 HEADER */}
      <div>
        <h1 className="text-xl font-semibold">Portfolio</h1>
        <p className="text-gray-600 dark:text-gray-400 text-sm">
          Your holdings & performance
        </p>
      </div>

      {/* 🔥 SUMMARY */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">

        {/* CARD */}
        <div className="bg-white dark:bg-black/30 border border-gray-200 dark:border-white/10 backdrop-blur-xl rounded-2xl p-4">
          <div className="text-xs text-gray-500 dark:text-gray-400">
            Total Value
          </div>
          <div className="font-semibold mt-1 text-lg">
            ₹ {format(totalValue)}
          </div>
        </div>

        {/* CARD */}
        <div className="bg-white dark:bg-black/30 border border-gray-200 dark:border-white/10 backdrop-blur-xl rounded-2xl p-4">
          <div className="text-xs text-gray-500 dark:text-gray-400">
            Investment
          </div>
          <div className="font-semibold mt-1 text-lg">
            ₹ {format(totalInvestment)}
          </div>
        </div>

        {/* CARD */}
        <div className="bg-white dark:bg-black/30 border border-gray-200 dark:border-white/10 backdrop-blur-xl rounded-2xl p-4">
          <div className="text-xs text-gray-500 dark:text-gray-400">
            PnL
          </div>
          <div
            className={`font-semibold mt-1 text-lg ${
              totalPnL >= 0
                ? "text-green-500"
                : "text-red-500"
            }`}
          >
            ₹ {format(totalPnL)}
          </div>
        </div>

      </div>

      {/* 📊 TABLE */}
      <div className="flex-1 rounded-2xl bg-white dark:bg-black/30 backdrop-blur-xl border border-gray-200 dark:border-white/10 overflow-hidden">

        {/* HEADER */}
        <div className="hidden md:grid grid-cols-6 text-[11px] text-gray-500 dark:text-gray-400 px-4 py-2 border-b border-gray-200 dark:border-white/10">
          <span>Symbol</span>
          <span>Qty</span>
          <span>Avg</span>
          <span>Current</span>
          <span>PnL</span>
          <span className="text-right">%</span>
        </div>

        {/* ROWS */}
        <div className="divide-y divide-gray-100 dark:divide-white/5">
          {enrichedHoldings.map((h, i) => {
            const isProfit = h.pnl >= 0;

            return (
              <motion.div
                key={h.symbol}
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.03 }}
                className="
                  grid grid-cols-2 md:grid-cols-6
                  px-4 py-3 items-center
                  hover:bg-gray-100 dark:hover:bg-white/5
                  transition
                "
              >
                {/* SYMBOL */}
                <div className="font-medium">
                  {h.symbol}
                </div>

                {/* QTY */}
                <div className="text-gray-600 dark:text-gray-400 text-sm">
                  {h.qty}
                </div>

                {/* AVG */}
                <div className="text-gray-600 dark:text-gray-400 text-sm">
                  ₹ {format(h.avg)}
                </div>

                {/* CURRENT */}
                <div className="text-sm">
                  {h.current
                    ? `₹ ${format(h.current)}`
                    : "--"}
                </div>

                {/* PNL */}
                <div
                  className={`text-sm font-semibold ${
                    isProfit
                      ? "text-green-500"
                      : "text-red-500"
                  }`}
                >
                  ₹ {format(h.pnl)}
                </div>

                {/* % */}
                <div
                  className={`text-sm font-semibold text-right ${
                    isProfit
                      ? "text-green-500"
                      : "text-red-500"
                  }`}
                >
                  {h.pnlPercent.toFixed(2)}%
                </div>
              </motion.div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default Portfolio;