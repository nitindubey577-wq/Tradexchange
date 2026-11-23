package com.tradexchange.engine;

import com.tradexchange.engine.events.*;
import com.tradexchange.enums.OrderSide;
import com.tradexchange.enums.OrderStatus;
import com.tradexchange.enums.OrderType;
import com.tradexchange.exception.TradingPlatformException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MatchingEngine {

    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final Map<String, EngineOrder> orderMap = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public MatchingEngine(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public OrderBook getOrderBook(String symbol) {
        String sym = symbol != null ? symbol.toUpperCase().trim() : "";
        return orderBooks.computeIfAbsent(sym, OrderBook::new);
    }

    public ReentrantLock getLock(String symbol) {
        String sym = symbol != null ? symbol.toUpperCase().trim() : "";
        return locks.computeIfAbsent(sym, k -> new ReentrantLock());
    }

    public List<TradeEvent> placeOrder(EngineOrder order) {
        OrderValidator.validate(order);

        String symbol = order.getSymbol().toUpperCase().trim();
        ReentrantLock lock = getLock(symbol);
        lock.lock();

        try {
            OrderBook book = getOrderBook(symbol);

            // Safe Market Order Check
            if (order.getType() == OrderType.MARKET && book.getBids().isEmpty() && book.getAsks().isEmpty()) {
                return Collections.emptyList();
            }

            // Init order
            order.setRemaining(order.getQuantity());
            order.setActive(true);
            order.setStatus(OrderStatus.OPEN);
            order.setSeq(sequence.incrementAndGet());

            orderMap.put(order.getId(), order);

            if (eventPublisher != null) {
                eventPublisher.publishEvent(new OrderPlacedEvent(order));
            }

            // Match
            List<TradeEvent> trades = match(order, book, order.getType() == OrderType.MARKET);

            // Post match
            if (order.getType() == OrderType.MARKET) {
                if (order.getRemaining() <= 0.000000001) {
                    order.setStatus(OrderStatus.FILLED);
                    if (eventPublisher != null) {
                        eventPublisher.publishEvent(new OrderFilledEvent(order));
                    }
                } else if (order.getRemaining() < order.getQuantity()) {
                    order.setStatus(OrderStatus.PARTIALLY_FILLED);
                    if (eventPublisher != null) {
                        eventPublisher.publishEvent(new OrderPartiallyFilledEvent(order));
                    }
                }
                order.setActive(false);
                orderMap.remove(order.getId());
            } else {
                if (order.getRemaining() > 0.000000001 && order.isActive()) {
                    book.add(order);
                } else {
                    order.setStatus(OrderStatus.FILLED);
                    order.setActive(false);
                    if (eventPublisher != null) {
                        eventPublisher.publishEvent(new OrderFilledEvent(order));
                    }
                    orderMap.remove(order.getId());
                }
            }

            return trades;

        } finally {
            lock.unlock();
        }
    }

    private List<TradeEvent> match(EngineOrder order, OrderBook book, boolean isMarket) {
        List<TradeEvent> trades = new ArrayList<>();
        PriorityQueue<EngineOrder> queue = (order.getSide() == OrderSide.BUY) ? book.getAsks() : book.getBids();

        while (order.getRemaining() > 0.000000001 && !queue.isEmpty()) {
            book.cleanTop(queue);
            if (queue.isEmpty()) break;

            EngineOrder best = queue.peek();
            if (best == null) break;

            // Price check
            if (!isMarket) {
                if ((order.getSide() == OrderSide.BUY && order.getPrice() < best.getPrice()) ||
                    (order.getSide() == OrderSide.SELL && order.getPrice() > best.getPrice())) {
                    break;
                }
            }

            double tradeQty = Math.min(order.getRemaining(), best.getRemaining());

            String buyOrderId = order.getSide() == OrderSide.BUY ? order.getId() : best.getId();
            String sellOrderId = order.getSide() == OrderSide.SELL ? order.getId() : best.getId();

            TradeEvent trade = new TradeEvent(
                    UUID.randomUUID().toString(),
                    order.getSymbol().toUpperCase(),
                    best.getPrice(),
                    tradeQty,
                    buyOrderId,
                    sellOrderId,
                    order.getSide(),
                    System.currentTimeMillis()
            );

            trades.add(trade);

            // Apply fill
            order.applyFill(best.getPrice(), tradeQty);
            best.applyFill(best.getPrice(), tradeQty);

            // Emit status
            emitOrderStatus(order);
            emitOrderStatus(best);

            // Clean filled
            if (!best.isActive()) {
                queue.poll();
                orderMap.remove(best.getId());
            }

            if (!order.isActive()) {
                orderMap.remove(order.getId());
            }

            // Update last traded price
            book.setLastTradedPrice(trade.getPrice());

            // Emit trade event
            if (eventPublisher != null) {
                eventPublisher.publishEvent(trade);
            }
        }

        return trades;
    }

    private void emitOrderStatus(EngineOrder order) {
        if (eventPublisher == null) return;
        if (order.getStatus() == OrderStatus.PARTIALLY_FILLED) {
            eventPublisher.publishEvent(new OrderPartiallyFilledEvent(order));
        } else if (order.getStatus() == OrderStatus.FILLED) {
            eventPublisher.publishEvent(new OrderFilledEvent(order));
        }
    }

    public void cancelOrder(String orderId) {
        if (orderId == null) {
            throw new TradingPlatformException("ORDER_ID_REQUIRED");
        }

        EngineOrder order = orderMap.get(orderId);
        if (order == null) {
            throw new TradingPlatformException("ORDER_NOT_FOUND");
        }

        ReentrantLock lock = getLock(order.getSymbol());
        lock.lock();

        try {
            order.setActive(false);
            order.setStatus(OrderStatus.CANCELLED);
            order.setRemaining(0.0);

            orderMap.remove(orderId);

            if (eventPublisher != null) {
                eventPublisher.publishEvent(new OrderCancelledEvent(order));
            }
        } finally {
            lock.unlock();
        }
    }

    public OrderBookDepth getDepth(String symbol) {
        OrderBook book = getOrderBook(symbol);
        return book.getDepth();
    }

    public EngineOrder getOrder(String orderId) {
        return orderMap.get(orderId);
    }
}
