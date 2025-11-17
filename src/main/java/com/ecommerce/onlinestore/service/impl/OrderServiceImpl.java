package com.ecommerce.onlinestore.service.impl;

import com.ecommerce.onlinestore.mapper.OrderMapper;
import com.ecommerce.onlinestore.model.dto.CreateOrderRequest;
import com.ecommerce.onlinestore.model.dto.OrderDTO;
import com.ecommerce.onlinestore.model.entity.*;
import com.ecommerce.onlinestore.model.enums.OrderStatus;
import com.ecommerce.onlinestore.model.exception.BusinessException;
import com.ecommerce.onlinestore.model.exception.CartNotFoundException;
import com.ecommerce.onlinestore.model.exception.InsufficientStockException;
import com.ecommerce.onlinestore.model.exception.OrderNotFoundException;
import com.ecommerce.onlinestore.repository.CartRepository;
import com.ecommerce.onlinestore.repository.OrderRepository;
import com.ecommerce.onlinestore.repository.ProductRepository;
import com.ecommerce.onlinestore.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.debug("Creating order from cart: {}", request.getCartId());

        // Fetch cart with items
        Cart cart = cartRepository.findByIdWithItems(request.getCartId())
                .orElseThrow(() -> new CartNotFoundException(request.getCartId()));

        // Validate cart is not empty
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot create order from empty cart");
        }

        // Validate stock and reserve products
        validateAndReserveStock(cart);

        // Create order
        Order order = buildOrderFromCart(cart, request);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Update product stock
        updateProductStock(cart);

        // Clear cart after successful order creation
        cart.clear();
        cartRepository.save(cart);

        log.info("Created order {} from cart {}", savedOrder.getOrderNumber(), request.getCartId());
        return orderMapper.toDTO(savedOrder);
    }

    private void validateAndReserveStock(Cart cart) {
        List<String> errors = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                errors.add(String.format(
                        "Insufficient stock for product '%s'. Requested: %d, Available: %d",
                        product.getName(), item.getQuantity(), product.getStockQuantity()
                ));
            }
        }

        if (!errors.isEmpty()) {
            throw new InsufficientStockException(String.join("; ", errors));
        }
    }

    // ... остальные методы остаются без изменений ...

    private void updateProductStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            int newStock = product.getStockQuantity() - item.getQuantity();
            product.setStockQuantity(newStock);
            productRepository.save(product);
        }
    }

    private void restoreProductStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new com.ecommerce.onlinestore.model.exception.ProductNotFoundException(item.getProductId()));
            int restoredStock = product.getStockQuantity() + item.getQuantity();
            product.setStockQuantity(restoredStock);
            productRepository.save(product);
        }
    }

    private Order buildOrderFromCart(Cart cart, CreateOrderRequest request) {
        Order order = Order.builder()
                .customerInfo(orderMapper.toCustomerInfoEntity(request.getCustomerInfo()))
                .shippingAddress(orderMapper.toAddressEntity(request.getShippingAddress()))
                .status(OrderStatus.PENDING)
                .build();

        // Create order items from cart items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(cartItem.getProduct().getId())
                    .productName(cartItem.getProduct().getName())
                    .unitPrice(cartItem.getProduct().getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
            // Вызываем метод расчета общей цены
            orderItem.calculateTotalPrice();
            order.addOrderItem(orderItem);
        }

        order.calculateTotalAmount();
        return order;
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Simple validation - in real application, you might want more complex state machine
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot change status from " + currentStatus);
        }
    }

    // ... остальные методы без изменений ...
    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        log.debug("Fetching order by id: {}", orderId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return orderMapper.toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderByNumber(String orderNumber) {
        log.debug("Fetching order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));

        return orderMapper.toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders with pageable: {}", pageable);
        return orderRepository.findAll(pageable)
                .map(orderMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.debug("Fetching orders by status: {}", status);
        return orderRepository.findByStatus(status, pageable)
                .map(orderMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByCustomerEmail(String email, Pageable pageable) {
        log.debug("Fetching orders by customer email: {}", email);
        return orderRepository.findByCustomerInfo_CustomerEmail(email, pageable)
                .map(orderMapper::toDTO);
    }

    @Override
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        log.debug("Updating order {} status to: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        log.info("Updated order {} status to {}", orderId, status);
        return orderMapper.toDTO(savedOrder);
    }

    @Override
    public OrderDTO cancelOrder(Long orderId) {
        log.debug("Cancelling order: {}", orderId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Only pending or confirmed orders can be cancelled
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        // Restore product stock
        restoreProductStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        log.info("Cancelled order {}", orderId);
        return orderMapper.toDTO(savedOrder);
    }

    @Override
    public void deleteOrder(Long orderId) {
        log.debug("Deleting order: {}", orderId);

        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException(orderId);
        }

        orderRepository.deleteById(orderId);
        log.info("Deleted order {}", orderId);
    }
}
