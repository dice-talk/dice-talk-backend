package com.example.dice_talk.item.service;

import com.example.dice_talk.aws.S3Uploader;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.item.repository.ItemRepository;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final S3Uploader s3Uploader;

    public ItemService(ItemRepository itemRepository, S3Uploader s3Uploader) {
        this.itemRepository = itemRepository;
        this.s3Uploader = s3Uploader;
    }

    public Item createItem(Item item, MultipartFile file) throws IOException {
        //관리자만 등록 가능
        AuthorizationUtils.verifyAdmin();
        // S3 업로드 후 Entity 에 Set
        if(file != null && !file.isEmpty()){
            String imageUrl = s3Uploader.upload(file, "item-image");
            item.setItemImage(imageUrl);
        }
        // 아이템 등록 후 반환
        return itemRepository.save(item);
    }

    public Item updateItem(Item item, MultipartFile file) throws IOException {
        //관리자만 수정 가능
        AuthorizationUtils.verifyAdmin();
        Item findItem = findVerifiedItem(item.getItemId());
        // 변경가능한 필드 확인 후 변경
        Optional.ofNullable(item.getItemName())
                .ifPresent(itemName -> findItem.setItemName(itemName));
        Optional.ofNullable(item.getDescription())
                .ifPresent(quantity -> findItem.setDescription(quantity));
        Optional.of(item.getDicePrice())
                .ifPresent(price -> findItem.setDicePrice(price));
        if(file != null && !file.isEmpty()){
            String imageUrl = s3Uploader.upload(file, "item-image");
            s3Uploader.moveToDeletedFolder(findItem.getItemImage(), "deleted-item-image");
            findItem.setItemImage(imageUrl);
        }
        // 저장 후 반환
        return itemRepository.save(findItem);
    }

    public Item findItem(long itemId){
        // 상품 존재하는지 확인 후 반환
        return findVerifiedItem(itemId);
    }

    public Page<Item> findItems(int page, int size){
        // page 번호 검증
        if(page < 1){
            throw new IllegalArgumentException("페이지의 번호는 1 이상이어야 합니다.");
        }
        // page 객체에 담아서 반환
        return itemRepository.findAll(PageRequest.of(page-1, size, Sort.by("dicePrice").ascending()));
    }

    public void deleteItem(long itemId){
        //관리자만 등록 가능
        AuthorizationUtils.verifyAdmin();
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.ITEM_NOT_EXIST));
        s3Uploader.moveToDeletedFolder(item.getItemImage(), "deleted-item-image");
        itemRepository.delete(item);
    }

    public Item findVerifiedItem(long itemId){
        // itemId로 DB에서 조회 후 없으면 예외 발생
        return  itemRepository.findById(itemId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.ITEM_NOT_EXIST));
    }
}
