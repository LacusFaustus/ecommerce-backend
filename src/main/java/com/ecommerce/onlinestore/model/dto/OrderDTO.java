package com.ecommerce.onlinestore.model.dto;

import com.ecommerce.onlinestore.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal totalAmount;

    @Builder.Default
    private List<OrderItemDTO> orderItems = new ArrayList<>();

    private CustomerInfoDTO customerInfo;
    private AddressDTO shippingAddress;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
