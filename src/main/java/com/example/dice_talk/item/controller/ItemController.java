package com.example.dice_talk.item.controller;

import com.example.dice_talk.chatroom.dto.ChatRoomDto;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.item.service.ItemService;
import com.example.dice_talk.item.dto.ItemDto;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.item.mapper.ItemMapper;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.JsonParserUtil;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Item API", description = "아이템 관련 API")
@SecurityRequirement(name = "JWT")
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

    @Operation(summary = "아이템 등록", description = "관리자가 새로운 아이템을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "아이템 등록 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "등록 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> postItem(@Parameter(name = "itemPostDtoString", description = "등록할 아이템 정보가 포함된 JSON 문자열",
                                                     example = "{\"itemName\": \"채팅방 나가기\", \"description\": \"하루 채팅방 나가기 2회 시 아이템을 사용해야 합니다.\", \"price\": 주사위 7개}")
                                             @Valid @RequestParam String itemPostDtoString,
                                         @Parameter(name = "image", description = "아이템 이미지 파일 (선택)", example = "thumbnail.jpg",
                                                 content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
                                            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        ItemDto.Post postDto = jsonParserUtil.parse(itemPostDtoString, ItemDto.Post.class);
        Item createdItem = itemService.createItem(mapper.itemPostToItem(postDto), imageFile);
        URI location = UriCreator.createUri(ITEM_DEFAULT_URL, createdItem.getItemId());
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "아이템 수정", description = "관리자가 기존에 등록된 아이템을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "아이템 수정 성공",
                            content = @Content(schema = @Schema(implementation = ItemDto.Response.class))),
                    @ApiResponse(responseCode = "403", description = "수정 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}"))),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방 수정 요청",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"The requested resource could not be found.\"}")))}
    )
    @PatchMapping("/{item-id}")
    public ResponseEntity<SingleResponseDto<ItemDto.Response>> patchItem(@Parameter(name = "item-id", description = "수정할 아이템의 ID", example = "10")
                                        @PathVariable("item-id") @Positive long itemId,
                                    @Parameter(name = "itemPatchDtoString", description = "수정할 아이템 정보가 포함된 JSON 문자열",
                                            example = "{\"itemName\": \"(단체)채팅방 나가기\", \"description\": \"하루 채팅방 나가기 2회 시 아이템을 사용해야 합니다.\", \"price\": 주사위 7개}")
                                    @Valid @RequestParam String itemPatchDtoString,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        ItemDto.Patch patchDto = jsonParserUtil.parse(itemPatchDtoString, ItemDto.Patch.class);
        patchDto.setItemId(itemId);
        Item item = itemService.updateItem(mapper.itemPatchToItem(patchDto), imageFile);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.itemToItemResponse(item)), HttpStatus.OK);
    }

    @Operation(summary = "아이템 목록 조회", description = "관리자가 등록된 아이템 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "아이템 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = ChatRoomDto.SingleResponse.class))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping
    public ResponseEntity<MultiResponseDto<ItemDto.Response>> getItems(@Parameter(name = "page", description = "조회할 페이지 번호 (1부터 시작)", example = "1")
                                                                           @Positive @RequestParam int page,
                                                                       @Parameter(name = "size", description = "한 페이지당 항목 수", example = "10")
                                                                           @Positive @RequestParam int size) {
        Page<Item> itemPage = itemService.findItems(page, size);
        List<Item> items = itemPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.itemsToItemResponses(items), itemPage), HttpStatus.OK);
    }
    @Operation(summary = "아이템 상세 조회", description = "관리자가 특정 아이템을 상세 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "아이템 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = ChatRoomDto.SingleResponse.class))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )

    @GetMapping("/{item-id}")
    public ResponseEntity<SingleResponseDto<ItemDto.Response>> getItem(@Parameter(name = "item-id", description = "상세 조회할 아이템의 ID", example = "10")
                                                                           @PathVariable("item-id") @Positive long itemId) {
        Item item = itemService.findItem(itemId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.itemToItemResponse(item)), HttpStatus.OK);
    }
    @Operation(summary = "아이템 삭제", description = "관리자가 특정 아이템을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "아이템 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "삭제 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @DeleteMapping("/{item-id}")
    public ResponseEntity<Void> deleteItem(@Parameter(name = "item-id", description = "삭제할 아이템의 ID", example = "10")
                                               @PathVariable("item-id") long itemId) {
        itemService.deleteItem(itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
