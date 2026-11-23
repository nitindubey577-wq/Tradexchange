package com.tradexchange.websocket;

import com.tradexchange.dto.response.CandleData;
import com.tradexchange.engine.MatchingEngine;
import com.tradexchange.engine.OrderBookDepth;
import com.tradexchange.engine.events.*;
import com.tradexchange.service.CandleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Broadcaster {

    private static final Logger log = LoggerFactory.getLogger(Broadcaster.class);

    private final SocketIoWebSocketHandler socketHandler;
    private final CandleService candleService;
    private final MatchingEngine matchingEngine;

    private final Map<String, Long> lastDepthEmit = new ConcurrentHashMap<>();

    public Broadcaster(SocketIoWebSocketHandler socketHandler, CandleService candleService, MatchingEngine matchingEngine) {
        this.socketHandler = socketHandler;
        this.candleService = candleService;
        this.matchingEngine = matchingEngine;
    }

    @EventListener
    public void onTrade(TradeEvent trade) {
        try {
            String symbol = trade.getSymbol().toUpperCase();

            // 1. Trade Event
            Map<String, Object> tradePayload = new HashMap<>();
            tradePayload.put("id", trade.getId());
            tradePayload.put("symbol", symbol);
            tradePayload.put("price", trade.getPrice());
            tradePayload.put("quantity", trade.getQuantity());
            tradePayload.put("buyOrderId", trade.getBuyOrderId());
            tradePayload.put("sellOrderId", trade.getSellOrderId());
            tradePayload.put("side", trade.getSide() != null ? trade.getSide().name() : null);
            tradePayload.put("timestamp", trade.getTimestamp());

            socketHandler.broadcast(symbol, "trade", tradePayload);

            // 2. Light Ticker Event
            Map<String, Object> tickerPayload = new HashMap<>();
            tickerPayload.put("symbol", symbol);
            tickerPayload.put("price", trade.getPrice());

            socketHandler.broadcast(symbol, "ticker", tickerPayload);

            // 3. Throttled Depth Event (300ms)
            long now = System.currentTimeMillis();
            long last = lastDepthEmit.getOrDefault(symbol, 0L);

            if (now - last > 300) {
                OrderBookDepth depth = matchingEngine.getDepth(symbol);

                Map<String, Object> depthPayload = new HashMap<>();
                depthPayload.put("symbol", symbol);
                depthPayload.put("bids", depth.getBids());
                depthPayload.put("asks", depth.getAsks());

                socketHandler.broadcast(symbol, "depth", depthPayload);
                lastDepthEmit.put(symbol, now);
            }

            // 4. Candle Event
            CandleData candle = candleService.updateRealtimeCandle(trade, "1m");
            if (candle != null) {
                Map<String, Object> candlePayload = new HashMap<>();
                candlePayload.put("symbol", symbol);
                candlePayload.put("interval", "1m");
                candlePayload.put("candle", candle);

                socketHandler.broadcast(symbol, "candle", candlePayload);
            }

        } catch (Exception e) {
            log.error("Trade broadcast handler error: {}", e.getMessage(), e);
        }
    }

    private void broadcastDepth(String symbol) {
        if (symbol == null) return;
        String sym = symbol.toUpperCase().trim();
        try {
            OrderBookDepth depth = matchingEngine.getDepth(sym);
            Map<String, Object> depthPayload = new HashMap<>();
            depthPayload.put("symbol", sym);
            depthPayload.put("bids", depth.getBids());
            depthPayload.put("asks", depth.getAsks());
            socketHandler.broadcast(sym, "depth", depthPayload);
        } catch (Exception e) {
            log.warn("Failed to broadcast depth for {}: {}", sym, e.getMessage());
        }
    }

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        socketHandler.broadcast("ORDERS", "orderPlaced", event.getOrder());
        if (event.getOrder() != null) {
            socketHandler.broadcast("ORDERS", "orders", java.util.List.of(event.getOrder()));
            broadcastDepth(event.getOrder().getSymbol());
        }
    }

    @EventListener
    public void onOrderPartiallyFilled(OrderPartiallyFilledEvent event) {
        socketHandler.broadcast("ORDERS", "orderPartiallyFilled", event.getOrder());
        if (event.getOrder() != null) {
            socketHandler.broadcast("ORDERS", "orders", java.util.List.of(event.getOrder()));
            broadcastDepth(event.getOrder().getSymbol());
        }
    }

    @EventListener
    public void onOrderFilled(OrderFilledEvent event) {
        socketHandler.broadcast("ORDERS", "orderFilled", event.getOrder());
        if (event.getOrder() != null) {
            socketHandler.broadcast("ORDERS", "orders", java.util.List.of(event.getOrder()));
            broadcastDepth(event.getOrder().getSymbol());
        }
    }

    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        socketHandler.broadcast("ORDERS", "orderCancelled", event.getOrder());
        if (event.getOrder() != null) {
            socketHandler.broadcast("ORDERS", "orders", java.util.List.of(event.getOrder()));
            broadcastDepth(event.getOrder().getSymbol());
        }
    }
}
