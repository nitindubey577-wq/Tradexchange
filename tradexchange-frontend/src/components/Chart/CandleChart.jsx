// ===============================
// 📁 src/components/Chart/CandleChart.jsx (FINAL)
// ===============================

import { useEffect, useRef, useState } from "react";
import { createChart, CrosshairMode } from "lightweight-charts";
import { getCandles } from "../../services/api";
import { useSocket } from "../../hooks/useSocket";
import { useThemeStore } from "../../theme/themeStore";

const CandleChart = ({ symbol }) => {
  const chartRef = useRef(null);
  const chartInstanceRef = useRef(null);
  const candleSeriesRef = useRef(null);
  const lastCandleRef = useRef(null);
  const frameRef = useRef(null);

  const { theme } = useThemeStore();

  const [loading, setLoading] = useState(true);
  const [price, setPrice] = useState(null);
  const [flash, setFlash] = useState("");

  const isDark = theme === "dark";

  /* ===============================
     📊 INIT CHART
  =============================== */
  useEffect(() => {
    if (!chartRef.current) return;

    const chart = createChart(chartRef.current, {
      layout: {
        background: { color: isDark ? "#0f172a" : "#ffffff" },
        textColor: isDark ? "#9CA3AF" : "#374151",
        fontSize: 12,
      },
      grid: {
        vertLines: { color: isDark ? "#1f2937" : "#e5e7eb" },
        horzLines: { color: isDark ? "#1f2937" : "#e5e7eb" },
      },
      width: chartRef.current.clientWidth,
      height: chartRef.current.clientHeight,
      crosshair: {
        mode: CrosshairMode.Normal,
      },
      timeScale: {
        timeVisible: true,
        secondsVisible: false,
      },
      rightPriceScale: {
        borderColor: "transparent",
      },
    });

    chartInstanceRef.current = chart;

    candleSeriesRef.current = chart.addCandlestickSeries({
      upColor: "#22c55e",
      downColor: "#ef4444",
      wickUpColor: "#22c55e",
      wickDownColor: "#ef4444",
      borderVisible: false,
    });

    const resizeObserver = new ResizeObserver((entries) => {
      const { width, height } = entries[0].contentRect;
      chart.applyOptions({ width, height });
    });

    resizeObserver.observe(chartRef.current);

    return () => {
      resizeObserver.disconnect();
      chart.remove();
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
     📥 INITIAL DATA
  =============================== */
  useEffect(() => {
    if (!symbol) return;

    const load = async () => {
      try {
        setLoading(true);

        const res = await getCandles(symbol);

        if (!res?.candles?.length) {
          console.warn("⚠️ No candles data");
          return;
        }

        const data = res.candles.map((c) => ({
          time: Math.floor(new Date(c.time).getTime() / 1000),
          open: Number(c.open),
          high: Number(c.high),
          low: Number(c.low),
          close: Number(c.close),
        }));

        candleSeriesRef.current?.setData(data);

        const last = data[data.length - 1];
        lastCandleRef.current = last;
        setPrice(last.close);
      } catch (err) {
        console.error("❌ Chart load error:", err);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [symbol]);

  /* ===============================
     📡 SOCKET (FIXED)
  =============================== */
  useSocket({
    symbol,
    event: "candle",
    handler: (data) => {
      const c = data?.candle;
      if (!c) return;

      if (frameRef.current) cancelAnimationFrame(frameRef.current);

      frameRef.current = requestAnimationFrame(() => {
        const candle = {
          time: Math.floor(new Date(c.time).getTime() / 1000),
          open: Number(c.open),
          high: Number(c.high),
          low: Number(c.low),
          close: Number(c.close),
        };

        candleSeriesRef.current?.update(candle);

        const prev = lastCandleRef.current?.close;

        if (prev !== undefined) {
          if (candle.close > prev) setFlash("text-green-500");
          else if (candle.close < prev) setFlash("text-red-500");

          setTimeout(() => setFlash(""), 150);
        }

        lastCandleRef.current = candle;
        setPrice(candle.close);
      });
    },
  });

  /* ===============================
     🧱 UI
  =============================== */

  return (
    <div className="relative w-full h-full rounded-2xl overflow-hidden">

      {/* 🔝 TOP BAR */}
      <div className="absolute top-3 left-4 z-10 flex gap-3 px-3 py-1.5 rounded-xl backdrop-blur-md bg-white/80 dark:bg-black/20 border border-gray-200 dark:border-white/10">
        <span className="text-xs text-gray-600 dark:text-gray-400">
          {symbol}
        </span>

        {price && (
          <span className={`text-sm font-semibold ${flash}`}>
            ₹{price.toLocaleString("en-IN")}
          </span>
        )}
      </div>

      {/* 🔄 LOADING */}
      {loading && (
        <div className="absolute inset-0 flex items-center justify-center text-sm bg-white/70 dark:bg-black/20 backdrop-blur-sm">
          Loading chart...
        </div>
      )}

      {/* 📈 CHART */}
      <div ref={chartRef} className="w-full h-full" />
    </div>
  );
};

export default CandleChart;