package com.ecommerce.onlinestore.repository;

import com.ecommerce.onlinestore.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceBetween(@Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByCategoryAndPriceBetween(@Param("category") String category,
                                                @Param("minPrice") BigDecimal minPrice,
                                                @Param("maxPrice") BigDecimal maxPrice,
                                                Pageable pageable);

    List<Product> findByStockQuantityGreaterThan(Integer quantity);

    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    Page<Product> findAvailableProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id IN :productIds")
    List<Product> findByIdIn(@Param("productIds") List<Long> productIds);
}
