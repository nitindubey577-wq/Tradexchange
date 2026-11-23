package com.tradexchange.enums;

public enum OrderSide {
    BUY,
    SELL;

    public static OrderSide fromString(String value) {
        if (value == null) return null;
        return OrderSide.valueOf(value.trim().toUpperCase());
    }
}
