package com.example.dice_talk.product.mapper;

import com.example.dice_talk.item.dto.ItemDto;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.product.dto.ProductDto;
import com.example.dice_talk.product.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product productPostToProduct(ProductDto.Post dto);

    Product productPatchToProduct(ProductDto.Patch dto);

    ProductDto.Response productToProductResponse(Product product);

    List<ProductDto.Response> productsToProductResponses(List<Product> products);
}
