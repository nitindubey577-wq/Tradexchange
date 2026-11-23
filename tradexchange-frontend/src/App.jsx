// ===============================
// 📁 src/App.js (FINAL CLEAN PRO)
// ===============================

import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import { lazy, Suspense, useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";

import MainLayout from "./layouts/MainLayout";
import { useMarketStore } from "./store/marketStore";
import { fetchMarkets } from "./services/marketService";
import { getWatchlist } from "./services/userService";
import { initSocketHandlers } from "./services/socketHandler";
import { MARKETS } from "./constants/markets";
import { socket } from "./services/socket";

/* ================= HELPERS ================= */
const normalize = (s) => s?.replace("/", "-").toUpperCase();

/* ================= LAZY PAGES ================= */
const Dashboard = lazy(() => import("./pages/Dashboard"));
const Markets = lazy(() => import("./pages/Markets"));
const Portfolio = lazy(() => import("./pages/Portfolio"));

/* ================= FALLBACK ================= */
const DEFAULT_MARKETS = MARKETS?.length
  ? MARKETS.map((m, i) => ({
      id: i + 1,
      symbol: normalize(m.name),
      price: Number(m.price) || 0,
      change: Number(m.change) || 0,
      category: (m.category || "other").toLowerCase(),
    }))
  : [];

/* ================= UI ================= */
const Loader = () => (
  <div className="min-h-screen flex items-center justify-center text-sm">
    Loading...
  </div>
);

const ErrorScreen = ({ message }) => (
  <div className="min-h-screen flex items-center justify-center text-red-500 text-sm">
    {message}
  </div>
);

/* ================= ROUTES ================= */
const AnimatedRoutes = () => {
  const location = useLocation();

  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={location.pathname}
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -8 }}
        transition={{ duration: 0.2 }}
        className="h-full"
      >
        <Routes location={location}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/markets" element={<Markets />} />
          <Route path="/portfolio" element={<Portfolio />} />
        </Routes>
      </motion.div>
    </AnimatePresence>
  );
};

/* ================= APP ================= */
const App = () => {
  const setMarkets = useMarketStore((s) => s.setMarkets);
  const updateMarket = useMarketStore((s) => s.updateMarket);
  const setWatchlist = useMarketStore((s) => s.setWatchlist);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  /* ================= INIT ================= */
  useEffect(() => {
    let isMounted = true;

    const initApp = async () => {
      try {
        const [marketsRes, watchlistRes] = await Promise.all([
          fetchMarkets(),
          getWatchlist(),
        ]);

        if (!isMounted) return;

        const safeMarkets =
          Array.isArray(marketsRes) && marketsRes.length
            ? marketsRes
            : DEFAULT_MARKETS;

        console.log("📦 Markets Loaded:", safeMarkets.length);

        setMarkets(safeMarkets);
        setWatchlist(watchlistRes || []);

        // ✅ Initialize socket listeners
        initSocketHandlers();
      } catch (err) {
        console.error("❌ Init Error:", err);

        if (isMounted) {
          setError("Failed to load app data");
          setMarkets(DEFAULT_MARKETS);
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    initApp();

    return () => {
      isMounted = false;
    };
  }, [setMarkets, setWatchlist]);

  /* ================= REAL-TIME TICKER ================= */
  useEffect(() => {
    const handleTicker = (data) => {
      if (!data?.symbol) return;

      updateMarket(data.symbol, data.price);
    };

    socket.on("ticker", handleTicker);

    return () => {
      socket.off("ticker", handleTicker);
    };
  }, [updateMarket]);

  /* ================= SOCKET CONNECT ================= */
  useEffect(() => {
    const handleConnect = () => {
      console.log("✅ Socket Connected");
    };

    socket.on("connect", handleConnect);

    return () => {
      socket.off("connect", handleConnect);
    };
  }, []);

  /* ================= STATES ================= */
  if (loading) return <Loader />;
  if (error) return <ErrorScreen message={error} />;

  /* ================= RENDER ================= */
  return (
    <BrowserRouter>
      <MainLayout>
        <Suspense fallback={<Loader />}>
          <AnimatedRoutes />
        </Suspense>
      </MainLayout>
    </BrowserRouter>
  );
};

export default App;