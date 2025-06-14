package com.example.dice_talk.product.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;

import com.example.dice_talk.product.dto.ProductDto;
import com.example.dice_talk.product.entity.Product;
import com.example.dice_talk.product.mapper.ProductMapper;
import com.example.dice_talk.product.service.ProductService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.JsonParserUtil;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Product", description = "상품 API")
@SecurityRequirement(name = "JWT")
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

    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> postProduct(@Parameter(description = "상품 생성 DTO 문자열(JSON)")
                                            @RequestPart("productPostDto") String productPostDtoString,
                                            @Parameter(description = "상품 이미지 파일", required = false)
                                            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        AuthorizationUtils.isAdmin();
        ProductDto.Post postDto = jsonParserUtil.parse(productPostDtoString, ProductDto.Post.class);
        Product createdProduct = productService.createProduct(mapper.productPostToProduct(postDto), imageFile);
        URI location = UriCreator.createUri(PRODUCT_DEFAULT_URL, createdProduct.getProductId());
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ProductDto.Response.class))
            ),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @PatchMapping(value = "/{product-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponseDto<ProductDto.Response>> patchProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable("product-id") @Positive long productId,
            @Parameter(description = "상품 수정 DTO 문자열(JSON)")
            @RequestPart("productPatchDto") String productPatchDtoString,
            @Parameter(description = "상품 이미지 파일", required = false)
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        AuthorizationUtils.isAdmin();
        ProductDto.Patch patchDto = jsonParserUtil.parse(productPatchDtoString, ProductDto.Patch.class);
        patchDto.setProductId(productId);
        Product product = productService.updateProduct(mapper.productPatchToProduct(patchDto), imageFile);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.productToProductResponse(product)), HttpStatus.OK);
    }

    @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductDto.Response.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 파라미터",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            )
    })
    @GetMapping
    public ResponseEntity<MultiResponseDto<ProductDto.Response>> getProducts(@Parameter(description = "페이지 번호(1 이상)", example = "1")
                                                                             @Positive @RequestParam int page,
                                                                             @Parameter(description = "페이지 크기(1 이상)", example = "10")
                                                                             @Positive @RequestParam int size) {
        Page<Product> productPage = productService.findProducts(page, size);
        List<Product> products = productPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.productsToProductResponses(products), productPage), HttpStatus.OK);
    }

    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductDto.Response.class))
            ),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @GetMapping("/{product-id}")
    public ResponseEntity<SingleResponseDto<ProductDto.Response>> getProduct(@Parameter(description = "상품 ID", example = "1")
                                                                             @PathVariable("product-id") @Positive long productId) {
        Product product = productService.findProduct(productId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.productToProductResponse(product)), HttpStatus.OK);
    }

    @Operation(summary = "상품 삭제", description = "특정 상품을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @DeleteMapping("/{product-id}")
    public ResponseEntity<Void> deleteItem(@Parameter(description = "상품 ID", example = "1")
                                           @PathVariable("product-id") long productId) {
        productService.deleteProduct(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
