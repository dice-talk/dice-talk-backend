package com.example.dice_talk.item.mapper;

import com.example.dice_talk.item.dto.ItemDto;
import com.example.dice_talk.item.entity.Item;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    Item itemPostToItem(ItemDto.Post dto);

    Item itemPatchToItem(ItemDto.Patch dto);

    ItemDto.Response itemToItemResponse(Item item);

    List<ItemDto.Response> itemsToItemResponses(List<Item> items);

}
