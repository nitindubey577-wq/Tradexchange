import { useState } from "react";
import { useSocket } from "../hooks/useSocket";

const Ticker = ({ symbol }) => {
  const [price, setPrice] = useState(0);

  useSocket({
    symbol,
    event: "trade",
    handler: (t) => {
      if (!t) return;
      setPrice(t.price);
    },
  });

  return (
    <div className="px-2">
      {symbol} ₹{price}
    </div>
  );
};

export default function TickerBar({ markets }) {
  return (
    <div className="flex gap-3 overflow-x-auto p-2 border-b">
      {markets.slice(0, 20).map((m) => (
        <Ticker key={m} symbol={m} />
      ))}
    </div>
  );
}