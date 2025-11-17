package com.ecommerce.onlinestore.integration;

import com.ecommerce.onlinestore.model.entity.Cart;
import com.ecommerce.onlinestore.model.entity.Product;
import com.ecommerce.onlinestore.repository.CartRepository;
import com.ecommerce.onlinestore.repository.ProductRepository;
import com.ecommerce.onlinestore.util.JsonUtils;
import com.ecommerce.onlinestore.util.TestDataFactory;
import com.ecommerce.onlinestore.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    private List<Product> sampleProducts;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        // Clear data
        cartRepository.deleteAll();
        productRepository.deleteAll();

        // Create test data
        sampleProducts = TestDataFactory.createSampleProducts();
        productRepository.saveAll(sampleProducts);

        testCart = TestDataFactory.createCart(null, TestConstants.TEST_SESSION_ID);
        testCart = cartRepository.save(testCart);
    }

    @Test
    void shouldCreateCart() throws Exception {
        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", notNullValue()))
                .andExpect(jsonPath("$.items", empty()));
    }

    @Test
    void shouldGetCartById() throws Exception {
        mockMvc.perform(get("/api/carts/{cartId}", testCart.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCart.getId().intValue())))
                .andExpect(jsonPath("$.sessionId", is(TestConstants.TEST_SESSION_ID)));
    }

    @Test
    void shouldAddItemToCart() throws Exception {
        Product product = sampleProducts.get(0);
        String addItemRequest = JsonUtils.toJson(TestDataFactory.createAddToCartRequest(product.getId(), 2));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCart.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productId", is(product.getId().intValue())))
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andExpect(jsonPath("$.totalItems", is(2)))
                .andExpect(jsonPath("$.totalPrice", is(1999.98))); // 999.99 * 2
    }

    @Test
    void shouldNotAddItemWithInsufficientStock() throws Exception {
        Product product = sampleProducts.get(0);
        // Try to add more than available stock
        String addItemRequest = JsonUtils.toJson(TestDataFactory.createAddToCartRequest(product.getId(), 20));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCart.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Insufficient stock")));
    }
}
