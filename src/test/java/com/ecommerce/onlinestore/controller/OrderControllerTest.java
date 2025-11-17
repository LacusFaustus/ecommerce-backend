package com.ecommerce.onlinestore.controller;

import com.ecommerce.onlinestore.model.dto.OrderDTO;
import com.ecommerce.onlinestore.model.dto.OrderItemDTO;
import com.ecommerce.onlinestore.model.enums.OrderStatus;
import com.ecommerce.onlinestore.service.OrderService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void shouldCreateOrder() throws Exception {
        // Given
        OrderItemDTO orderItem = OrderItemDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Laptop")
                .unitPrice(BigDecimal.valueOf(999.99))
                .quantity(1)
                .totalPrice(BigDecimal.valueOf(999.99))
                .build();

        OrderDTO order = OrderDTO.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(999.99))
                .orderItems(List.of(orderItem))
                .customerInfo(com.ecommerce.onlinestore.model.dto.CustomerInfoDTO.builder()
                        .customerName("John Doe")
                        .customerEmail("john@example.com")
                        .customerPhone("+1234567890")
                        .build())
                .shippingAddress(com.ecommerce.onlinestore.model.dto.AddressDTO.builder()
                        .street("123 Main St")
                        .city("New York")
                        .state("NY")
                        .postalCode("10001")
                        .country("USA")
                        .build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderService.createOrder(any())).thenReturn(order);

        String createOrderRequest = """
            {
                "cartId": 1,
                "customerInfo": {
                    "customerName": "John Doe",
                    "customerEmail": "john@example.com",
                    "customerPhone": "+1234567890"
                },
                "shippingAddress": {
                    "street": "123 Main St",
                    "city": "New York",
                    "state": "NY",
                    "postalCode": "10001",
                    "country": "USA"
                }
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-123"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(999.99));
    }

    @Test
    void shouldGetOrderById() throws Exception {
        // Given
        OrderDTO order = OrderDTO.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(999.99))
                .build();

        when(orderService.getOrderById(1L)).thenReturn(order);

        // When & Then
        mockMvc.perform(get("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-123"));
    }

    @Test
    void shouldGetAllOrders() throws Exception {
        // Given
        OrderDTO order1 = OrderDTO.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(999.99))
                .build();

        OrderDTO order2 = OrderDTO.builder()
                .id(2L)
                .orderNumber("ORD-124")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(499.99))
                .build();

        Page<OrderDTO> orderPage = new PageImpl<>(List.of(order1, order2), PageRequest.of(0, 10), 2);

        when(orderService.getAllOrders(any(PageRequest.class))).thenReturn(orderPage);

        // When & Then
        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-123"))
                .andExpect(jsonPath("$.content[1].orderNumber").value("ORD-124"));
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {
        // Given
        OrderDTO order = OrderDTO.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(999.99))
                .build();

        when(orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED)).thenReturn(order);

        // When & Then
        mockMvc.perform(put("/api/orders/1/status")
                        .param("status", "CONFIRMED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        // Given
        OrderDTO order = OrderDTO.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .status(OrderStatus.CANCELLED)
                .totalAmount(BigDecimal.valueOf(999.99))
                .build();

        when(orderService.cancelOrder(1L)).thenReturn(order);

        // When & Then
        mockMvc.perform(put("/api/orders/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldValidateCreateOrderRequest() throws Exception {
        String invalidRequest = """
            {
                "cartId": null,
                "customerInfo": null,
                "shippingAddress": null
            }
            """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}
