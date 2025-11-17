package com.ecommerce.onlinestore.controller;

import com.ecommerce.onlinestore.model.dto.CreateOrderRequest;
import com.ecommerce.onlinestore.model.dto.OrderDTO;
import com.ecommerce.onlinestore.model.enums.OrderStatus;
import com.ecommerce.onlinestore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order from cart")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve paginated list of all orders")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve order by ID")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Retrieve order by order number")
    public ResponseEntity<OrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        OrderDTO order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Retrieve orders by status")
    public ResponseEntity<Page<OrderDTO>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{email}")
    @Operation(summary = "Get orders by customer", description = "Retrieve orders by customer email")
    public ResponseEntity<Page<OrderDTO>> getOrdersByCustomer(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = orderService.getOrdersByCustomerEmail(email, pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update order status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        OrderDTO order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel order")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long orderId) {
        OrderDTO order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Delete order", description = "Delete order")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
