package com.example.dice_talk.item.controller;

import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.item.service.ItemService;
import com.example.dice_talk.item.dto.ItemDto;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.item.mapper.ItemMapper;
import com.example.dice_talk.utils.JsonParserUtil;
import com.example.dice_talk.utils.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
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
    private final JsonParserUtil jsonParserUtil;

    public ItemController(ItemService itemService, ItemMapper mapper, JsonParserUtil jsonParserUtil) {
        this.itemService = itemService;
        this.mapper = mapper;
        this.jsonParserUtil = jsonParserUtil;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity postItem(@Valid @RequestParam String itemPostDtoString,
                                   @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        ItemDto.Post postDto = jsonParserUtil.parse(itemPostDtoString, ItemDto.Post.class);
        Item createdItem = itemService.createItem(mapper.itemPostToItem(postDto), imageFile);
        URI location = UriCreator.createUri(ITEM_DEFAULT_URL, createdItem.getItemId());
        return ResponseEntity.created(location).build();
    }

    @PatchMapping(value = "/{item-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity patchItem(
            @PathVariable("item-id") @Positive long itemId,
            @Valid @RequestParam String itemPatchDtoString,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        ItemDto.Patch patchDto = jsonParserUtil.parse(itemPatchDtoString, ItemDto.Patch.class);
        patchDto.setItemId(itemId);
        Item item = itemService.updateItem(mapper.itemPatchToItem(patchDto), imageFile);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.itemToItemResponse(item)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getItems(@Positive @RequestParam int page, @Positive @RequestParam int size) {
        Page<Item> itemPage = itemService.findItems(page, size);
        List<Item> items = itemPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.itemsToItemResponses(items), itemPage), HttpStatus.OK);
    }

    @GetMapping("/{item-id}")
    public ResponseEntity getItem(@PathVariable("item-id") @Positive long itemId) {
        Item item = itemService.findItem(itemId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.itemToItemResponse(item)), HttpStatus.OK);
    }

    @DeleteMapping("/{item-id}")
    public ResponseEntity deleteItem(@PathVariable("item-id") long itemId) {
        itemService.deleteItem(itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
