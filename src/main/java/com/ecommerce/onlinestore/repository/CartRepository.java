package com.ecommerce.onlinestore.repository;

import com.ecommerce.onlinestore.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findBySessionId(String sessionId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product WHERE c.id = :id")
    Optional<Cart> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product WHERE c.sessionId = :sessionId")
    Optional<Cart> findBySessionIdWithItems(@Param("sessionId") String sessionId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.updatedAt < :cutoffDate")
    void deleteOldCarts(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.updatedAt > :since")
    Long countActiveCarts(@Param("since") LocalDateTime since);
}
