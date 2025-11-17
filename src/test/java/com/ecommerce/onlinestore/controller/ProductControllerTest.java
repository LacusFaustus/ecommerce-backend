package com.ecommerce.onlinestore.controller;

import com.ecommerce.onlinestore.model.dto.ProductDTO;
import com.ecommerce.onlinestore.model.exception.ProductNotFoundException;
import com.ecommerce.onlinestore.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void shouldGetAllProducts() throws Exception {
        // Given
        ProductDTO product1 = ProductDTO.builder()
                .id(1L)
                .name("Laptop")
                .price(BigDecimal.valueOf(999.99))
                .stockQuantity(10)
                .category("Electronics")
                .build();

        ProductDTO product2 = ProductDTO.builder()
                .id(2L)
                .name("Smartphone")
                .price(BigDecimal.valueOf(499.99))
                .stockQuantity(15)
                .category("Electronics")
                .build();

        Page<ProductDTO> productPage = new PageImpl<>(List.of(product1, product2), PageRequest.of(0, 10), 2);

        when(productService.getAllProducts(any(PageRequest.class))).thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.content[1].name").value("Smartphone"));
    }

    @Test
    void shouldGetProductById() throws Exception {
        // Given
        ProductDTO product = ProductDTO.builder()
                .id(1L)
                .name("Laptop")
                .price(BigDecimal.valueOf(999.99))
                .stockQuantity(10)
                .category("Electronics")
                .build();

        when(productService.getProductById(1L)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    void shouldReturnNotFoundForNonExistentProduct() throws Exception {
        // Given
        when(productService.getProductById(999L)).thenThrow(
                new ProductNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }
}
