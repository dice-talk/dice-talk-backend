package com.example.dice_talk.product.service;

import com.example.dice_talk.auth.utils.AuthorityUtils;
import com.example.dice_talk.aws.S3Uploader;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.item.repository.ItemRepository;
import com.example.dice_talk.product.entity.Product;
import com.example.dice_talk.product.repository.ProductRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.JsonParserUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final S3Uploader s3Uploader;

    public ProductService(ProductRepository productRepository, JsonParserUtil jsonParserUtil, S3Uploader s3Uploader) {
        this.productRepository = productRepository;
        this.s3Uploader = s3Uploader;
    }


    @Transactional
    public Product createProduct(Product product, MultipartFile file) throws IOException {
        AuthorizationUtils.verifyAdmin();
        // S3 업로드 후 엔티티에 set
        if (file != null && !file.isEmpty()) {
            String imageUrl = s3Uploader.upload(file, "product-image");
            product.setProductImage(imageUrl);
        } else {
            product.setProductImage("No-Image");
        }
        // 아이템 등록 후 반환
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Product product, MultipartFile file) throws IOException {
        //관리자만 수정 가능
        AuthorizationUtils.verifyAdmin();
        //저장된 product 가져오기
        Product findProduct = findVerifiedProduct(product.getProductId());
        product.setProductImage(findProduct.getProductImage());
        // 변경가능한 필드 확인 후 변경
        Optional.ofNullable(product.getProductName())
                .ifPresent(name -> findProduct.setProductName(name));
        Optional.ofNullable(product.getPrice())
                .ifPresent(price -> findProduct.setPrice(price));
        Optional.ofNullable(product.getQuantity())
                .ifPresent(quantity -> findProduct.setQuantity(quantity));
        if(file != null && !file.isEmpty()){
            s3Uploader.moveToDeletedFolder(product.getProductImage(), "deleted-product-image");
            String newFileUrl = s3Uploader.upload(file, "product-image");
            findProduct.setProductImage(newFileUrl);
        }
        // 저장 후 반환
        return productRepository.save(findProduct);
    }

    public Product findProduct(long productId) {
        // 상품 존재하는지 확인 후 반환
        return findVerifiedProduct(productId);
    }

    public Page<Product> findProducts(int page, int size) {
        // page 번호 검증
        if (page < 1) {
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        // page 객체에 담아서 반환
        return productRepository.findAll(PageRequest.of(page - 1, size, Sort.by("price").ascending()));
    }

    @Transactional
    public void deleteProduct(long productId) {
        //관리지만 삭제 가능
        AuthorizationUtils.verifyAdmin();
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.PRODUCT_NOT_FOUND));
        s3Uploader.moveToDeletedFolder(product.getProductImage(), "deleted-product-image");
        productRepository.delete(product);
    }

    public Product findVerifiedProduct(long productId) {
        // itemId로 DB에서 조회 후 없으면 예외 발생
        return productRepository.findById(productId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.PRODUCT_NOT_FOUND));
    }
}

