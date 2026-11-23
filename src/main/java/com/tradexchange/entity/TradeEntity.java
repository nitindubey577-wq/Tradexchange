package com.tradexchange.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "trades", indexes = {
    @Index(name = "idx_trades_trade_id", columnList = "trade_id", unique = true),
    @Index(name = "idx_trades_symbol_timestamp", columnList = "symbol, timestamp"),
    @Index(name = "idx_trades_buy_order_timestamp", columnList = "buy_order_id, timestamp"),
    @Index(name = "idx_trades_sell_order_timestamp", columnList = "sell_order_id, timestamp"),
    @Index(name = "idx_trades_symbol_price", columnList = "symbol, price")
})
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", nullable = false, unique = true, length = 64)
    private String tradeId;

    @Column(name = "buy_order_id", nullable = false, length = 64)
    private String buyOrderId;

    @Column(name = "sell_order_id", nullable = false, length = 64)
    private String sellOrderId;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "`value`")
    private Double value;

    @Column(name = "buy_fee")
    private Double buyFee = 0.0;

    @Column(name = "sell_fee")
    private Double sellFee = 0.0;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp = Instant.now();

    public TradeEntity() {}

    @PrePersist
    @PreUpdate
    public void calculateValueAndNormalize() {
        if (price != null && quantity != null) {
            this.value = price * quantity;
        }
        if (symbol != null) {
            symbol = symbol.toUpperCase().trim();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTradeId() { return tradeId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }

    public String getBuyOrderId() { return buyOrderId; }
    public void setBuyOrderId(String buyOrderId) { this.buyOrderId = buyOrderId; }

    public String getSellOrderId() { return sellOrderId; }
    public void setSellOrderId(String sellOrderId) { this.sellOrderId = sellOrderId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public Double getBuyFee() { return buyFee; }
    public void setBuyFee(Double buyFee) { this.buyFee = buyFee; }

    public Double getSellFee() { return sellFee; }
    public void setSellFee(Double sellFee) { this.sellFee = sellFee; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
