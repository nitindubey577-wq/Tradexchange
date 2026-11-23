package com.tradexchange.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tradexchange.enums.OrderSide;
import com.tradexchange.enums.OrderStatus;
import com.tradexchange.enums.OrderType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_order_id", columnList = "order_id", unique = true),
    @Index(name = "idx_orders_symbol_status", columnList = "symbol, status"),
    @Index(name = "idx_orders_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_orders_symbol_created", columnList = "symbol, created_at")
})
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 50)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderType type;

    private Double price;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Double remaining;

    @Column(name = "filled_quantity")
    private Double filledQuantity = 0.0;

    @Column(name = "avg_price")
    private Double avgPrice = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.OPEN;

    private Double fees = 0.0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;

    public OrderEntity() {}

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (symbol != null) symbol = symbol.toUpperCase().trim();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public OrderSide getSide() { return side; }
    public void setSide(OrderSide side) { this.side = side; }

    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Double getRemaining() { return remaining; }
    public void setRemaining(Double remaining) { this.remaining = remaining; }

    public Double getFilledQuantity() { return filledQuantity; }
    public void setFilledQuantity(Double filledQuantity) { this.filledQuantity = filledQuantity; }

    public Double getAvgPrice() { return avgPrice; }
    public void setAvgPrice(Double avgPrice) { this.avgPrice = avgPrice; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Double getFees() { return fees; }
    public void setFees(Double fees) { this.fees = fees; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
