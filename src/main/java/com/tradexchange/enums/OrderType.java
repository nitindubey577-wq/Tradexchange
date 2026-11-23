package com.tradexchange.enums;

public enum OrderType {
    LIMIT,
    MARKET;

    public static OrderType fromString(String value) {
        if (value == null) return null;
        return OrderType.valueOf(value.trim().toUpperCase());
    }
}
