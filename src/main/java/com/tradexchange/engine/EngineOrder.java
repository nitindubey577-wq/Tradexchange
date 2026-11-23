package com.tradexchange.engine;

import com.tradexchange.enums.OrderSide;
import com.tradexchange.enums.OrderStatus;
import com.tradexchange.enums.OrderType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EngineOrder {
    private String id;
    private String userId;
    private OrderType type;
    private OrderSide side;
    private String symbol;
    private Double price;
    private double quantity;
    private double remaining;
    private double filledQuantity;
    private double avgPrice;
    private Double lastPrice;
    private OrderStatus status;
    private boolean active;
    private long seq;
    private long timestamp;

    public EngineOrder() {}

    public EngineOrder(String id, String userId, OrderType type, OrderSide side, String symbol, Double price, double quantity, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.side = side;
        this.symbol = symbol != null ? symbol.toUpperCase().trim() : null;
        this.price = (type == OrderType.MARKET) ? null : price;
        this.quantity = quantity;
        this.remaining = quantity;
        this.filledQuantity = 0.0;
        this.avgPrice = 0.0;
        this.lastPrice = null;
        this.status = OrderStatus.OPEN;
        this.active = true;
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    public void applyFill(double fillPrice, double qty) {
        if (!this.active || qty <= 0) return;

        double fillQty = Math.min(qty, this.remaining);
        if (fillQty <= 0) return;

        double previousFilled = this.filledQuantity;
        this.remaining -= fillQty;
        this.filledQuantity += fillQty;

        // VWAP average price
        double totalCost = (this.avgPrice * previousFilled) + (fillPrice * fillQty);
        this.avgPrice = BigDecimal.valueOf(totalCost / this.filledQuantity)
                .setScale(8, RoundingMode.HALF_UP)
                .doubleValue();

        this.lastPrice = fillPrice;

        if (this.remaining <= 0.000000001) {
            this.remaining = 0.0;
            this.status = OrderStatus.FILLED;
            this.active = false;
        } else if (this.remaining < this.quantity) {
            this.status = OrderStatus.PARTIALLY_FILLED;
        }
    }

    public void cancel() {
        if (!this.active) return;
        this.active = false;
        this.status = OrderStatus.CANCELLED;
        this.remaining = 0.0;
    }

    public boolean isFilled() {
        return this.remaining <= 0.000000001;
    }

    public boolean isOpen() {
        return this.active && this.remaining > 0;
    }

    public boolean isMarket() {
        return this.type == OrderType.MARKET;
    }

    public boolean isLimit() {
        return this.type == OrderType.LIMIT;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }

    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getRemaining() { return remaining; }
    public void setRemaining(double remaining) { this.remaining = remaining; }

    public double getFilledQuantity() { return filledQuantity; }
    public void setFilledQuantity(double filledQuantity) { this.filledQuantity = filledQuantity; }

    public double getAvgPrice() { return avgPrice; }
    public void setAvgPrice(double avgPrice) { this.avgPrice = avgPrice; }

    public Double getLastPrice() { return lastPrice; }
    public void setLastPrice(Double lastPrice) { this.lastPrice = lastPrice; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getSeq() { return seq; }
    public void setSeq(long seq) { this.seq = seq; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
