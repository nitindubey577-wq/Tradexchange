package com.tradexchange.engine;

import java.util.ArrayList;
import java.util.List;

public class OrderBookDepth {

    public static class Level {
        private double price;
        private double quantity;

        public Level() {}

        public Level(double price, double quantity) {
            this.price = price;
            this.quantity = quantity;
        }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
    }

    private List<Level> bids = new ArrayList<>();
    private List<Level> asks = new ArrayList<>();
    private Double lastTradedPrice;

    public OrderBookDepth() {}

    public OrderBookDepth(List<Level> bids, List<Level> asks, Double lastTradedPrice) {
        this.bids = bids != null ? bids : new ArrayList<>();
        this.asks = asks != null ? asks : new ArrayList<>();
        this.lastTradedPrice = lastTradedPrice;
    }

    public List<Level> getBids() { return bids; }
    public void setBids(List<Level> bids) { this.bids = bids; }

    public List<Level> getAsks() { return asks; }
    public void setAsks(List<Level> asks) { this.asks = asks; }

    public Double getLastTradedPrice() { return lastTradedPrice; }
    public void setLastTradedPrice(Double lastTradedPrice) { this.lastTradedPrice = lastTradedPrice; }
}
