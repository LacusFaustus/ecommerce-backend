package com.ecommerce.onlinestore.service;

import com.ecommerce.onlinestore.model.dto.CreateOrderRequest;
import com.ecommerce.onlinestore.model.dto.OrderDTO;
import com.ecommerce.onlinestore.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrderServiceTest {

    @MockBean
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        OrderDTO orderDTO = OrderDTO.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(999.99))
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(orderDTO);

        // Test implementation would verify the service behavior
    }
}
