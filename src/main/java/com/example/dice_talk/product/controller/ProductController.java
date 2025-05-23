package com.example.dice_talk.product.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;

import com.example.dice_talk.product.dto.ProductDto;
import com.example.dice_talk.product.entity.Product;
import com.example.dice_talk.product.mapper.ProductMapper;
import com.example.dice_talk.product.service.ProductService;
import com.example.dice_talk.utils.JsonParserUtil;
import com.example.dice_talk.utils.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.attribute.standard.Media;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
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
    private final JsonParserUtil jsonParserUtil;

    public ProductController(ProductService productService, ProductMapper mapper, JsonParserUtil jsonParserUtil) {
        this.productService = productService;
        this.mapper = mapper;
        this.jsonParserUtil = jsonParserUtil;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity postProduct(@RequestParam("productPostDto") String productPostDtoString,
                                      @RequestPart(value = "image", required = false)MultipartFile imageFile) throws IOException {
        ProductDto.Post postDto = jsonParserUtil.parse(productPostDtoString, ProductDto.Post.class);
        Product createdProduct = productService.createProduct(mapper.productPostToProduct(postDto), imageFile);
        URI location = UriCreator.createUri(PRODUCT_DEFAULT_URL, createdProduct.getProductId());
        return ResponseEntity.created(location).build();
    }

    @PatchMapping(value = "/{product-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity patchProduct(
            @PathVariable("product-id") @Positive long productId,
            @RequestParam("productPatchDto") String productPatchDtoString,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        ProductDto.Patch patchDto = jsonParserUtil.parse(productPatchDtoString, ProductDto.Patch.class);
        patchDto.setProductId(productId);
        Product product = productService.updateProduct(mapper.productPatchToProduct(patchDto), imageFile);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.productToProductResponse(product)), HttpStatus.OK);
    }

    //
    @GetMapping
    public ResponseEntity getProducts(@Positive @RequestParam int page, @Positive @RequestParam int size){
        Page<Product> productPage = productService.findProducts(page, size);
        List<Product> products = productPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.productsToProductResponses(products), productPage), HttpStatus.OK);
    }

    @GetMapping("/{product-id}")
    public ResponseEntity getProduct(@PathVariable("product-id") @Positive long productId){
        Product product = productService.findProduct(productId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.productToProductResponse(product)), HttpStatus.OK);
    }

    @DeleteMapping("/{product-id}")
    public ResponseEntity deleteItem(@PathVariable("product-id") long productId){
        productService.deleteProduct(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
