package com.ecommerce.onlinestore.util;

import com.ecommerce.onlinestore.model.dto.*;
import com.ecommerce.onlinestore.model.entity.*;
import com.ecommerce.onlinestore.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public class TestDataFactory {

    public static Product createProduct(Long id, String name, String category, BigDecimal price, Integer stock) {
        return Product.builder()
                .id(id)
                .name(name)
                .description("Test description for " + name)
                .price(price)
                .stockQuantity(stock)
                .category(category)
                .sku("SKU-" + name.toUpperCase().replace(" ", "-"))
                .build();
    }

    public static ProductDTO createProductDTO(Long id, String name, String category, BigDecimal price, Integer stock) {
        return ProductDTO.builder()
                .id(id)
                .name(name)
                .description("Test description for " + name)
                .price(price)
                .stockQuantity(stock)
                .category(category)
                .sku("SKU-" + name.toUpperCase().replace(" ", "-"))
                .build();
    }

    public static List<Product> createSampleProducts() {
        return List.of(
                createProduct(1L, "Laptop", "Electronics", BigDecimal.valueOf(999.99), 10),
                createProduct(2L, "Smartphone", "Electronics", BigDecimal.valueOf(499.99), 15),
                createProduct(3L, "Programming Book", "Books", BigDecimal.valueOf(29.99), 50),
                createProduct(4L, "Wireless Headphones", "Electronics", BigDecimal.valueOf(149.99), 20)
        );
    }

    public static Cart createCart(Long id, String sessionId) {
        return Cart.builder()
                .id(id)
                .sessionId(sessionId)
                .totalPrice(BigDecimal.ZERO)
                .totalItems(0)
                .build();
    }

    public static CartItem createCartItem(Long id, Cart cart, Product product, Integer quantity) {
        return CartItem.builder()
                .id(id)
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .build();
    }

    public static Order createOrder(Long id, String orderNumber, OrderStatus status) {
        Order order = Order.builder()
                .id(id)
                .orderNumber(orderNumber)
                .status(status)
                .totalAmount(BigDecimal.valueOf(199.98))
                .customerInfo(createCustomerInfo())
                .shippingAddress(createAddress())
                .build();

        // Add order items to make order valid
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .productId(1L)
                .productName("Test Product")
                .unitPrice(BigDecimal.valueOf(99.99))
                .quantity(2)
                .build();
        orderItem.calculateTotalPrice();
        order.addOrderItem(orderItem);

        return order;
    }

    public static CustomerInfo createCustomerInfo() {
        return CustomerInfo.builder()
                .customerName(TestConstants.TEST_CUSTOMER_NAME)
                .customerEmail(TestConstants.TEST_CUSTOMER_EMAIL)
                .customerPhone(TestConstants.TEST_CUSTOMER_PHONE)
                .build();
    }

    public static Address createAddress() {
        return Address.builder()
                .street(TestConstants.TEST_STREET)
                .city(TestConstants.TEST_CITY)
                .state(TestConstants.TEST_STATE)
                .postalCode(TestConstants.TEST_POSTAL_CODE)
                .country(TestConstants.TEST_COUNTRY)
                .build();
    }

    public static CustomerInfoDTO createCustomerInfoDTO() {
        return CustomerInfoDTO.builder()
                .customerName(TestConstants.TEST_CUSTOMER_NAME)
                .customerEmail(TestConstants.TEST_CUSTOMER_EMAIL)
                .customerPhone(TestConstants.TEST_CUSTOMER_PHONE)
                .build();
    }

    public static AddressDTO createAddressDTO() {
        return AddressDTO.builder()
                .street(TestConstants.TEST_STREET)
                .city(TestConstants.TEST_CITY)
                .state(TestConstants.TEST_STATE)
                .postalCode(TestConstants.TEST_POSTAL_CODE)
                .country(TestConstants.TEST_COUNTRY)
                .build();
    }

    public static CreateOrderRequest createCreateOrderRequest(Long cartId) {
        return new CreateOrderRequest(
                cartId,
                createCustomerInfoDTO(),
                createAddressDTO()
        );
    }

    public static AddToCartRequest createAddToCartRequest(Long productId, Integer quantity) {
        return new AddToCartRequest(productId, quantity);
    }
}
