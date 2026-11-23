import { useEffect, useState, useRef } from "react";
import api from "../../services/api";
import { useSocket } from "../../hooks/useSocket";

const POLL_INTERVAL = 10000;

const OrdersTable = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const intervalRef = useRef(null);

  /* ===============================
     🔄 INITIAL LOAD
  =============================== */
  const load = async () => {
    try {
      const res = await api.get("/orders");
      setOrders(res.data?.data || []);
    } catch (err) {
      console.error("❌ Orders fetch error:", err);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  /* ===============================
     📡 REAL-TIME SOCKET (FIXED)
  =============================== */
  useSocket({
    symbol: "ORDERS", // 🔥 global room
    event: "orders",
    debug: true, // remove later

    handler: (data) => {
      if (!data) return;

      console.log("📡 ORDERS UPDATE:", data);

      // 🔥 support both formats
      const incoming = data.orders || data;

      if (!Array.isArray(incoming)) return;

      setOrders((prev) => {
        const map = new Map(prev.map((o) => [o.orderId, o]));

        for (const o of incoming) {
          map.set(o.orderId, {
            ...map.get(o.orderId),
            ...o,
          });
        }

        return Array.from(map.values());
      });
    },
  });

  /* ===============================
     🔁 FALLBACK POLLING
  =============================== */
  useEffect(() => {
    load();

    intervalRef.current = setInterval(load, POLL_INTERVAL);

    return () => clearInterval(intervalRef.current);
  }, []);

  /* ===============================
     ❌ CANCEL ORDER
  =============================== */
  const cancelOrder = async (id) => {
    // optimistic UI
    setOrders((prev) =>
      prev.map((o) =>
        o.orderId === id ? { ...o, status: "CANCELLED" } : o
      )
    );

    try {
      await api.delete(`/orders/${id}`);
    } catch (err) {
      console.error("❌ Cancel failed:", err);
      load(); // rollback
    }
  };

  /* ===============================
     🧠 FORMATTERS
  =============================== */
  const format = (n) =>
    Number(n || 0).toLocaleString("en-IN", {
      maximumFractionDigits: 2,
    });

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
      {/* SCROLL */}
      <div className="flex-1 overflow-y-auto">

        {/* HEADER */}
        <div
          className="
            sticky top-0 z-20
            flex items-center justify-between px-3 py-2 text-xs
            text-gray-500 border-b border-gray-200
            bg-white/95 backdrop-blur
            dark:bg-[#0b1220]/95 dark:text-gray-400 dark:border-white/10
          "
        >
          <span>Open Orders</span>
          <span className="text-[10px]">{orders.length}</span>
        </div>

        {/* COLUMN HEADER */}
        <div
          className="
            sticky top-[36px] z-10
            grid grid-cols-5 px-3 py-1 text-[10px]
            text-gray-500 border-b border-gray-200
            bg-white/95 backdrop-blur
            dark:bg-[#0b1220]/95 dark:text-gray-400 dark:border-white/10
          "
        >
          <span>Side</span>
          <span>Price</span>
          <span>Qty</span>
          <span>Status</span>
          <span></span>
        </div>

        {/* LOADING */}
        {loading && (
          <div className="text-center text-gray-500 py-4">
            Loading orders...
          </div>
        )}

        {/* EMPTY */}
        {!loading && orders.length === 0 && (
          <div className="text-center text-gray-500 py-4">
            No Orders
          </div>
        )}

        {/* LIST */}
        {!loading &&
          orders.map((o) => {
            const isBuy = o.side === "BUY";

            return (
              <div
                key={o.orderId}
                className="
                  grid grid-cols-5 px-3 py-2 items-center
                  border-b border-gray-100
                  hover:bg-gray-100 transition
                  dark:border-white/5 dark:hover:bg-white/5
                "
              >
                {/* SIDE */}
                <span
                  className={`font-semibold ${
                    isBuy
                      ? "text-green-500 dark:text-green-400"
                      : "text-red-500 dark:text-red-400"
                  }`}
                >
                  {o.side}
                </span>

                {/* PRICE */}
                <span>
                  {o.price ? `₹${format(o.price)}` : "-"}
                </span>

                {/* QTY */}
                <span className="text-gray-500 dark:text-gray-400">
                  {format(o.remaining)}
                </span>

                {/* STATUS */}
                <span
                  className={`
                    text-[10px] px-2 py-[2px] rounded-md w-fit
                    ${
                      o.status === "OPEN"
                        ? "bg-yellow-100 text-yellow-600 dark:bg-yellow-500/20 dark:text-yellow-400"
                        : o.status === "FILLED"
                        ? "bg-green-100 text-green-600 dark:bg-green-500/20 dark:text-green-400"
                        : o.status === "CANCELLED"
                        ? "bg-red-100 text-red-600 dark:bg-red-500/20 dark:text-red-400"
                        : "bg-gray-100 text-gray-600 dark:bg-gray-500/20 dark:text-gray-400"
                    }
                  `}
                >
                  {o.status}
                </span>

                {/* ACTION */}
                <span className="text-right">
                  {o.status === "OPEN" && (
                    <button
                      onClick={() => cancelOrder(o.orderId)}
                      className="
                        px-2 py-[2px] rounded text-[10px] transition
                        bg-red-100 text-red-600 hover:bg-red-200
                        dark:bg-red-500/20 dark:text-red-400 dark:hover:bg-red-500/30
                      "
                    >
                      Cancel
                    </button>
                  )}
                </span>
              </div>
            );
          })}
      </div>
    </div>
  );
};

export default OrdersTable;