package com.tradexchange.engine;

import com.tradexchange.enums.OrderSide;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class OrderBook {
    private final String symbol;
    private final PriorityQueue<EngineOrder> bids;
    private final PriorityQueue<EngineOrder> asks;
    private Double lastTradedPrice;

    public OrderBook(String symbol) {
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : "";

        // Buy side: Max-heap on price, FIFO on sequence
        this.bids = new PriorityQueue<>((a, b) -> {
            int priceCmp = Double.compare(b.getPrice() != null ? b.getPrice() : 0.0,
                                          a.getPrice() != null ? a.getPrice() : 0.0);
            if (priceCmp != 0) return priceCmp;
            return Long.compare(a.getSeq(), b.getSeq());
        });

        // Sell side: Min-heap on price, FIFO on sequence
        this.asks = new PriorityQueue<>((a, b) -> {
            int priceCmp = Double.compare(a.getPrice() != null ? a.getPrice() : 0.0,
                                          b.getPrice() != null ? b.getPrice() : 0.0);
            if (priceCmp != 0) return priceCmp;
            return Long.compare(a.getSeq(), b.getSeq());
        });
    }

    public void cleanTop(PriorityQueue<EngineOrder> queue) {
        while (!queue.isEmpty()) {
            EngineOrder top = queue.peek();
            if (top == null || !top.isActive() || top.getRemaining() <= 0.000000001) {
                queue.poll();
            } else {
                break;
            }
        }
    }

    public void add(EngineOrder order) {
        if (order == null || !order.isActive() || order.getRemaining() <= 0.000000001) return;
        if (order.getSide() == OrderSide.BUY) {
            bids.offer(order);
        } else {
            asks.offer(order);
        }
    }

    public void remove(EngineOrder order) {
        if (order == null) return;
        order.setActive(false);
        order.setRemaining(0.0);
    }

    public EngineOrder getBestBid() {
        cleanTop(bids);
        return bids.peek();
    }

    public EngineOrder getBestAsk() {
        cleanTop(asks);
        return asks.peek();
    }

    public boolean hasLiquidity(OrderSide side) {
        if (side == OrderSide.BUY) {
            cleanTop(asks);
            return !asks.isEmpty();
        } else {
            cleanTop(bids);
            return !bids.isEmpty();
        }
    }

    public OrderBookDepth getDepth() {
        List<OrderBookDepth.Level> bidLevels = aggregate(bids, true);
        List<OrderBookDepth.Level> askLevels = aggregate(asks, false);

        return new OrderBookDepth(bidLevels, askLevels, lastTradedPrice);
    }

    private List<OrderBookDepth.Level> aggregate(PriorityQueue<EngineOrder> queue, boolean isBid) {
        Map<Double, Double> map = new HashMap<>();

        for (EngineOrder o : queue) {
            if (o == null || !o.isActive() || o.getRemaining() <= 0.000000001 || o.getPrice() == null) continue;
            map.put(o.getPrice(), map.getOrDefault(o.getPrice(), 0.0) + o.getRemaining());
        }

        List<OrderBookDepth.Level> result = new ArrayList<>();
        for (Map.Entry<Double, Double> entry : map.entrySet()) {
            double roundedQty = BigDecimal.valueOf(entry.getValue())
                    .setScale(8, RoundingMode.HALF_UP)
                    .doubleValue();
            result.add(new OrderBookDepth.Level(entry.getKey(), roundedQty));
        }

        if (isBid) {
            result.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
        } else {
            result.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        }

        return result;
    }

    public String getSymbol() { return symbol; }
    public PriorityQueue<EngineOrder> getBids() { return bids; }
    public PriorityQueue<EngineOrder> getAsks() { return asks; }
    public Double getLastTradedPrice() { return lastTradedPrice; }
    public void setLastTradedPrice(Double lastTradedPrice) { this.lastTradedPrice = lastTradedPrice; }
}
