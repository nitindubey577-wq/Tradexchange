package com.tradexchange.repository;

import com.tradexchange.entity.OrderEntity;
import com.tradexchange.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderId(String orderId);

    @Query("SELECT o FROM OrderEntity o WHERE " +
           "(:userId IS NULL OR o.userId = :userId) AND " +
           "(:symbol IS NULL OR o.symbol = :symbol) AND " +
           "(:status IS NULL OR o.status = :status) " +
           "ORDER BY o.createdAt DESC")
    List<OrderEntity> findOrdersWithFilter(@Param("userId") String userId,
                                          @Param("symbol") String symbol,
                                          @Param("status") OrderStatus status,
                                          Pageable pageable);
}
