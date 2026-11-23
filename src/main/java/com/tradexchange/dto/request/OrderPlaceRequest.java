package com.tradexchange.dto.request;

import jakarta.validation.constraints.NotNull;

public class OrderPlaceRequest {

    @NotNull(message = "USER_ID_REQUIRED")
    private String userId;

    @NotNull(message = "SYMBOL_REQUIRED")
    private String symbol;

    @NotNull(message = "INVALID_SIDE")
    private String side;

    @NotNull(message = "INVALID_TYPE")
    private String type;

    private Double price;

    @NotNull(message = "INVALID_QUANTITY")
    private Double quantity;

    public OrderPlaceRequest() {}

    public OrderPlaceRequest(String userId, String symbol, String side, String type, Double price, Double quantity) {
        this.userId = userId;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
}
