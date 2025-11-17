package com.ecommerce.onlinestore.service.impl;

import com.ecommerce.onlinestore.mapper.OrderMapper;
import com.ecommerce.onlinestore.model.dto.CreateOrderRequest;
import com.ecommerce.onlinestore.model.dto.OrderDTO;
import com.ecommerce.onlinestore.model.entity.Cart;
import com.ecommerce.onlinestore.model.entity.Product;
import com.ecommerce.onlinestore.model.enums.OrderStatus;
import com.ecommerce.onlinestore.model.exception.CartNotFoundException;
import com.ecommerce.onlinestore.repository.CartRepository;
import com.ecommerce.onlinestore.repository.OrderRepository;
import com.ecommerce.onlinestore.repository.ProductRepository;
import com.ecommerce.onlinestore.util.TestDataFactory;
import com.ecommerce.onlinestore.util.TestConstants;
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
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void shouldCreateOrder() {
        // Given
        Cart cart = TestDataFactory.createCart(1L, TestConstants.TEST_SESSION_ID);
        Product product = TestDataFactory.createProduct(1L, "Laptop", "Electronics", BigDecimal.valueOf(999.99), 10);

        // Add item to cart
        var cartItem = TestDataFactory.createCartItem(1L, cart, product, 2);
        cart.addItem(cartItem);

        CreateOrderRequest request = TestDataFactory.createCreateOrderRequest(1L);

        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toDTO(any())).thenReturn(OrderDTO.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(1999.98))
                .build());

        // When
        OrderDTO result = orderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("ORD-123");
        verify(cartRepository).findByIdWithItems(1L);
        verify(orderRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCartNotFound() {
        // Given
        CreateOrderRequest request = TestDataFactory.createCreateOrderRequest(999L);

        when(cartRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(CartNotFoundException.class)
                .hasMessageContaining("Cart not found with id: 999");
    }

    @Test
    void shouldThrowExceptionWhenCartIsEmpty() {
        // Given
        Cart emptyCart = TestDataFactory.createCart(1L, TestConstants.TEST_SESSION_ID);
        CreateOrderRequest request = TestDataFactory.createCreateOrderRequest(1L);

        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(emptyCart));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("empty cart");
    }
}
