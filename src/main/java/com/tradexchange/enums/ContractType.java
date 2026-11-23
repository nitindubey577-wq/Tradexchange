package com.tradexchange.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ContractType {
    SPOT("spot"),
    FUTURES("futures");

    private final String value;

    ContractType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ContractType fromString(String text) {
        if (text == null) return null;
        for (ContractType b : ContractType.values()) {
            if (b.value.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
