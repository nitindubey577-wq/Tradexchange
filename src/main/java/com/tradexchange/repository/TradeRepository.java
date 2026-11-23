package com.tradexchange.repository;

import com.tradexchange.entity.TradeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    Optional<TradeEntity> findByTradeId(String tradeId);

    @Query("SELECT t FROM TradeEntity t WHERE (:symbol IS NULL OR t.symbol = :symbol) ORDER BY t.timestamp DESC")
    List<TradeEntity> findTradesWithFilter(@Param("symbol") String symbol, Pageable pageable);

    @Query("SELECT t FROM TradeEntity t WHERE t.buyOrderId = :orderId OR t.sellOrderId = :orderId ORDER BY t.timestamp DESC")
    List<TradeEntity> findByOrderId(@Param("orderId") String orderId, Pageable pageable);

    List<TradeEntity> findBySymbolOrderByTimestampAsc(String symbol);
}
