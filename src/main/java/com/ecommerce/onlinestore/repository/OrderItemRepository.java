package com.ecommerce.onlinestore.repository;

import com.ecommerce.onlinestore.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.productId = :productId")
    List<OrderItem> findByProductId(@Param("productId") Long productId);

    @Query("SELECT oi FROM OrderItem oi JOIN oi.order o WHERE oi.productId = :productId AND o.status = 'DELIVERED'")
    List<OrderItem> findDeliveredByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Long getTotalQuantitySoldByProductId(@Param("productId") Long productId);

    @Query("SELECT oi.productId, SUM(oi.quantity) as totalSold FROM OrderItem oi GROUP BY oi.productId ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts();
}
