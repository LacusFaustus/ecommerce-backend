package com.ecommerce.onlinestore.service.impl;

import com.ecommerce.onlinestore.mapper.CartMapper;
import com.ecommerce.onlinestore.model.dto.AddToCartRequest;
import com.ecommerce.onlinestore.model.dto.CartDTO;
import com.ecommerce.onlinestore.model.entity.Cart;
import com.ecommerce.onlinestore.model.entity.Product;
import com.ecommerce.onlinestore.model.exception.CartNotFoundException;
import com.ecommerce.onlinestore.model.exception.InsufficientStockException;
import com.ecommerce.onlinestore.model.exception.ProductNotFoundException;
import com.ecommerce.onlinestore.repository.CartRepository;
import com.ecommerce.onlinestore.repository.ProductRepository;
import com.ecommerce.onlinestore.util.TestConstants;
import com.ecommerce.onlinestore.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
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
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private Product testProduct;
    private CartDTO testCartDTO;

    @BeforeEach
    void setUp() {
        testCart = TestDataFactory.createCart(1L, TestConstants.TEST_SESSION_ID);
        testProduct = TestDataFactory.createProduct(1L, "Laptop", "Electronics", BigDecimal.valueOf(999.99), 10);
        testCartDTO = new CartDTO();
    }

    @Test
    void shouldCreateCart() {
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartMapper.toDTO(testCart)).thenReturn(testCartDTO);

        CartDTO result = cartService.createCart(TestConstants.TEST_SESSION_ID);

        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldGetCartById() {
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(cartMapper.toDTO(testCart)).thenReturn(testCartDTO);

        CartDTO result = cartService.getCartById(1L);

        assertThat(result).isNotNull();
        verify(cartRepository).findByIdWithItems(1L);
    }

    @Test
    void shouldThrowExceptionWhenCartNotFound() {
        when(cartRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCartById(999L))
                .isInstanceOf(CartNotFoundException.class)
                .hasMessageContaining("Cart not found with id: 999");
    }

    @Test
    void shouldAddItemToCart() {
        AddToCartRequest request = new AddToCartRequest(1L, 2);

        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
        when(cartMapper.toDTO(testCart)).thenReturn(testCartDTO);

        CartDTO result = cartService.addItemToCart(1L, request);

        assertThat(result).isNotNull();
        verify(cartRepository).findByIdWithItems(1L);
        verify(productRepository).findById(1L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        AddToCartRequest request = new AddToCartRequest(999L, 2);

        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart(1L, request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        AddToCartRequest request = new AddToCartRequest(1L, 15); // More than available stock

        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() -> cartService.addItemToCart(1L, request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void shouldClearCart() {
        when(cartRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.clearCart(1L);

        verify(cartRepository).findByIdWithItems(1L);
        verify(cartRepository).save(any(Cart.class));
    }
}
