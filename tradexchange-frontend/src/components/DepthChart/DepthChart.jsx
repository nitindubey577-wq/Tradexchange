// ===============================
// 📁 src/components/DepthChart/DepthChart.jsx (PRO FINAL)
// ===============================

import { useEffect, useRef, useState } from "react";
import { createChart } from "lightweight-charts";
import { useSocket } from "../../hooks/useSocket";
import { useThemeStore } from "../../theme/themeStore";

const MAX_LEVELS = 30;

const DepthChart = ({ symbol }) => {
  const chartRef = useRef(null);
  const chartInstanceRef = useRef(null);
  const bidSeriesRef = useRef(null);
  const askSeriesRef = useRef(null);
  const frameRef = useRef(null);

  const { theme } = useThemeStore();
  const isDark = theme === "dark";

  const [spread, setSpread] = useState("--");

  /* ===============================
     📊 INIT CHART
  =============================== */
  useEffect(() => {
    if (!chartRef.current) return;

    const chart = createChart(chartRef.current, {
      layout: {
        background: { color: isDark ? "#0f172a" : "#ffffff" },
        textColor: isDark ? "#9CA3AF" : "#374151",
        fontSize: 11,
      },
      grid: {
        vertLines: { color: isDark ? "#1f2937" : "#e5e7eb" },
        horzLines: { color: isDark ? "#1f2937" : "#e5e7eb" },
      },
      width: chartRef.current.clientWidth,
      height: chartRef.current.clientHeight,
      timeScale: { visible: false },
      rightPriceScale: { borderVisible: false },
      crosshair: { mode: 0 },
    });

    chartInstanceRef.current = chart;

    bidSeriesRef.current = chart.addAreaSeries({
      lineColor: "#22c55e",
      topColor: "rgba(34,197,94,0.35)",
      bottomColor: "rgba(34,197,94,0.02)",
      lineWidth: 2,
    });

    askSeriesRef.current = chart.addAreaSeries({
      lineColor: "#ef4444",
      topColor: "rgba(239,68,68,0.35)",
      bottomColor: "rgba(239,68,68,0.02)",
      lineWidth: 2,
    });

    const resizeObserver = new ResizeObserver(([entry]) => {
      const { width, height } = entry.contentRect;
      chart.applyOptions({ width, height });
    });

    resizeObserver.observe(chartRef.current);

    return () => {
      resizeObserver.disconnect();
      chart.remove();
      if (frameRef.current) cancelAnimationFrame(frameRef.current);
    };
  }, []);

  /* ===============================
     🎨 THEME UPDATE
  =============================== */
  useEffect(() => {
    const chart = chartInstanceRef.current;
    if (!chart) return;

    chart.applyOptions({
      layout: {
        background: { color: isDark ? "#0f172a" : "#ffffff" },
        textColor: isDark ? "#9CA3AF" : "#374151",
      },
      grid: {
        vertLines: { color: isDark ? "#1f2937" : "#e5e7eb" },
        horzLines: { color: isDark ? "#1f2937" : "#e5e7eb" },
      },
    });
  }, [isDark]);

  /* ===============================
     📡 SOCKET
  =============================== */
  useSocket({
    symbol,
    event: "depth",
    handler: (data) => {
      if (!data?.bids?.length || !data?.asks?.length) return;

      if (frameRef.current) cancelAnimationFrame(frameRef.current);

      frameRef.current = requestAnimationFrame(() => {
        if (!bidSeriesRef.current || !askSeriesRef.current) return;

        // ✅ SORT CORRECTLY
        const bids = data.bids
          .slice(0, MAX_LEVELS)
          .map((b) => ({
            price: Number(b.price),
            quantity: Number(b.quantity),
          }))
          .sort((a, b) => b.price - a.price);

        const asks = data.asks
          .slice(0, MAX_LEVELS)
          .map((a) => ({
            price: Number(a.price),
            quantity: Number(a.quantity),
          }))
          .sort((a, b) => a.price - b.price);

        const bestBid = bids[0]?.price;
        const bestAsk = asks[0]?.price;

        if (bestBid !== undefined && bestAsk !== undefined) {
          setSpread((bestAsk - bestBid).toFixed(2));
        }

        // ✅ DEPTH CURVE USING PRICE
        let bidTotal = 0;
        const bidData = bids
          .slice()
          .reverse()
          .map((b) => {
            bidTotal += b.quantity;
            return {
              time: b.price,
              value: bidTotal,
            };
          });

        let askTotal = 0;
        const askData = asks.map((a) => {
          askTotal += a.quantity;
          return {
            time: a.price,
            value: askTotal,
          };
        });

        bidSeriesRef.current.setData(bidData);
        askSeriesRef.current.setData(askData);

        chartInstanceRef.current.timeScale().fitContent();
      });
    },
  });

  /* ===============================
     🧱 UI
  =============================== */
  return (
    <div className="relative w-full h-full min-h-[240px] rounded-2xl overflow-hidden">
      {/* HEADER */}
      <div className="absolute top-3 left-3 right-3 flex justify-between z-10 px-3 py-1.5 rounded-xl backdrop-blur-md bg-white/80 dark:bg-black/20 border border-gray-200 dark:border-white/10">
        <span className="text-xs text-gray-600 dark:text-gray-400">
          Order Book Depth
        </span>

        <span className="text-xs font-medium text-gray-700 dark:text-gray-300">
          Spread: {spread === "--" ? "--" : `₹${spread}`}
        </span>
      </div>

      <div ref={chartRef} className="w-full h-full" />

      {/* LEGEND */}
      <div className="absolute bottom-3 left-3 flex gap-4 text-[11px] px-3 py-1 rounded-lg backdrop-blur-md bg-white/80 dark:bg-black/20 border border-gray-200 dark:border-white/10">
        <span className="text-green-500">● Bids</span>
        <span className="text-red-500">● Asks</span>
      </div>
    </div>
  );
};

export default DepthChart;