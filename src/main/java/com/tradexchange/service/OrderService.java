package com.tradexchange.service;

import com.tradexchange.dto.request.OrderPlaceRequest;
import com.tradexchange.engine.EngineOrder;
import com.tradexchange.engine.MatchingEngine;
import com.tradexchange.engine.OrderBookDepth;
import com.tradexchange.engine.events.TradeEvent;
import com.tradexchange.entity.MarketEntity;
import com.tradexchange.entity.OrderEntity;
import com.tradexchange.enums.OrderSide;
import com.tradexchange.enums.OrderStatus;
import com.tradexchange.enums.OrderType;
import com.tradexchange.exception.TradingPlatformException;
import com.tradexchange.repository.MarketRepository;
import com.tradexchange.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final MatchingEngine matchingEngine;
    private final OrderRepository orderRepository;
    private final MarketRepository marketRepository;
    private final TradeService tradeService;
    private final MarketService marketService;

    public OrderService(MatchingEngine matchingEngine,
                        OrderRepository orderRepository,
                        MarketRepository marketRepository,
                        TradeService tradeService,
                        MarketService marketService) {
        this.matchingEngine = matchingEngine;
        this.orderRepository = orderRepository;
        this.marketRepository = marketRepository;
        this.tradeService = tradeService;
        this.marketService = marketService;
    }

    @Transactional
    public Map<String, Object> placeOrder(OrderPlaceRequest req) {
        if (req == null) throw new TradingPlatformException("INVALID_REQUEST");
        if (req.getUserId() == null || req.getUserId().trim().isEmpty()) throw new TradingPlatformException("USER_ID_REQUIRED");
        if (req.getSymbol() == null || req.getSymbol().trim().isEmpty()) throw new TradingPlatformException("SYMBOL_REQUIRED");

        String symbol = req.getSymbol().toUpperCase().trim();
        OrderSide side;
        try {
            side = OrderSide.fromString(req.getSide());
            if (side == null) throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new TradingPlatformException("INVALID_SIDE");
        }

        OrderType type;
        try {
            type = OrderType.fromString(req.getType());
            if (type == null) throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new TradingPlatformException("INVALID_TYPE");
        }

        Double quantity = req.getQuantity();
        Double price = req.getPrice();

        if (quantity == null || quantity <= 0) {
            throw new TradingPlatformException("INVALID_QUANTITY");
        }

        if (type == OrderType.LIMIT && (price == null || price <= 0)) {
            throw new TradingPlatformException("INVALID_PRICE");
        }

        // Market Validation & Rules
        MarketEntity market = marketRepository.findBySymbol(symbol).orElse(null);
        if (market != null) {
            if (!Boolean.TRUE.equals(market.getIsActive())) {
                throw new TradingPlatformException("MARKET_NOT_AVAILABLE");
            }

            int qtyPrecision = market.getQuantityPrecision() != null ? market.getQuantityPrecision() : 6;
            int pricePrecision = market.getPricePrecision() != null ? market.getPricePrecision() : 2;

            quantity = round(quantity, qtyPrecision);

            // Lot size
            double lot = market.getLotSize() != null && market.getLotSize() > 0 ? market.getLotSize() : 0.000001;
            long qtySteps = (long) Math.ceil(quantity / lot);
            quantity = round(qtySteps * lot, qtyPrecision);

            if (quantity <= 0) {
                throw new TradingPlatformException("INVALID_QUANTITY_AFTER_ROUND");
            }

            // Tick size
            if (type == OrderType.LIMIT) {
                price = round(price, pricePrecision);
                double tick = market.getTickSize() != null && market.getTickSize() > 0 ? market.getTickSize() : 0.01;
                long priceSteps = Math.round(price / tick);
                price = round(priceSteps * tick, pricePrecision);
            } else {
                price = null;
            }

            // Min / Max Checks
            if (market.getMinQty() != null && quantity < market.getMinQty()) {
                throw new TradingPlatformException("BELOW_MIN_QTY");
            }
            if (market.getMaxQty() != null && quantity > market.getMaxQty()) {
                throw new TradingPlatformException("ABOVE_MAX_QTY");
            }

            double orderValue = (price != null) ? price * quantity : quantity;
            if (market.getMinNotional() != null && orderValue < market.getMinNotional()) {
                throw new TradingPlatformException("BELOW_MIN_NOTIONAL");
            }
            if (market.getMaxNotional() != null && orderValue > market.getMaxNotional()) {
                throw new TradingPlatformException("ABOVE_MAX_NOTIONAL");
            }
        }

        String orderId = UUID.randomUUID().toString();
        EngineOrder engineOrder = new EngineOrder(
                orderId,
                req.getUserId().trim(),
                type,
                side,
                symbol,
                price,
                quantity,
                System.currentTimeMillis()
        );

        List<TradeEvent> trades;
        try {
            trades = matchingEngine.placeOrder(engineOrder);
        } catch (TradingPlatformException e) {
            throw e;
        } catch (Exception e) {
            log.error("Execution failed for order: {}", orderId, e);
            throw new TradingPlatformException("ORDER_EXECUTION_FAILED");
        }

        // Persist order
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(orderId);
        orderEntity.setUserId(req.getUserId().trim());
        orderEntity.setSymbol(symbol);
        orderEntity.setSide(side);
        orderEntity.setType(type);
        orderEntity.setPrice(price);
        orderEntity.setQuantity(quantity);
        orderEntity.setRemaining(engineOrder.getRemaining());
        orderEntity.setFilledQuantity(engineOrder.getFilledQuantity());
        orderEntity.setAvgPrice(engineOrder.getAvgPrice());
        orderEntity.setStatus(engineOrder.getStatus());
        orderEntity.setFees(0.0);

        orderRepository.save(orderEntity);

        // Persist executed trades & update market stats
        for (TradeEvent t : trades) {
            tradeService.saveTrade(t);
            marketService.updateMarketPriceAndVolume(t.getSymbol(), t.getPrice(), t.getQuantity());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("orderId", orderId);
        result.put("status", engineOrder.getStatus().name());
        result.put("filledQuantity", engineOrder.getFilledQuantity());
        result.put("avgPrice", engineOrder.getAvgPrice());
        result.put("trades", trades);

        return result;
    }

    @Transactional
    public Map<String, Object> cancelOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new TradingPlatformException("ORDER_ID_REQUIRED");
        }

        OrderEntity order = orderRepository.findByOrderId(orderId.trim())
                .orElseThrow(() -> new TradingPlatformException("ORDER_NOT_FOUND"));

        if (order.getStatus() != OrderStatus.OPEN && order.getStatus() != OrderStatus.PARTIALLY_FILLED) {
            throw new TradingPlatformException("ORDER_NOT_OPEN");
        }

        try {
            matchingEngine.cancelOrder(orderId.trim());
        } catch (TradingPlatformException e) {
            log.warn("Matching engine cancel note: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Engine cancel error: {}", e.getMessage());
            throw new TradingPlatformException("CANCEL_FAILED");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setRemaining(0.0);
        orderRepository.save(order);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("orderId", orderId);
        return res;
    }

    public OrderEntity getOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new TradingPlatformException("ORDER_ID_REQUIRED");
        }
        return orderRepository.findByOrderId(orderId.trim())
                .orElseThrow(() -> new TradingPlatformException("ORDER_NOT_FOUND"));
    }

    public List<OrderEntity> getAllOrders(String userId, String symbol, OrderStatus status, int limit) {
        int maxLimit = (limit > 0) ? Math.min(limit, 500) : 100;
        String sym = (symbol != null && !symbol.trim().isEmpty()) ? symbol.toUpperCase().trim() : null;
        String uid = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : null;
        return orderRepository.findOrdersWithFilter(uid, sym, status, PageRequest.of(0, maxLimit));
    }

    public OrderBookDepth getDepth(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new TradingPlatformException("SYMBOL_REQUIRED");
        }
        return matchingEngine.getDepth(symbol.toUpperCase().trim());
    }

    private double round(double val, int precision) {
        return BigDecimal.valueOf(val)
                .setScale(precision, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
