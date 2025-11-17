package com.ecommerce.onlinestore.model.exception;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
