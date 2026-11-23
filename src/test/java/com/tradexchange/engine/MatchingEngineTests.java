package com.tradexchange.engine;

import com.tradexchange.engine.events.TradeEvent;
import com.tradexchange.enums.OrderSide;
import com.tradexchange.enums.OrderStatus;
import com.tradexchange.enums.OrderType;
import com.tradexchange.exception.TradingPlatformException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTests {

    private MatchingEngine engine;
    private AtomicInteger idCounter;

    @BeforeEach
    void setUp() {
        engine = new MatchingEngine(null);
        idCounter = new AtomicInteger(1);
    }

    private EngineOrder createOrder(String userId, String symbol, OrderSide side, OrderType type, Double price, double quantity) {
        return new EngineOrder(
                String.valueOf(idCounter.getAndIncrement()),
                userId,
                type,
                side,
                symbol,
                price,
                quantity,
                System.currentTimeMillis()
        );
    }

    @Test
    @DisplayName("Test Basic Match")
    void testBasicMatch() {
        String symbol = "STEEL";
        EngineOrder sell = createOrder("S1", symbol, OrderSide.SELL, OrderType.LIMIT, 100.0, 10.0);
        EngineOrder buy = createOrder("B1", symbol, OrderSide.BUY, OrderType.LIMIT, 100.0, 10.0);

        engine.placeOrder(sell);
        List<TradeEvent> trades = engine.placeOrder(buy);

        assertEquals(1, trades.size());
        assertEquals(10.0, trades.get(0).getQuantity());
        assertEquals(100.0, trades.get(0).getPrice());
        assertEquals(OrderStatus.FILLED, sell.getStatus());
        assertEquals(OrderStatus.FILLED, buy.getStatus());
    }

    @Test
    @DisplayName("Test Partial Fill")
    void testPartialFill() {
        String symbol = "WHEAT";
        EngineOrder sell = createOrder("S1", symbol, OrderSide.SELL, OrderType.LIMIT, 50.0, 20.0);
        EngineOrder buy = createOrder("B1", symbol, OrderSide.BUY, OrderType.LIMIT, 50.0, 10.0);

        engine.placeOrder(sell);
        List<TradeEvent> trades = engine.placeOrder(buy);

        assertEquals(1, trades.size());
        assertEquals(10.0, trades.get(0).getQuantity());
        assertEquals(OrderStatus.PARTIALLY_FILLED, sell.getStatus());
        assertEquals(10.0, sell.getRemaining());
        assertEquals(OrderStatus.FILLED, buy.getStatus());

        OrderBookDepth depth = engine.getDepth(symbol);
        assertEquals(1, depth.getAsks().size());
        assertEquals(10.0, depth.getAsks().get(0).getQuantity());
    }

    @Test
    @DisplayName("Test Multiple Matches")
    void testMultipleMatches() {
        String symbol = "OIL";
        EngineOrder sell1 = createOrder("S1", symbol, OrderSide.SELL, OrderType.LIMIT, 100.0, 5.0);
        EngineOrder sell2 = createOrder("S2", symbol, OrderSide.SELL, OrderType.LIMIT, 100.0, 5.0);
        EngineOrder buy = createOrder("B1", symbol, OrderSide.BUY, OrderType.LIMIT, 100.0, 10.0);

        engine.placeOrder(sell1);
        engine.placeOrder(sell2);
        List<TradeEvent> trades = engine.placeOrder(buy);

        assertEquals(2, trades.size());
        double totalVolume = trades.stream().mapToDouble(TradeEvent::getQuantity).sum();
        assertEquals(10.0, totalVolume);
        assertEquals(OrderStatus.FILLED, buy.getStatus());
    }

    @Test
    @DisplayName("Test Market Order")
    void testMarketOrder() {
        String symbol = "RICE";
        EngineOrder sell = createOrder("S1", symbol, OrderSide.SELL, OrderType.LIMIT, 80.0, 10.0);
        EngineOrder buy = createOrder("B1", symbol, OrderSide.BUY, OrderType.MARKET, null, 5.0);

        engine.placeOrder(sell);
        List<TradeEvent> trades = engine.placeOrder(buy);

        assertEquals(1, trades.size());
        assertEquals(80.0, trades.get(0).getPrice());
        assertEquals(5.0, trades.get(0).getQuantity());
        assertEquals(OrderStatus.FILLED, buy.getStatus());
    }

    @Test
    @DisplayName("Test Cancel Order")
    void testCancelOrder() {
        String symbol = "COPPER";
        EngineOrder order = createOrder("S1", symbol, OrderSide.SELL, OrderType.LIMIT, 200.0, 10.0);

        engine.placeOrder(order);
        engine.cancelOrder(order.getId());

        OrderBookDepth depth = engine.getDepth(symbol);
        assertTrue(depth.getAsks().isEmpty());
    }

    @Test
    @DisplayName("Test Multi Commodity Isolation")
    void testMultiCommodityIsolation() {
        EngineOrder sell = createOrder("S1", "STEEL_X", OrderSide.SELL, OrderType.LIMIT, 100.0, 10.0);
        EngineOrder buy = createOrder("B1", "GOLD_X", OrderSide.BUY, OrderType.LIMIT, 100.0, 10.0);

        engine.placeOrder(sell);
        engine.placeOrder(buy);

        OrderBookDepth depthSteel = engine.getDepth("STEEL_X");
        OrderBookDepth depthGold = engine.getDepth("GOLD_X");

        assertEquals(0, depthSteel.getBids().size());
        assertEquals(1, depthSteel.getAsks().size());
        assertEquals(0, depthGold.getAsks().size());
        assertEquals(1, depthGold.getBids().size());
    }

    @Test
    @DisplayName("Test Invalid Orders")
    void testInvalidOrders() {
        EngineOrder invalid = createOrder("B1", "TEST", OrderSide.BUY, OrderType.LIMIT, -10.0, 5.0);
        assertThrows(TradingPlatformException.class, () -> engine.placeOrder(invalid));
    }

    @Test
    @DisplayName("Test Depth Aggregation")
    void testDepthAggregation() {
        String symbol = "ALUMINUM";
        EngineOrder sell1 = createOrder("S1", symbol, OrderSide.SELL, OrderType.LIMIT, 300.0, 5.0);
        EngineOrder sell2 = createOrder("S2", symbol, OrderSide.SELL, OrderType.LIMIT, 300.0, 5.0);

        engine.placeOrder(sell1);
        engine.placeOrder(sell2);

        OrderBookDepth depth = engine.getDepth(symbol);
        assertEquals(1, depth.getAsks().size());
        assertEquals(10.0, depth.getAsks().get(0).getQuantity());
    }

    @Test
    @DisplayName("Test Concurrency Safety")
    void testConcurrency() throws Exception {
        String symbol = "ZINC";
        int orderCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < orderCount; i++) {
            final int idx = i;
            tasks.add(() -> {
                EngineOrder sell = createOrder("S" + idx, symbol, OrderSide.SELL, OrderType.LIMIT, 100.0, 1.0);
                engine.placeOrder(sell);
                return null;
            });
        }

        List<Future<Void>> futures = executor.invokeAll(tasks);
        for (Future<Void> f : futures) {
            f.get();
        }
        executor.shutdown();

        EngineOrder buy = createOrder("B1", symbol, OrderSide.BUY, OrderType.MARKET, null, 20.0);
        List<TradeEvent> trades = engine.placeOrder(buy);

        assertEquals(20, trades.size());
        double totalVolume = trades.stream().mapToDouble(TradeEvent::getQuantity).sum();
        assertEquals(20.0, totalVolume);
    }

    @Test
    @DisplayName("Test Empty Market Order")
    void testEmptyMarketOrder() {
        EngineOrder buy = createOrder("B1", "EMPTY", OrderSide.BUY, OrderType.MARKET, null, 10.0);
        List<TradeEvent> trades = engine.placeOrder(buy);
        assertEquals(0, trades.size());
    }

    @Test
    @DisplayName("Test No Negative Remaining")
    void testNoNegativeRemaining() {
        String symbol = "SAFE";
        EngineOrder sell = createOrder("S1", symbol, OrderSide.SELL, OrderType.LIMIT, 100.0, 5.0);
        EngineOrder buy = createOrder("B1", symbol, OrderSide.BUY, OrderType.LIMIT, 100.0, 10.0);

        engine.placeOrder(sell);
        engine.placeOrder(buy);

        assertTrue(sell.getRemaining() >= 0);
        assertEquals(0.0, sell.getRemaining());
        assertEquals(5.0, buy.getRemaining());
    }
}
