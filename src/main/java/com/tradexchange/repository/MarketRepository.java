package com.tradexchange.repository;

import com.tradexchange.entity.MarketEntity;
import com.tradexchange.enums.MarketType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<MarketEntity, Long> {

    Optional<MarketEntity> findBySymbol(String symbol);

    boolean existsBySymbol(String symbol);

    List<MarketEntity> findByTypeAndIsActive(MarketType type, Boolean isActive);

    @Query("SELECT m FROM MarketEntity m WHERE " +
           "(:isActive IS NULL OR m.isActive = :isActive) AND " +
           "(:type IS NULL OR m.type = :type) AND " +
           "(:search IS NULL OR UPPER(m.symbol) LIKE CONCAT('%', UPPER(:search), '%')) " +
           "ORDER BY m.symbol ASC")
    List<MarketEntity> findMarketsWithFilter(@Param("isActive") Boolean isActive,
                                            @Param("type") MarketType type,
                                            @Param("search") String search,
                                            Pageable pageable);
}
