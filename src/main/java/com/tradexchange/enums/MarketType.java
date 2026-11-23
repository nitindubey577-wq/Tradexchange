package com.tradexchange.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MarketType {
    CRYPTO("crypto"),
    STOCK("stock"),
    COMMODITY("commodity");

    private final String value;

    MarketType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MarketType fromString(String text) {
        if (text == null) return null;
        for (MarketType b : MarketType.values()) {
            if (b.value.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
