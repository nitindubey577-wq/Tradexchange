package com.tradexchange.engine;

import com.tradexchange.enums.OrderSide;
import com.tradexchange.enums.OrderType;
import com.tradexchange.exception.TradingPlatformException;

public class OrderValidator {

    public static void validate(EngineOrder order) {
        if (order == null) {
            throw new TradingPlatformException("INVALID_ORDER");
        }
        if (order.getId() == null || order.getId().trim().isEmpty()) {
            throw new TradingPlatformException("ORDER_ID_REQUIRED");
        }
        if (order.getUserId() == null || order.getUserId().trim().isEmpty()) {
            throw new TradingPlatformException("USER_ID_REQUIRED");
        }
        if (order.getSymbol() == null || order.getSymbol().trim().isEmpty()) {
            throw new TradingPlatformException("SYMBOL_REQUIRED");
        }
        if (order.getSide() == null) {
            throw new TradingPlatformException("INVALID_SIDE");
        }
        if (order.getType() == null) {
            throw new TradingPlatformException("INVALID_TYPE");
        }
        if (Double.isNaN(order.getQuantity()) || order.getQuantity() <= 0) {
            throw new TradingPlatformException("INVALID_QUANTITY");
        }
        if (order.getType() == OrderType.LIMIT) {
            if (order.getPrice() == null || Double.isNaN(order.getPrice()) || order.getPrice() <= 0) {
                throw new TradingPlatformException("INVALID_PRICE");
            }
        }
    }
}
