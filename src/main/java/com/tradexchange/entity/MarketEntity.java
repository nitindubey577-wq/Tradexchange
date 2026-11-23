package com.tradexchange.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tradexchange.enums.ContractType;
import com.tradexchange.enums.MarketStatus;
import com.tradexchange.enums.MarketType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "markets", indexes = {
    @Index(name = "idx_market_type_active", columnList = "type, is_active"),
    @Index(name = "idx_market_symbol_active", columnList = "symbol, is_active"),
    @Index(name = "idx_market_category", columnList = "category")
})
public class MarketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String symbol;

    @Column(name = "base_asset", nullable = false, length = 20)
    private String baseAsset;

    @Column(name = "quote_asset", nullable = false, length = 20)
    private String quoteAsset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MarketType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 20)
    private ContractType contractType = ContractType.SPOT;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Column(name = "contract_size")
    private Double contractSize = 1.0;

    @Column(length = 20)
    private String unit;

    @Column(name = "min_qty")
    private Double minQty = 0.000001;

    @Column(name = "max_qty")
    private Double maxQty = 100000.0;

    @Column(name = "lot_size")
    private Double lotSize = 0.000001;

    @Column(name = "min_price")
    private Double minPrice = 0.0;

    @Column(name = "max_price")
    private Double maxPrice = 100000000.0;

    @Column(name = "tick_size")
    private Double tickSize = 0.01;

    @Column(name = "min_notional")
    private Double minNotional = 100.0;

    @Column(name = "max_notional")
    private Double maxNotional = 100000000.0;

    @Column(name = "price_precision")
    private Integer pricePrecision = 2;

    @Column(name = "quantity_precision")
    private Integer quantityPrecision = 6;

    @Column(name = "maker_fee")
    private Double makerFee = 0.001;

    @Column(name = "taker_fee")
    private Double takerFee = 0.002;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MarketStatus status = MarketStatus.ACTIVE;

    @Column(name = "is_active", nullable = false)
    @JsonProperty("isActive")
    private Boolean isActive = true;

    @Column(name = "last_price")
    private Double lastPrice;

    @Column(name = "volume_24h")
    @JsonProperty("volume24h")
    private Double volume24h = 0.0;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(length = 255)
    private String logo;

    @Column(length = 50)
    private String category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;

    public MarketEntity() {}

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (symbol != null) symbol = symbol.toUpperCase().trim();
        if (baseAsset != null) baseAsset = baseAsset.toUpperCase().trim();
        if (quoteAsset != null) quoteAsset = quoteAsset.toUpperCase().trim();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getBaseAsset() { return baseAsset; }
    public void setBaseAsset(String baseAsset) { this.baseAsset = baseAsset; }

    public String getQuoteAsset() { return quoteAsset; }
    public void setQuoteAsset(String quoteAsset) { this.quoteAsset = quoteAsset; }

    public MarketType getType() { return type; }
    public void setType(MarketType type) { this.type = type; }

    public ContractType getContractType() { return contractType; }
    public void setContractType(ContractType contractType) { this.contractType = contractType; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public Double getContractSize() { return contractSize; }
    public void setContractSize(Double contractSize) { this.contractSize = contractSize; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getMinQty() { return minQty; }
    public void setMinQty(Double minQty) { this.minQty = minQty; }

    public Double getMaxQty() { return maxQty; }
    public void setMaxQty(Double maxQty) { this.maxQty = maxQty; }

    public Double getLotSize() { return lotSize; }
    public void setLotSize(Double lotSize) { this.lotSize = lotSize; }

    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public Double getTickSize() { return tickSize; }
    public void setTickSize(Double tickSize) { this.tickSize = tickSize; }

    public Double getMinNotional() { return minNotional; }
    public void setMinNotional(Double minNotional) { this.minNotional = minNotional; }

    public Double getMaxNotional() { return maxNotional; }
    public void setMaxNotional(Double maxNotional) { this.maxNotional = maxNotional; }

    public Integer getPricePrecision() { return pricePrecision; }
    public void setPricePrecision(Integer pricePrecision) { this.pricePrecision = pricePrecision; }

    public Integer getQuantityPrecision() { return quantityPrecision; }
    public void setQuantityPrecision(Integer quantityPrecision) { this.quantityPrecision = quantityPrecision; }

    public Double getMakerFee() { return makerFee; }
    public void setMakerFee(Double makerFee) { this.makerFee = makerFee; }

    public Double getTakerFee() { return takerFee; }
    public void setTakerFee(Double takerFee) { this.takerFee = takerFee; }

    public MarketStatus getStatus() { return status; }
    public void setStatus(MarketStatus status) { this.status = status; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Double getLastPrice() { return lastPrice; }
    public void setLastPrice(Double lastPrice) { this.lastPrice = lastPrice; }

    public Double getVolume24h() { return volume24h; }
    public void setVolume24h(Double volume24h) { this.volume24h = volume24h; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
