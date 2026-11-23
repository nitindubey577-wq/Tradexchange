package com.tradexchange.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MarketStatus {
    ACTIVE("active"),
    HALTED("halted"),
    CLOSED("closed");

    private final String value;

    MarketStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MarketStatus fromString(String text) {
        if (text == null) return null;
        for (MarketStatus b : MarketStatus.values()) {
            if (b.value.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
