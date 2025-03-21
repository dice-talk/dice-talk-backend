package com.example.dice_talk.item.controller;

import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.item.service.ItemService;
import com.example.dice_talk.item.dto.ItemDto;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.item.mapper.ItemMapper;
import com.example.dice_talk.utils.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/items")
@Validated
@Slf4j
public class ItemController {
    private final static String ITEM_DEFAULT_URL = "/items";
    private final ItemService itemService;
    private final ItemMapper mapper;

    public ItemController(ItemService itemService, ItemMapper mapper) {
        this.itemService = itemService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postItem(@Valid @RequestBody ItemDto.Post postDto){
        Item item = mapper.itemPostToItem(postDto);
        Item createdItem = itemService.createItem(item);
        URI location = UriCreator.createUri(ITEM_DEFAULT_URL, createdItem.getItemId());
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{item-id}")
    public ResponseEntity patchItem(
            @PathVariable("item-id") @Positive long itemId,
            @Valid @RequestBody ItemDto.Patch patchDto
    ){
        patchDto.setItemId(itemId);
        Item item = itemService.updateItem(mapper.itemPatchToItem(patchDto));
        return new ResponseEntity(new SingleResponseDto<>(mapper.itemToItemResponse(item)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getItems(@Positive @RequestParam int page, @Positive @RequestParam int size){
        Page<Item> itemPage = itemService.findItems(page, size);
        List<Item> items = itemPage.getContent();
        return new ResponseEntity(new MultiResponseDto<>(mapper.itemsToItemResponses(items), itemPage), HttpStatus.OK);
    }

    @GetMapping("/{item-id}")
    public ResponseEntity getItem(@PathVariable("item-id") @Positive long itemId){
        Item item = itemService.findItem(itemId);
        return new ResponseEntity(new SingleResponseDto<>(mapper.itemToItemResponse(item)), HttpStatus.OK);
    }

    @DeleteMapping("/{item-id}")
    public ResponseEntity deleteItem(@PathVariable("item-id") long itemId){
        itemService.deleteItem(itemId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
