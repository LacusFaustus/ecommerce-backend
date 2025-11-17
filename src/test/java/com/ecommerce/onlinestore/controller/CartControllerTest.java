package com.ecommerce.onlinestore.controller;

import com.ecommerce.onlinestore.model.dto.CartDTO;
import com.ecommerce.onlinestore.model.dto.CartItemDTO;
import com.ecommerce.onlinestore.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    void shouldCreateCart() throws Exception {
        // Given
        CartDTO cart = CartDTO.builder()
                .id(1L)
                .sessionId("session-123")
                .totalPrice(BigDecimal.ZERO)
                .totalItems(0)
                .items(Collections.emptyList())
                .build();

        when(cartService.createCart(anyString())).thenReturn(cart);

        // When & Then
        mockMvc.perform(post("/api/carts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sessionId").value("session-123"));
    }

    @Test
    void shouldGetCartById() throws Exception {
        // Given
        CartDTO cart = CartDTO.builder()
                .id(1L)
                .sessionId("session-123")
                .totalPrice(BigDecimal.ZERO)
                .totalItems(0)
                .items(Collections.emptyList())
                .build();

        when(cartService.getCartById(1L)).thenReturn(cart);

        // When & Then
        mockMvc.perform(get("/api/carts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldAddItemToCart() throws Exception {
        // Given
        CartItemDTO cartItem = CartItemDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Laptop")
                .productPrice(BigDecimal.valueOf(999.99))
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(999.99))
                .totalPrice(BigDecimal.valueOf(1999.98))
                .build();

        CartDTO cart = CartDTO.builder()
                .id(1L)
                .sessionId("session-123")
                .totalPrice(BigDecimal.valueOf(1999.98))
                .totalItems(2)
                .items(List.of(cartItem))
                .build();

        when(cartService.addItemToCart(eq(1L), any())).thenReturn(cart);

        String addItemRequest = """
            {
                "productId": 1,
                "quantity": 2
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(1999.98));
    }

    @Test
    void shouldUpdateCartItem() throws Exception {
        // Given
        CartItemDTO cartItem = CartItemDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Laptop")
                .productPrice(BigDecimal.valueOf(999.99))
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(999.99))
                .totalPrice(BigDecimal.valueOf(4999.95))
                .build();

        CartDTO cart = CartDTO.builder()
                .id(1L)
                .sessionId("session-123")
                .totalPrice(BigDecimal.valueOf(4999.95))
                .totalItems(5)
                .items(List.of(cartItem))
                .build();

        when(cartService.updateCartItem(eq(1L), eq(1L), any())).thenReturn(cart);

        String updateRequest = """
            {
                "quantity": 5
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/carts/1/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5))
                .andExpect(jsonPath("$.totalPrice").value(4999.95));
    }

    @Test
    void shouldRemoveItemFromCart() throws Exception {
        // Given
        CartDTO cart = CartDTO.builder()
                .id(1L)
                .sessionId("session-123")
                .totalPrice(BigDecimal.ZERO)
                .totalItems(0)
                .items(Collections.emptyList())
                .build();

        when(cartService.removeItemFromCart(1L, 1L)).thenReturn(cart);

        // When & Then
        mockMvc.perform(delete("/api/carts/1/items/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    void shouldClearCart() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/carts/1/clear")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldValidateAddToCartRequest() throws Exception {
        String invalidRequest = """
            {
                "productId": null,
                "quantity": 0
            }
            """;

        mockMvc.perform(post("/api/carts/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}
