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
class FullOrderFlowIntegrationTest {

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
        cartRepository.deleteAll();
        productRepository.deleteAll();

        sampleProducts = TestDataFactory.createSampleProducts();
        productRepository.saveAll(sampleProducts);

        testCart = TestDataFactory.createCart(null, TestConstants.TEST_SESSION_ID);
        cartRepository.save(testCart);
    }

    @Test
    void shouldCompleteFullOrderFlow() throws Exception {
        // 1. Add items to cart
        Product laptop = findProductByName("Laptop"); // 999.99
        Product smartphone = findProductByName("Smartphone"); // 499.99

        String addLaptopRequest = JsonUtils.toJson(TestDataFactory.createAddToCartRequest(laptop.getId(), 1));
        String addPhoneRequest = JsonUtils.toJson(TestDataFactory.createAddToCartRequest(smartphone.getId(), 1)); // Changed to 1 item

        mockMvc.perform(post("/api/carts/{cartId}/items", testCart.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(addLaptopRequest));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCart.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(addPhoneRequest));

        // Verify cart contents
        mockMvc.perform(get("/api/carts/{cartId}", testCart.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.totalItems", is(2))) // 1 + 1 = 2
                .andExpect(jsonPath("$.totalPrice", is(1499.98))); // 999.99 + 499.99 = 1499.98

        // 2. Create order
        String createOrderRequest = JsonUtils.toJson(TestDataFactory.createCreateOrderRequest(testCart.getId()));

        var orderResult = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber", notNullValue()))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(1499.98))) // Same as cart total
                .andExpect(jsonPath("$.orderItems", hasSize(2)))
                .andExpect(jsonPath("$.customerInfo.customerEmail", is(TestConstants.TEST_CUSTOMER_EMAIL)))
                .andReturn();

        // Extract order number from response
        String orderResponse = orderResult.getResponse().getContentAsString();
        String orderNumber = JsonUtils.fromJson(orderResponse, com.fasterxml.jackson.databind.JsonNode.class)
                .path("orderNumber").asText();

        // 3. Verify cart is cleared after order
        mockMvc.perform(get("/api/carts/{cartId}", testCart.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", empty()))
                .andExpect(jsonPath("$.totalItems", is(0)))
                .andExpect(jsonPath("$.totalPrice", is(0)));

        // 4. Verify product stock is updated
        mockMvc.perform(get("/api/products/{id}", laptop.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity", is(9))); // Was 10, ordered 1

        mockMvc.perform(get("/api/products/{id}", smartphone.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity", is(14))); // Was 15, ordered 1

        // 5. Get order by number
        mockMvc.perform(get("/api/orders/number/{orderNumber}", orderNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber", is(orderNumber)))
                .andExpect(jsonPath("$.orderItems", hasSize(2)));

        // 6. Update order status
        mockMvc.perform(put("/api/orders/{orderId}/status", 1)
                        .param("status", "CONFIRMED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    void shouldNotCreateOrderFromEmptyCart() throws Exception {
        String createOrderRequest = JsonUtils.toJson(TestDataFactory.createCreateOrderRequest(testCart.getId()));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("empty cart")));
    }

    @Test
    void shouldNotCreateOrderWithInsufficientStock() throws Exception {
        // Add more items than available stock
        Product laptop = findProductByName("Laptop");
        String addItemRequest = JsonUtils.toJson(TestDataFactory.createAddToCartRequest(laptop.getId(), 20)); // Only 10 available

        mockMvc.perform(post("/api/carts/{cartId}/items", testCart.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemRequest))
                .andExpect(status().isBadRequest());

        // Try to create order anyway - should fail because cart is empty after failed item addition
        String createOrderRequest = JsonUtils.toJson(TestDataFactory.createCreateOrderRequest(testCart.getId()));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("empty cart")));
    }

    private Product findProductByName(String name) {
        return sampleProducts.stream()
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found: " + name));
    }
}
