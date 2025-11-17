package com.ecommerce.onlinestore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoDTO {
    private String customerName;
    private String customerEmail;
    private String customerPhone;
}
