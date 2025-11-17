package com.ecommerce.onlinestore.mapper;

import com.ecommerce.onlinestore.model.dto.*;
import com.ecommerce.onlinestore.model.entity.Order;
import com.ecommerce.onlinestore.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDTO toDTO(Order order);

    CustomerInfoDTO toCustomerInfoDTO(com.ecommerce.onlinestore.model.entity.CustomerInfo customerInfo);

    AddressDTO toAddressDTO(com.ecommerce.onlinestore.model.entity.Address address);

    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "unitPrice", target = "unitPrice")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "totalPrice", target = "totalPrice")
    OrderItemDTO itemToDTO(OrderItem item);

    com.ecommerce.onlinestore.model.entity.CustomerInfo toCustomerInfoEntity(CustomerInfoDTO customerInfoDTO);

    com.ecommerce.onlinestore.model.entity.Address toAddressEntity(AddressDTO addressDTO);
}
