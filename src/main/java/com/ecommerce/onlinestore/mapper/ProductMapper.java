package com.ecommerce.onlinestore.mapper;

import com.ecommerce.onlinestore.model.dto.ProductDTO;
import com.ecommerce.onlinestore.model.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDTO toDTO(Product product);

    Product toEntity(ProductDTO productDTO);
}
