package com.example.dice_talk.item.service;

import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.item.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item createItem(Item item){
        // 아이템 등록 후 반환
        return itemRepository.save(item);
    }

    public Item updateItem(Item item){
        Item findItem = findVerifiedItem(item.getItemId());
        // 변경가능한 필드 확인 후 변경
        Optional.ofNullable(item.getItemName())
                .ifPresent(itemName -> findItem.setItemName(itemName));
        Optional.ofNullable(item.getQuantity())
                .ifPresent(quantity -> findItem.setQuantity(quantity));
        Optional.ofNullable(item.getPrice())
                .ifPresent(price -> findItem.setPrice(price));
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
        return itemRepository.findAll(PageRequest.of(page-1, size, Sort.by("price").ascending()));
    }

    public void deleteItem(long itemId){
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.ITEM_NOT_EXIST));
        itemRepository.delete(item);
    }

    public Item findVerifiedItem(long itemId){
        // itemId로 DB에서 조회 후 없으면 예외 발생
        return  itemRepository.findById(itemId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.ITEM_NOT_EXIST));
    }
}
