// ===============================
// 📁 src/components/OrderBook/OrderBook.jsx (PRO FINAL)
// ===============================

import { useState, useRef, useMemo, useEffect } from "react";
import { useSocket } from "../../hooks/useSocket";
import { useOrderFormStore } from "../../store/useOrderFormStore";

const MAX_LEVELS = 15;

const OrderBook = ({ symbol }) => {
  const [bids, setBids] = useState([]);
  const [asks, setAsks] = useState([]);

  const frameRef = useRef(null);
  const setPrice = useOrderFormStore((s) => s.setPrice);

  /* ===============================
     📡 SOCKET
  =============================== */
  useSocket({
    symbol,
    event: "depth",

    handler: (data) => {
      if (!data?.bids || !data?.asks) return;

      const incomingSymbol = data.symbol?.toUpperCase();
      if (!incomingSymbol || incomingSymbol !== symbol?.toUpperCase()) return;

      if (frameRef.current) cancelAnimationFrame(frameRef.current);

      frameRef.current = requestAnimationFrame(() => {
        // ✅ SORT
        const topBids = data.bids
          .slice(0, MAX_LEVELS)
          .sort((a, b) => b.price - a.price);

        const topAsks = data.asks
          .slice(0, MAX_LEVELS)
          .sort((a, b) => a.price - b.price);

        let bidTotal = 0;
        const bidsCum = topBids.map((b) => {
          bidTotal += Number(b.quantity || 0);
          return {
            price: Number(b.price),
            quantity: Number(b.quantity),
            total: bidTotal,
          };
        });

        let askTotal = 0;
        const asksCum = topAsks.map((a) => {
          askTotal += Number(a.quantity || 0);
          return {
            price: Number(a.price),
            quantity: Number(a.quantity),
            total: askTotal,
          };
        });

        setBids(bidsCum);
        setAsks(asksCum);
      });
    },
  });

  useEffect(() => {
    return () => {
      if (frameRef.current) cancelAnimationFrame(frameRef.current);
    };
  }, []);

  /* ===============================
     📊 CALCULATIONS
  =============================== */
  const maxQty = useMemo(() => {
    return Math.max(
      ...bids.map((b) => b.total || 0),
      ...asks.map((a) => a.total || 0),
      1
    );
  }, [bids, asks]);

  const bestBid = bids[0]?.price;
  const bestAsk = asks[0]?.price;

  const spread =
    bestBid !== undefined && bestAsk !== undefined
      ? (bestAsk - bestBid).toFixed(2)
      : "--";

  const format = (n) =>
    Number(n || 0).toLocaleString("en-IN", {
      maximumFractionDigits: 2,
    });

  /* ===============================
     🧱 UI
  =============================== */
  return (
    <div className="h-full flex flex-col text-[11px] font-mono rounded-2xl overflow-hidden bg-white border border-gray-200 dark:bg-black/30 dark:border-white/10">
      {/* HEADER */}
      <div className="grid grid-cols-3 px-3 py-2 text-[10px] text-gray-500 border-b border-gray-200 dark:text-gray-400 dark:border-white/10">
        <span>Price</span>
        <span className="text-center">Qty</span>
        <span className="text-right">Total</span>
      </div>

      {/* ASKS */}
      <div className="flex-1 overflow-y-auto">
        {asks.slice().reverse().map((a, i) => {
          const intensity = (a.total / maxQty) * 100;

          return (
            <div
              key={`${a.price}-${i}`}
              onClick={() => setPrice(a.price)}
              className="relative grid grid-cols-3 px-3 py-[4px] cursor-pointer hover:bg-red-100 dark:hover:bg-red-500/10"
            >
              <div
                className="absolute right-0 top-0 h-full bg-red-500/20"
                style={{
                  width: `${intensity}%`,
                  opacity: 0.15 + intensity / 120,
                }}
              />

              <span className="text-red-500 z-10 font-medium">
                {format(a.price)}
              </span>
              <span className="text-center z-10">
                {format(a.quantity)}
              </span>
              <span className="text-right z-10">
                {format(a.total)}
              </span>
            </div>
          );
        })}
      </div>

      {/* SPREAD */}
      <div className="text-center py-1.5 text-[10px] font-medium bg-gray-100 border-y border-gray-200 dark:bg-white/5 dark:border-white/10">
        Spread:{" "}
        <span className="text-yellow-500">
          {spread === "--" ? spread : `₹${spread}`}
        </span>
      </div>

      {/* BIDS */}
      <div className="flex-1 overflow-y-auto">
        {bids.map((b, i) => {
          const intensity = (b.total / maxQty) * 100;

          return (
            <div
              key={`${b.price}-${i}`}
              onClick={() => setPrice(b.price)}
              className="relative grid grid-cols-3 px-3 py-[4px] cursor-pointer hover:bg-green-100 dark:hover:bg-green-500/10"
            >
              <div
                className="absolute right-0 top-0 h-full bg-green-500/20"
                style={{
                  width: `${intensity}%`,
                  opacity: 0.15 + intensity / 120,
                }}
              />

              <span className="text-green-500 z-10 font-medium">
                {format(b.price)}
              </span>
              <span className="text-center z-10">
                {format(b.quantity)}
              </span>
              <span className="text-right z-10">
                {format(b.total)}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default OrderBook;