package com.example.dice_talk.notice.controller;

import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.notice.entity.Notice;
import com.example.dice_talk.notice.mapper.NoticeMapper;
import com.example.dice_talk.notice.service.NoticeService;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.JsonParserUtil;
import com.example.dice_talk.utils.UriCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notices")
@Validated
public class NoticeController {
    private final static String NOTICE_DEFAULT_URL = "/notice";
    private final NoticeService noticeService;
    private final NoticeMapper mapper;
    private final JsonParserUtil jsonParserUtil;

    public NoticeController(NoticeService noticeService, NoticeMapper mapper, JsonParserUtil jsonParserUtil) {
        this.noticeService = noticeService;
        this.mapper = mapper;
        this.jsonParserUtil = jsonParserUtil;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity postNotice(
            @RequestParam("noticePostDto") String noticePostDtoString,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "thumbnailFlags", required = false) List<String> thumbnailFlagsStr
    ) throws IOException {
        AuthorizationUtils.isAdmin();

        // JSON -> DTO 수동 파싱
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        NoticeDto.Post parsedDto = objectMapper.readValue(noticePostDtoString, NoticeDto.Post.class);
        NoticeDto.Post parsedDto = jsonParserUtil.parse(noticePostDtoString, NoticeDto.Post.class);
        // 문자열로 들어온 썸네일 여부 파싱
        List<Boolean> thumbnailFlags = null;
        if (thumbnailFlagsStr != null) {
            thumbnailFlags = thumbnailFlagsStr.stream()
                    .map(thumnailFlag -> Boolean.parseBoolean(thumnailFlag))
                    .collect(Collectors.toList());
        }

        Notice createdNotice = noticeService.createNotice(parsedDto, imageFiles, thumbnailFlags);
        URI location = UriCreator.createUri(NOTICE_DEFAULT_URL, createdNotice.getNoticeId());
        return ResponseEntity.created(location).build();
    }

    @PatchMapping(value = "/{notice-id}", consumes = "multipart/form-data")
    public ResponseEntity patchNotice(
            @RequestParam("noticePatchDto") @Valid String noticePatchDtoString,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            @RequestPart(value = "thumbnailFlags", required = false) List<Boolean> thumbnailFlags,
            @PathVariable("notice-id") @Positive long noticeId
    ) throws IOException {
        AuthorizationUtils.isAdmin();
        NoticeDto.Patch patchDto = jsonParserUtil.parse(noticePatchDtoString, NoticeDto.Patch.class);

        patchDto.setNoticeId(noticeId);
        Notice updated = noticeService.updateNotice(patchDto, imageFiles, thumbnailFlags);

        return new ResponseEntity<>(new SingleResponseDto<>(mapper.noticeToNoticeResponse(updated)), HttpStatus.OK);
    }

    @GetMapping("/{notice-id}")
    public ResponseEntity getNotice(@PathVariable("notice-id") @Positive long noticeId) {
        Notice findNotice = noticeService.findNotice(noticeId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.noticeToNoticeResponse(findNotice)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getNotices(@RequestParam @Positive int page,
                                     @RequestParam @Positive int size) {
        Page<Notice> noticePage = noticeService.findNotices(page, size);
        List<Notice> notices = noticePage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.noticesToNoticeResponses(notices), noticePage), HttpStatus.OK);
    }

    @DeleteMapping("/{notice-id}")
    public ResponseEntity deleteNotice(@PathVariable("notice-id") @Positive long noticeId) {
        noticeService.deleteNotice(noticeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}