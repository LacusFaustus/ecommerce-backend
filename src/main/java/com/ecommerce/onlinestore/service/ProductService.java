package com.ecommerce.onlinestore.service;

import com.ecommerce.onlinestore.model.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {

    Page<ProductDTO> getAllProducts(Pageable pageable);

    ProductDTO getProductById(Long id);

    Page<ProductDTO> getProductsByCategory(String category, Pageable pageable);

    Page<ProductDTO> searchProducts(String name, Pageable pageable);

    Page<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<ProductDTO> getProductsByCategoryAndPriceRange(String category, BigDecimal minPrice,
                                                        BigDecimal maxPrice, Pageable pageable);

    Page<ProductDTO> searchByKeyword(String keyword, Pageable pageable);

    Page<ProductDTO> getAvailableProducts(Pageable pageable);

    ProductDTO getProductBySku(String sku);
}
