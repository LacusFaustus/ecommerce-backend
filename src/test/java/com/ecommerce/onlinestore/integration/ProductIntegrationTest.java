package com.ecommerce.onlinestore.integration;

import com.ecommerce.onlinestore.model.entity.Product;
import com.ecommerce.onlinestore.repository.ProductRepository;
import com.ecommerce.onlinestore.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    private List<Product> sampleProducts;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        sampleProducts = TestDataFactory.createSampleProducts();
        productRepository.saveAll(sampleProducts);
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[0].name", is("Laptop")))
                .andExpect(jsonPath("$.content[1].name", is("Programming Book"))) // Исправлен порядок
                .andExpect(jsonPath("$.content[2].name", is("Smartphone")))
                .andExpect(jsonPath("$.content[3].name", is("Wireless Headphones")));
    }

    @Test
    void shouldGetProductById() throws Exception {
        Product product = sampleProducts.get(0);

        mockMvc.perform(get("/api/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(product.getId().intValue())))
                .andExpect(jsonPath("$.name", is(product.getName())))
                .andExpect(jsonPath("$.price", is(product.getPrice().doubleValue())));
    }

    @Test
    void shouldReturnNotFoundForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetProductsByCategory() throws Exception {
        mockMvc.perform(get("/api/products/category/Electronics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].category", everyItem(is("Electronics"))));
    }

    @Test
    void shouldSearchProducts() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("name", "laptop")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Laptop")));
    }

    @Test
    void shouldFilterProductsByPriceRange() throws Exception {
        mockMvc.perform(get("/api/products/filter/price")
                        .param("minPrice", "100")
                        .param("maxPrice", "500")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Smartphone", "Wireless Headphones")));
    }

    @Test
    void shouldGetAvailableProducts() throws Exception {
        // Create a product with zero stock
        Product outOfStockProduct = TestDataFactory.createProduct(
                5L, "Out of Stock Item", "Electronics", BigDecimal.valueOf(199.99), 0);
        productRepository.save(outOfStockProduct);

        mockMvc.perform(get("/api/products/available")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4))) // Only the original 4 products
                .andExpect(jsonPath("$.content[*].stockQuantity", everyItem(greaterThan(0))));
    }
}
