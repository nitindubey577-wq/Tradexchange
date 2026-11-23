package com.tradexchange.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_watchlists", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "symbol", length = 50)
    private Set<String> watchlist = new LinkedHashSet<>();

    @Column(columnDefinition = "TEXT")
    private String balances = "{}";

    @Column(name = "is_active", nullable = false)
    @JsonProperty("isActive")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;

    public UserEntity() {}

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (email != null) email = email.toLowerCase().trim();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Set<String> getWatchlist() { return watchlist; }
    public void setWatchlist(Set<String> watchlist) { this.watchlist = watchlist; }

    public String getBalances() { return balances; }
    public void setBalances(String balances) { this.balances = balances; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
