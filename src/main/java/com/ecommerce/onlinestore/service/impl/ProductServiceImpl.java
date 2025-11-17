package com.ecommerce.onlinestore.service.impl;

import com.ecommerce.onlinestore.mapper.ProductMapper;
import com.ecommerce.onlinestore.model.dto.ProductDTO;
import com.ecommerce.onlinestore.model.entity.Product;
import com.ecommerce.onlinestore.model.exception.ProductNotFoundException;
import com.ecommerce.onlinestore.repository.ProductRepository;
import com.ecommerce.onlinestore.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products with pageable: {}", pageable);
        return productRepository.findAll(pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product by id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toDTO(product);
    }

    @Override
    public Page<ProductDTO> getProductsByCategory(String category, Pageable pageable) {
        log.debug("Fetching products by category: {}", category);
        return productRepository.findByCategory(category, pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> searchProducts(String name, Pageable pageable) {
        log.debug("Searching products by name: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching products by price range: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByCategoryAndPriceRange(String category, BigDecimal minPrice,
                                                               BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching products by category: {} and price range: {} - {}", category, minPrice, maxPrice);
        return productRepository.findByCategoryAndPriceBetween(category, minPrice, maxPrice, pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> searchByKeyword(String keyword, Pageable pageable) {
        log.debug("Searching products by keyword: {}", keyword);
        return productRepository.searchByKeyword(keyword, pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> getAvailableProducts(Pageable pageable) {
        log.debug("Fetching available products (in stock)");
        return productRepository.findAvailableProducts(pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public ProductDTO getProductBySku(String sku) {
        log.debug("Fetching product by SKU: {}", sku);
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        return productMapper.toDTO(product);
    }
}
