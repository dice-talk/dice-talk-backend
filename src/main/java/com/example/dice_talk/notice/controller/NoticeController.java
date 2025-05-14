package com.example.dice_talk.notice.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.notice.entity.Notice;
import com.example.dice_talk.notice.entity.NoticeImage;
import com.example.dice_talk.notice.mapper.NoticeMapper;
import com.example.dice_talk.notice.service.NoticeService;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.UriCreator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/notices")
@Validated
public class NoticeController {
    private final static String NOTICE_DEFAULT_URL = "/notice";
    private final NoticeService noticeService;
    private final NoticeMapper mapper;

    public NoticeController(NoticeService noticeService, NoticeMapper mapper) {
        this.noticeService = noticeService;
        this.mapper = mapper;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity postNotice(@RequestPart("noticePostDto") @Valid NoticeDto.Post postDto,
                                     @RequestPart(value = "images", required = false)List<MultipartFile> imageFiles,
                                     @RequestPart(value = "thumbnailFlags", required = false) List<Boolean> thumbnailFlags){
        // 관리자만 가능
        AuthorizationUtils.isAdmin();

        List<NoticeImage> imageEntities = new ArrayList<>();

        if (imageFiles != null && !imageFiles.isEmpty()){
            for (int i = 0; i < imageFiles.size(); i++){
                MultipartFile file = imageFiles.get(i);
                boolean isThumbnail = thumbnailFlags != null && i < thumbnailFlags.size() &&
                        Boolean.TRUE.equals(thumbnailFlags.get(i));

                // 🚩 S3 업로드 후 이미지 URL 받아오는 로직 (임시 URL 처리함)
                String imageUrl = "/dummy-url" + file.getOriginalFilename(); // TODO: S3 업로드 시 변경 필요

                NoticeImage image = new NoticeImage();
                image.setImageUrl(imageUrl);
                image.setThumbnail(isThumbnail);
                imageEntities.add(image);
            }
        }

        Notice notice = mapper.noticePostToNotice(postDto);
        imageEntities.forEach(noticeImage -> notice.setImage(noticeImage));

        Notice createdNotice = noticeService.createNotice(notice);
        URI location = UriCreator.createUri(NOTICE_DEFAULT_URL, createdNotice.getNoticeId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping(value = "/{notice-id}", consumes = "multipart/form-data")
    public ResponseEntity patchNotice(@RequestPart @Valid NoticeDto.Patch patchDto,
                                      @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
                                      @RequestPart(value = "thumbnailFlags", required = false) List<Boolean> thumbnailFlags,
                                      @PathVariable("notice-id") @Positive long noticeId) {
        // 관리자 확인
        AuthorizationUtils.isAdmin();

        patchDto.setNoticeId(noticeId);
        List<NoticeImage> imageEntities = new ArrayList<>();

        if (imageFiles != null && !imageFiles.isEmpty()){
            for (int i = 0; i < imageFiles.size(); i++){
                MultipartFile file = imageFiles.get(i);
                boolean isThumbnail = thumbnailFlags != null && i < thumbnailFlags.size() && Boolean.TRUE.equals(thumbnailFlags.get(i));

                String imageUrl = "/dummy-url/" + file.getOriginalFilename(); // TODO: S3 연동 시 변경

                NoticeImage image = new NoticeImage();
                image.setImageUrl(imageUrl);
                image.setThumbnail(isThumbnail);
                imageEntities.add(image);
            }
        }

        Notice notice = mapper.noticePatchToNotice(patchDto);
        imageEntities.forEach(noticeImage -> notice.setImage(noticeImage));

        Notice updated = noticeService.updateNotice(notice);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.noticeToNoticeResponse(updated)), HttpStatus.OK);
    }

    @GetMapping("/{notice-id}")
    public ResponseEntity getNotice(@PathVariable("notice-id") @Positive long noticeId) {
        Notice findNotice = noticeService.findNotice(noticeId);
        return new ResponseEntity<>(new SingleResponseDto<>(
                mapper.noticeToNoticeResponse(findNotice)), HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity getNotices(@RequestParam @Positive int page,
                                     @RequestParam @Positive int size) {
        Page<Notice> noticePage = noticeService.findNotices(page, size);
        List<Notice> notices = noticePage.getContent();

        return new ResponseEntity<>(new MultiResponseDto<>(mapper.noticesToNoticeResponses(notices), noticePage),
                HttpStatus.OK);
    }

    @DeleteMapping("/{notice-id}")
    public ResponseEntity deleteNotice(@PathVariable("notice-id") @Positive long noticeId){
        noticeService.deleteNotice(noticeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
