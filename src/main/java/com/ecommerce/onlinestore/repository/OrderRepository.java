package com.ecommerce.onlinestore.repository;

import com.ecommerce.onlinestore.model.entity.Order;
import com.ecommerce.onlinestore.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByCustomerInfo_CustomerEmail(String customerEmail, Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.customerInfo.customerEmail = :email AND o.status = :status")
    Page<Order> findByCustomerEmailAndStatus(@Param("email") String email,
                                             @Param("status") OrderStatus status,
                                             Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED'")
    BigDecimal getTotalRevenueInPeriod(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    List<Order> findByStatusIn(List<OrderStatus> statuses);
}
