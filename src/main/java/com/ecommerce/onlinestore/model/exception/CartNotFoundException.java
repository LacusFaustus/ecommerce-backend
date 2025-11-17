package com.ecommerce.onlinestore.model.exception;

public class CartNotFoundException extends BusinessException {

    public CartNotFoundException(Long cartId) {
        super("Cart not found with id: " + cartId);
    }

    public CartNotFoundException(String message) {
        super(message);
    }
}
