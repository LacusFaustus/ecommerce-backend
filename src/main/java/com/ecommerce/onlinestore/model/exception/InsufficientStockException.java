package com.ecommerce.onlinestore.model.exception;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String productName, Integer requested, Integer available) {
        super(String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d",
                productName, requested, available));
    }

    // Добавляем конструктор для строкового сообщения
    public InsufficientStockException(String message) {
        super(message);
    }
}
