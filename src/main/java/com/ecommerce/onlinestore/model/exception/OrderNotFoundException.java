package com.ecommerce.onlinestore.model.exception;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
    }

    public OrderNotFoundException(String orderNumber) {
        super("Order not found with number: " + orderNumber);
    }
}
