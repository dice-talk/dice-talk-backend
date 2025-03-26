package com.example.dice_talk.product.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;

import com.example.dice_talk.product.dto.ProductDto;
import com.example.dice_talk.product.entity.Product;
import com.example.dice_talk.product.mapper.ProductMapper;
import com.example.dice_talk.product.service.ProductService;
import com.example.dice_talk.utils.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/products")
@Validated
@Slf4j
public class ProductController {
    private final static String PRODUCT_DEFAULT_URL = "/products";
    private final ProductService productService;
    private final ProductMapper mapper;

    public ProductController(ProductService productService, ProductMapper mapper) {
        this.productService = productService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postProduct(@Valid @RequestBody ProductDto.Post postDto){

        Product product = mapper.productPostToProduct(postDto);
        Product createdProduct = productService.createProduct(product);
        URI location = UriCreator.createUri(PRODUCT_DEFAULT_URL, createdProduct.getProductId());
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{product-id}")
    public ResponseEntity patchProduct(
            @PathVariable("product-id") @Positive long productId,
            @Valid @RequestBody ProductDto.Patch patchDto
    ){
        patchDto.setProductId(productId);
        Product product = productService.updateProduct(mapper.productPatchToProduct(patchDto));
        return new ResponseEntity(new SingleResponseDto<>(mapper.productToProductResponse(product)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getProducts(@Positive @RequestParam int page, @Positive @RequestParam int size){
        Page<Product> productPage = productService.findProducts(page, size);
        List<Product> products = productPage.getContent();
        return new ResponseEntity(new MultiResponseDto<>(mapper.productsToProductResponses(products), productPage), HttpStatus.OK);
    }

    @GetMapping("/{product-id}")
    public ResponseEntity getProduct(@PathVariable("product-id") @Positive long productId){
        Product product = productService.findProduct(productId);
        return new ResponseEntity(new SingleResponseDto<>(mapper.productToProductResponse(product)), HttpStatus.OK);
    }

    @DeleteMapping("/{product-id}")
    public ResponseEntity deleteItem(@PathVariable("product-id") long productId){
        productService.deleteProduct(productId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
