import { useState } from "react";
import api from "../../services/api";
import { useOrderFormStore } from "../../store/useOrderFormStore";

const OrderForm = ({ symbol, userId = "user_1" }) => {
  const {
    price,
    setPrice,
    quantity,
    setQuantity,
    resetForm,
    side,
    setSide,
    orderType,
    setOrderType,
    getTotal,
  } = useOrderFormStore();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  /* ---------------- CALCULATIONS ---------------- */

  const total = getTotal();

  const isValid =
    Number(quantity) > 0 &&
    (orderType === "MARKET" ||
      (price !== "" && Number(price) > 0));

  const format = (n) =>
    Number(n || 0).toLocaleString("en-IN", {
      maximumFractionDigits: 2,
    });

  /* ---------------- SUBMIT ---------------- */

  const submit = async () => {
    if (!isValid) {
      setError("Enter valid price & quantity");
      return;
    }

    try {
      setLoading(true);
      setError("");

      await api.post("/orders", {
        userId,
        symbol,
        side,
        type: orderType,
        price:
          orderType === "LIMIT" && Number(price) > 0
            ? Number(price)
            : undefined,
        quantity: Number(quantity),
      });

      resetForm();
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || "Order failed");
    } finally {
      setLoading(false);
    }
  };

  /* ---------------- UI ---------------- */

  return (
    <div
      className="
      rounded-2xl p-4 space-y-4 border shadow-sm
      bg-white border-gray-200
      dark:bg-black/30 dark:border-white/10 dark:backdrop-blur-xl dark:shadow-xl
    "
    >
      {/* HEADER */}
      <div className="flex justify-between items-center text-xs">
        <span className="text-gray-500 dark:text-gray-400">Order</span>
        <span className="font-medium text-gray-700 dark:text-gray-300">
          {symbol}
        </span>
      </div>

      {/* BUY / SELL */}
      <div className="grid grid-cols-2 gap-2">
        {["BUY", "SELL"].map((s) => {
          const active = side === s;

          return (
            <button
              key={s}
              onClick={() => setSide(s)}
              className={`
                py-2 rounded-xl font-semibold transition-all border
                ${
                  active
                    ? s === "BUY"
                      ? "bg-green-500 text-black border-green-400 shadow-md"
                      : "bg-red-500 text-black border-red-400 shadow-md"
                    : "bg-gray-100 border-gray-200 text-gray-600 hover:bg-gray-200 dark:bg-white/5 dark:border-white/10 dark:text-gray-300 dark:hover:bg-white/10"
                }
              `}
            >
              {s}
            </button>
          );
        })}
      </div>

      {/* ORDER TYPE */}
      <div className="flex gap-2 text-xs">
        {["LIMIT", "MARKET"].map((t) => (
          <button
            key={t}
            onClick={() => setOrderType(t)}
            className={`
              flex-1 py-1.5 rounded-lg border transition
              ${
                orderType === t
                  ? "bg-blue-100 border-blue-400 text-blue-600 dark:bg-blue-500/20 dark:text-blue-300"
                  : "bg-gray-100 border-gray-200 text-gray-500 hover:bg-gray-200 dark:bg-white/5 dark:border-white/10 dark:text-gray-400 dark:hover:bg-white/10"
              }
            `}
          >
            {t}
          </button>
        ))}
      </div>

      {/* PRICE */}
      {orderType === "LIMIT" && (
        <div className="relative">
          <span className="absolute left-3 top-2 text-gray-500 text-xs">₹</span>
          <input
            type="number"
            inputMode="decimal"
            className="
              w-full pl-6 pr-3 py-2 rounded-xl border text-sm
              bg-white border-gray-200 text-gray-900
              focus:outline-none focus:border-blue-500
              dark:bg-white/5 dark:border-white/10 dark:text-white
            "
            placeholder="Price"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
          />
        </div>
      )}

      {/* QUANTITY */}
      <input
        type="number"
        inputMode="decimal"
        className="
          w-full px-3 py-2 rounded-xl border text-sm
          bg-white border-gray-200 text-gray-900
          focus:outline-none focus:border-blue-500
          dark:bg-white/5 dark:border-white/10 dark:text-white
        "
        placeholder="Quantity"
        value={quantity}
        onChange={(e) => setQuantity(e.target.value)}
      />

      {/* TOTAL */}
      <div
        className="
        flex justify-between items-center text-xs px-3 py-2 rounded-lg border
        bg-gray-100 border-gray-200
        dark:bg-white/5 dark:border-white/10
      "
      >
        <span className="text-gray-500 dark:text-gray-400">Total</span>
        <span className="font-medium text-gray-800 dark:text-gray-200">
          ₹ {format(total)}
        </span>
      </div>

      {/* QUICK % */}
      <div className="grid grid-cols-4 gap-2 text-[10px]">
        {[25, 50, 75, 100].map((p) => (
          <button
            key={p}
            className="
              py-1 rounded-md border transition
              bg-gray-100 border-gray-200 hover:bg-gray-200
              dark:bg-white/5 dark:border-white/10 dark:hover:bg-white/10
            "
            onClick={() => {
              const baseQty = 10;
              const qty = (p / 100) * baseQty;
              setQuantity(qty.toFixed(2));
            }}
          >
            {p}%
          </button>
        ))}
      </div>

      {/* ERROR */}
      {error && (
        <div className="text-red-500 text-xs bg-red-100 dark:bg-red-500/10 px-2 py-1 rounded">
          {error}
        </div>
      )}

      {/* SUBMIT */}
      <button
        onClick={submit}
        disabled={loading || !isValid}
        className={`
          w-full py-2.5 rounded-xl font-semibold transition
          ${
            side === "BUY"
              ? "bg-green-500 text-black hover:bg-green-400"
              : "bg-red-500 text-black hover:bg-red-400"
          }
          ${loading || !isValid ? "opacity-50 cursor-not-allowed" : ""}
        `}
      >
        {loading ? "Placing Order..." : `${side} ${symbol}`}
      </button>
    </div>
  );
};

export default OrderForm;