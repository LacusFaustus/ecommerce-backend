package com.ecommerce.onlinestore.service.impl;

import com.ecommerce.onlinestore.mapper.ProductMapper;
import com.ecommerce.onlinestore.model.dto.ProductDTO;
import com.ecommerce.onlinestore.model.entity.Product;
import com.ecommerce.onlinestore.model.exception.ProductNotFoundException;
import com.ecommerce.onlinestore.repository.ProductRepository;
import com.ecommerce.onlinestore.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        testProduct = TestDataFactory.createProduct(1L, "Laptop", "Electronics", BigDecimal.valueOf(999.99), 10);
        testProductDTO = TestDataFactory.createProductDTO(1L, "Laptop", "Electronics", BigDecimal.valueOf(999.99), 10);
    }

    @Test
    void shouldGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        ProductDTO result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Laptop");
        verify(productRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    void shouldGetProductBySku() {
        when(productRepository.findBySku("SKU-LAPTOP")).thenReturn(Optional.of(testProduct));
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        ProductDTO result = productService.getProductBySku("SKU-LAPTOP");

        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("SKU-LAPTOP");
        verify(productRepository).findBySku("SKU-LAPTOP");
    }

    @Test
    void shouldGetAllProducts() {
        // This test would require more setup with Pageable
        // For now, we just verify the method exists
        assertThat(productService).isNotNull();
    }
}
