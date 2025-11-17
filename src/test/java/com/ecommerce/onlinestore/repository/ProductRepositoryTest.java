package com.ecommerce.onlinestore.repository;

import com.ecommerce.onlinestore.model.entity.Product;
import com.ecommerce.onlinestore.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

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
    void shouldFindAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> products = productRepository.findAll(pageable);

        assertThat(products.getContent()).hasSize(4);
    }

    @Test
    void shouldFindProductsByCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> electronics = productRepository.findByCategory("Electronics", pageable);

        assertThat(electronics.getContent()).hasSize(3);
        assertThat(electronics.getContent())
                .extracting(Product::getCategory)
                .containsOnly("Electronics");
    }

    @Test
    void shouldFindProductsByNameContaining() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> products = productRepository.findByNameContainingIgnoreCase("laptop", pageable);

        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void shouldFindProductsByPriceRange() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> products = productRepository.findByPriceBetween(
                BigDecimal.valueOf(100), BigDecimal.valueOf(500), pageable);

        assertThat(products.getContent()).hasSize(2); // Smartphone and Headphones
    }

    @Test
    void shouldFindAvailableProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> products = productRepository.findAvailableProducts(pageable);

        assertThat(products.getContent()).hasSize(4);
        assertThat(products.getContent())
                .allMatch(product -> product.getStockQuantity() > 0);
    }

    @Test
    void shouldFindProductBySku() {
        Product laptop = sampleProducts.get(0);
        Optional<Product> found = productRepository.findBySku("SKU-LAPTOP");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Laptop");
    }

    @Test
    void shouldSearchByKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        // Search for "phone" should find "Smartphone" only
        Page<Product> products = productRepository.searchByKeyword("smart", pageable);

        assertThat(products.getContent()).hasSize(1);
        assertThat(products.getContent().get(0).getName()).isEqualTo("Smartphone");
    }

    @Test
    void shouldFindProductsWithSufficientStock() {
        List<Product> products = productRepository.findByStockQuantityGreaterThan(10);

        // Laptop (10) - not included, Smartphone (15) - included, Book (50) - included, Headphones (20) - included
        assertThat(products).hasSize(3);
        assertThat(products)
                .allMatch(product -> product.getStockQuantity() > 10);
    }
}
