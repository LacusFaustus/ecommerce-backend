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
class OrderIntegrationTest {

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
        // Clear all data
        cartRepository.deleteAll();
        productRepository.deleteAll();

        // Create and save sample products
        sampleProducts = TestDataFactory.createSampleProducts();
        productRepository.saveAll(sampleProducts);

        // Create test cart
        testCart = TestDataFactory.createCart(null, TestConstants.TEST_SESSION_ID);
        cartRepository.save(testCart);
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        // Given - Add items to cart
        Product laptop = findProductByName("Laptop");
        Product smartphone = findProductByName("Smartphone");

        String addLaptopRequest = JsonUtils.toJson(TestDataFactory.createAddToCartRequest(laptop.getId(), 1));
        String addPhoneRequest = JsonUtils.toJson(TestDataFactory.createAddToCartRequest(smartphone.getId(), 1));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCart.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(addLaptopRequest));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCart.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(addPhoneRequest));

        // When - Create order
        String createOrderRequest = JsonUtils.toJson(TestDataFactory.createCreateOrderRequest(testCart.getId()));

        // Then - Verify order creation
        var result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderNumber").isNotEmpty())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(1499.98))) // 999.99 + 499.99
                .andExpect(jsonPath("$.orderItems", hasSize(2)))
                .andExpect(jsonPath("$.customerInfo.customerName", is(TestConstants.TEST_CUSTOMER_NAME)))
                .andExpect(jsonPath("$.customerInfo.customerEmail", is(TestConstants.TEST_CUSTOMER_EMAIL)))
                .andExpect(jsonPath("$.shippingAddress.street", is(TestConstants.TEST_STREET)))
                .andExpect(jsonPath("$.shippingAddress.city", is(TestConstants.TEST_CITY)))
                .andReturn();

        // Extract order number for later use
        String orderResponse = result.getResponse().getContentAsString();
        String orderNumber = JsonUtils.fromJson(orderResponse, com.fasterxml.jackson.databind.JsonNode.class)
                .path("orderNumber").asText();

        // Verify we can retrieve the order by number
        mockMvc.perform(get("/api/orders/number/{orderNumber}", orderNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber", is(orderNumber)));
    }

    @Test
    void shouldGetOrderByOrderNumber() throws Exception {
        // Given - Create an order first and get its number
        String orderResponse = addProductToCartAndCreateOrder();
        String orderNumber = JsonUtils.fromJson(orderResponse, com.fasterxml.jackson.databind.JsonNode.class)
                .path("orderNumber").asText();

        // When & Then - Get order by number
        mockMvc.perform(get("/api/orders/number/{orderNumber}", orderNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber", is(orderNumber)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.customerInfo.customerEmail", is(TestConstants.TEST_CUSTOMER_EMAIL)));
    }

    @Test
    void shouldGetOrderById() throws Exception {
        // Given - Create an order first and get its ID
        String orderResponse = addProductToCartAndCreateOrder();
        Long orderId = JsonUtils.fromJson(orderResponse, com.fasterxml.jackson.databind.JsonNode.class)
                .path("id").asLong();

        // When & Then - Get order by ID
        mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.intValue())))
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void shouldGetAllOrdersPaginated() throws Exception {
        // Given - Create an order
        addProductToCartAndCreateOrder();

        // When & Then - Get all orders
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].orderNumber").exists())
                .andExpect(jsonPath("$.content[0].status", is("PENDING")));
    }

    @Test
    void shouldGetOrdersByStatus() throws Exception {
        // Given - Create an order
        addProductToCartAndCreateOrder();

        // When & Then - Get orders by status
        mockMvc.perform(get("/api/orders/status/PENDING")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("PENDING")));
    }

    @Test
    void shouldGetOrdersByCustomerEmail() throws Exception {
        // Given - Create an order
        addProductToCartAndCreateOrder();

        // When & Then - Get orders by customer email
        mockMvc.perform(get("/api/orders/customer/{email}", TestConstants.TEST_CUSTOMER_EMAIL)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].customerInfo.customerEmail", is(TestConstants.TEST_CUSTOMER_EMAIL)));
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {
        // Given - Create an order first and get its ID
        String orderResponse = addProductToCartAndCreateOrder();
        Long orderId = JsonUtils.fromJson(orderResponse, com.fasterxml.jackson.databind.JsonNode.class)
                .path("id").asLong();

        // When & Then - Update order status
        mockMvc.perform(put("/api/orders/{orderId}/status", orderId)
                        .param("status", "CONFIRMED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.intValue())))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        // Given - Create an order first and get its ID
        String orderResponse = addProductToCartAndCreateOrder();
        Long orderId = JsonUtils.fromJson(orderResponse, com.fasterxml.jackson.databind.JsonNode.class)
                .path("id").asLong();

        // When & Then - Cancel order
        mockMvc.perform(put("/api/orders/{orderId}/cancel", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.intValue())))
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        // Given - Create an order first and get its ID
        String orderResponse = addProductToCartAndCreateOrder();
        Long orderId = JsonUtils.fromJson(orderResponse, com.fasterxml.jackson.databind.JsonNode.class)
                .path("id").asLong();

        // When & Then - Delete order
        mockMvc.perform(delete("/api/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify order is deleted
        mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingOrderFromEmptyCart() throws Exception {
        // Given - Empty cart
        String createOrderRequest = JsonUtils.toJson(TestDataFactory.createCreateOrderRequest(testCart.getId()));

        // When & Then - Try to create order from empty cart
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("empty cart")));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentOrder() throws Exception {
        // When & Then - Try to get non-existent order
        mockMvc.perform(get("/api/orders/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/orders/number/NON-EXISTENT-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentOrder() throws Exception {
        // When & Then - Try to update non-existent order
        mockMvc.perform(put("/api/orders/999/status")
                        .param("status", "CONFIRMED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/orders/999/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentOrder() throws Exception {
        // When & Then - Try to delete non-existent order
        mockMvc.perform(delete("/api/orders/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldClearCartAfterSuccessfulOrder() throws Exception {
        // Given - Add item to cart
        Product laptop = findProductByName("Laptop");
        String addItemRequest = JsonUtils.toJson(TestDataFactory.createAddToCartRequest(laptop.getId(), 1));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCart.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(addItemRequest));

        // Verify cart has items
        mockMvc.perform(get("/api/carts/{cartId}", testCart.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));

        // When - Create order
        String createOrderRequest = JsonUtils.toJson(TestDataFactory.createCreateOrderRequest(testCart.getId()));
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderRequest));

        // Then - Verify cart is cleared
        mockMvc.perform(get("/api/carts/{cartId}", testCart.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", empty()))
                .andExpect(jsonPath("$.totalItems", is(0)))
                .andExpect(jsonPath("$.totalPrice", is(0)));
    }

    @Test
    void shouldUpdateProductStockAfterSuccessfulOrder() throws Exception {
        // Given - Add item to cart
        Product laptop = findProductByName("Laptop");
        int initialStock = laptop.getStockQuantity();
        String addItemRequest = JsonUtils.toJson(TestDataFactory.createAddToCartRequest(laptop.getId(), 2));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCart.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(addItemRequest));

        // When - Create order
        String createOrderRequest = JsonUtils.toJson(TestDataFactory.createCreateOrderRequest(testCart.getId()));
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createOrderRequest));

        // Then - Verify product stock is updated
        mockMvc.perform(get("/api/products/{id}", laptop.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity", is(initialStock - 2)));
    }

    // Helper methods
    private String addProductToCartAndCreateOrder() throws Exception {
        // Add product to cart
        Product product = findProductByName("Laptop");
        String addItemRequest = JsonUtils.toJson(TestDataFactory.createAddToCartRequest(product.getId(), 1));

        mockMvc.perform(post("/api/carts/{cartId}/items", testCart.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(addItemRequest));

        // Create order and return response
        String createOrderRequest = JsonUtils.toJson(TestDataFactory.createCreateOrderRequest(testCart.getId()));

        return mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderRequest))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private Product findProductByName(String name) {
        return sampleProducts.stream()
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found: " + name));
    }
}
