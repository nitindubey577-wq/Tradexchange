package com.tradexchange.dto.request;

import jakarta.validation.constraints.NotNull;

public class WatchlistToggleRequest {

    @NotNull(message = "SYMBOL_REQUIRED")
    private String symbol;

    private String userId;

    public WatchlistToggleRequest() {}

    public WatchlistToggleRequest(String symbol, String userId) {
        this.symbol = symbol;
        this.userId = userId;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
