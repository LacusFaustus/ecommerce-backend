package com.ecommerce.onlinestore.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Cart ID is required")
    private Long cartId;

    @Valid
    @NotNull(message = "Customer info is required")
    private CustomerInfoDTO customerInfo;

    @Valid
    @NotNull(message = "Shipping address is required")
    private AddressDTO shippingAddress;
}
