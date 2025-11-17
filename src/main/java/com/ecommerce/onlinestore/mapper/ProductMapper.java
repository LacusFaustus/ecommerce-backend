package com.ecommerce.onlinestore.mapper;

import com.ecommerce.onlinestore.model.dto.ProductDTO;
import com.ecommerce.onlinestore.model.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductDTO toDTO(Product product);

    Product toEntity(ProductDTO productDTO);
}
