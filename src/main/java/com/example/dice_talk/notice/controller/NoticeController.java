package com.example.dice_talk.notice.controller;

import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.notice.dto.NoticeDto;
import com.example.dice_talk.notice.entity.Notice;
import com.example.dice_talk.notice.mapper.NoticeMapper;
import com.example.dice_talk.notice.service.NoticeService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.JsonParserUtil;
import com.example.dice_talk.utils.UriCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Notice", description = "공지사항/이벤트 API")
@SecurityRequirement(name = "JWT")
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

    @Operation(summary = "공지/이벤트 등록", description = "새로운 공지사항 또는 이벤트를 등록합니다.")
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
    public ResponseEntity<Void> postNotice(
            @Parameter(description = "공지 생성 DTO 문자열(JSON)")
            @RequestParam("noticePostDto") String noticePostDtoString,
            @Parameter(description = "공지 이미지 파일 목록", required = false)
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            @Parameter(description = "썸네일 플래그 문자열 목록(JSON)", required = false)
            @RequestParam(value = "thumbnailFlags", required = false) List<String> thumbnailFlagsStr
    ) throws IOException {
        AuthorizationUtils.isAdmin();

        // JSON -> DTO 수동 파싱
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

    @Operation(summary = "공지/이벤트 수정", description = "기존 공지사항 또는 이벤트를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = NoticeDto.Response.class))
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
    @PatchMapping(value = "/{notice-id}", consumes = "multipart/form-data")
    public ResponseEntity<SingleResponseDto<NoticeDto.Response>> patchNotice(
            @Parameter(description = "공지 ID", example = "1")
            @PathVariable("notice-id") @Positive long noticeId,
            @Parameter(description = "공지 수정 DTO 문자열(JSON)")
            @RequestParam("noticePatchDto") String noticePatchDtoString,
            @Parameter(description = "공지 이미지 파일 목록", required = false)
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            @Parameter(description = "썸네일 여부 목록", required = false)
            @RequestPart(value = "thumbnailFlags", required = false) List<Boolean> thumbnailFlags
    ) throws IOException {
        AuthorizationUtils.isAdmin();
        NoticeDto.Patch patchDto = jsonParserUtil.parse(noticePatchDtoString, NoticeDto.Patch.class);

        patchDto.setNoticeId(noticeId);
        Notice updated = noticeService.updateNotice(patchDto, imageFiles, thumbnailFlags);

        return new ResponseEntity<>(new SingleResponseDto<>(mapper.noticeToNoticeResponse(updated)), HttpStatus.OK);
    }

    @Operation(summary = "공지/이벤트 상세 조회", description = "특정 공지사항 또는 이벤트의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NoticeDto.Response.class))
            ),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @GetMapping("/{notice-id}")
    public ResponseEntity<SingleResponseDto<NoticeDto.Response>> getNotice(@Parameter(description = "공지 ID", example = "1")
                                                                           @PathVariable("notice-id") @Positive long noticeId) {
        Notice findNotice = noticeService.findNotice(noticeId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.noticeToNoticeResponse(findNotice)), HttpStatus.OK);
    }

    @Operation(summary = "공지/이벤트 목록 조회", description = "전체 공지사항 및 이벤트 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NoticeDto.Response.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 파라미터",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            )
    })
    @GetMapping
    public ResponseEntity<MultiResponseDto<NoticeDto.Response>> getNotices(@Parameter(description = "페이지 번호(1 이상)", example = "1") @RequestParam @Positive int page,
                                                                           @Parameter(description = "페이지 크기(1 이상)", example = "10") @RequestParam @Positive int size) {
        Page<Notice> noticePage = noticeService.findNotices(page, size);
        List<Notice> notices = noticePage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.noticesToNoticeResponses(notices), noticePage), HttpStatus.OK);
    }

    @Operation(summary = "공지/이벤트 삭제", description = "특정 공지사항 또는 이벤트를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @DeleteMapping("/{notice-id}")
    public ResponseEntity<Void> deleteNotice(@Parameter(description = "공지 ID", example = "1")
                                             @PathVariable("notice-id") @Positive long noticeId) {
        noticeService.deleteNotice(noticeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}