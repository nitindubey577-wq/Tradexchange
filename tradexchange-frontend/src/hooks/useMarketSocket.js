import { useEffect } from "react";
import { socket } from "../services/socket";
import { useMarketStore } from "../store/marketStore";

export const useMarketSocket = (symbol) => {
  const updateMarket = useMarketStore((s) => s.updateMarket);

  useEffect(() => {
    if (!symbol) return;

    socket.emit("subscribe", symbol);

    socket.on("trade", (trade) => {
      if (trade.symbol !== symbol) return;

      updateMarket(trade.symbol, {
        lastPrice: trade.price,
      });
    });

    return () => {
      socket.emit("unsubscribe", symbol);
      socket.off("trade");
    };
  }, [symbol]);
};