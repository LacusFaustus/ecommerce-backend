package com.ecommerce.onlinestore.mapper;

import com.ecommerce.onlinestore.model.dto.CartDTO;
import com.ecommerce.onlinestore.model.dto.CartItemDTO;
import com.ecommerce.onlinestore.model.entity.Cart;
import com.ecommerce.onlinestore.model.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface CartMapper {

    CartDTO toDTO(Cart cart);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.price", target = "productPrice")
    @Mapping(source = "unitPrice", target = "unitPrice")
    @Mapping(expression = "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))",
            target = "totalPrice")
    CartItemDTO itemToDTO(CartItem item);
}
