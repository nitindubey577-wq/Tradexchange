import { NavLink, useLocation } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import { Sun, Moon, BarChart2, Briefcase, Layers } from "lucide-react";
import { useThemeStore } from "../theme/themeStore";
import { useMarketStore } from "../store/marketStore";
import { memo, useState, useEffect } from "react";
import clsx from "clsx";

/* COMPONENTS */
import MarketSelector from "../components/MarketSelector";
import CandleChart from "../components/Chart/CandleChart";
import OrderBook from "../components/OrderBook/OrderBook";
import OrderForm from "../components/OrderForm/OrderForm";
import OrdersTable from "../components/OrdersTable/OrdersTable";
import DepthChart from "../components/DepthChart/DepthChart";

const navItems = [
  { name: "Trade", path: "/", icon: BarChart2 },
  { name: "Markets", path: "/markets", icon: Layers },
  { name: "Portfolio", path: "/portfolio", icon: Briefcase },
];

const MainLayout = ({ children }) => {
  const { theme, toggleTheme } = useThemeStore();
  const { selectedSymbol, setSymbol } = useMarketStore();
  const location = useLocation();

  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [showWatchlist, setShowWatchlist] = useState(true);

  /* ✅ Ensure default symbol */
  useEffect(() => {
    if (!selectedSymbol) setSymbol("BTC-INR");
  }, [selectedSymbol]);

  const isTradePage = location.pathname === "/";

  return (
    <div className="h-[100dvh] flex overflow-hidden bg-white text-gray-900 dark:bg-[#0b1220] dark:text-white">

      {/* 🖥 SIDEBAR */}
      <motion.div
        animate={{ width: collapsed ? 70 : 240 }}
        className="hidden md:flex flex-col border-r border-gray-200 dark:border-white/10 bg-white/80 dark:bg-black/30 backdrop-blur-xl"
      >
        <div className="h-14 flex items-center justify-between px-4 border-b">
          {!collapsed && <span className="font-bold text-blue-500">TradeX</span>}
          <button onClick={() => setCollapsed(!collapsed)}>☰</button>
        </div>

        <div className="flex-1 p-2 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink key={item.name} to={item.path}>
                {({ isActive }) => (
                  <div
                    className={clsx(
                      "flex items-center gap-3 px-3 py-2 rounded-lg text-sm",
                      isActive
                        ? "bg-blue-500 text-white"
                        : "text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-white/5"
                    )}
                  >
                    <Icon size={18} />
                    {!collapsed && item.name}
                  </div>
                )}
              </NavLink>
            );
          })}
        </div>

        
       
      </motion.div>

      {/* 🚀 MAIN */}
      <div className="flex-1 flex flex-col min-h-0 overflow-hidden">

        {/* 🔝 TOP BAR */}
        <div className="h-12 flex items-center justify-between px-4 border-b">
          <span className="text-sm">{selectedSymbol}</span>
          <button onClick={toggleTheme}>
            {theme === "dark" ? <Sun /> : <Moon />}
          </button>
        </div>

        {/* ✅ TRADE PAGE */}
        {isTradePage ? (
          <div className="flex-1 flex min-h-0 overflow-hidden">

            {/* WATCHLIST */}
            <div
              className={clsx(
                "border-r flex flex-col transition-all",
                showWatchlist ? "w-[260px]" : "w-[60px]"
              )}
            >
              <div className="flex justify-between p-2 border-b">
                {showWatchlist && "Watchlist"}
                <button onClick={() => setShowWatchlist(!showWatchlist)}>
                  ⟨
                </button>
              </div>

              <div className="flex-1 min-h-0 overflow-hidden">
                {showWatchlist && <MarketSelector />}
              </div>
            </div>

            {/* TRADING AREA */}
            <div className="flex-1 grid grid-rows-[65%_35%] min-h-0">

               {/* 🔥 CHART + DEPTH */}
               <div className="grid grid-cols-1 lg:grid-cols-[2fr_1fr] min-h-0 border-b border-gray-200 dark:border-white/10">

                {/* 📈 CANDLE */}
                <div className="h-full min-h-0 border-r border-gray-200 dark:border-white/10">
                  <CandleChart symbol={selectedSymbol} />
                </div>

                {/* 📊 DEPTH */}
                <div className="h-full min-h-0">
                  <DepthChart symbol={selectedSymbol} />
                </div>

              </div>

              {/* BOTTOM */}
              <div className="grid grid-cols-1 md:grid-cols-3 min-h-0">

                <div className="overflow-auto min-h-0 border-r">
                  <OrderBook symbol={selectedSymbol} />
                </div>

                <div className="overflow-auto min-h-0 border-r">
                  <OrderForm symbol={selectedSymbol} />
                </div>

                <div className="overflow-auto min-h-0">
                  <OrdersTable />
                </div>

              </div>
            </div>
          </div>
        ) : (
          /* ✅ OTHER PAGES */
          <div className="flex-1 overflow-auto">
            {children}
          </div>
        )}
      </div>
    </div>
  );
};

export default memo(MainLayout);