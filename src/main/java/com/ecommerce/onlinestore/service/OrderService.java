package com.ecommerce.onlinestore.service;

import com.ecommerce.onlinestore.model.dto.CreateOrderRequest;
import com.ecommerce.onlinestore.model.dto.OrderDTO;
import com.ecommerce.onlinestore.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderDTO createOrder(CreateOrderRequest request);

    OrderDTO getOrderById(Long orderId);

    OrderDTO getOrderByNumber(String orderNumber);

    Page<OrderDTO> getAllOrders(Pageable pageable);

    Page<OrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable);

    Page<OrderDTO> getOrdersByCustomerEmail(String email, Pageable pageable);

    OrderDTO updateOrderStatus(Long orderId, OrderStatus status);

    OrderDTO cancelOrder(Long orderId);

    void deleteOrder(Long orderId);
}
