package com.example.dice_talk.notification.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.notification.dto.NotificationDto;
import com.example.dice_talk.notification.entity.Notification;
import com.example.dice_talk.notification.mapper.NotificationMapper;
import com.example.dice_talk.notification.service.NotificationService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Tag(name = "Notification", description = "앱 내 알림 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/notifications")
@Validated
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper mapper;

    public NotificationController(NotificationService notificationService, NotificationMapper mapper) {
        this.notificationService = notificationService;
        this.mapper = mapper;
    }

    @Operation(summary = "알림 생성", description = "새로운 알림을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
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
            )
    })
    @PostMapping
    public ResponseEntity<SingleResponseDto<NotificationDto.Response>> createNotification(
            @Parameter(description = "알림 생성 DTO", required = true,
                    content = @Content(schema = @Schema(implementation = NotificationDto.Post.class)))
            @Valid @RequestBody NotificationDto.Post postDto){
        Notification created = notificationService.createNotification(
                mapper.postToNotification(postDto)
        );
        NotificationDto.Response response = mapper.notificationToResponse(created);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.CREATED);
    }

    // 특정회원의 알림리스트 조회
    @Operation(summary = "알림 목록 조회", description = "현재 로그인한 사용자의 알림 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationDto.Response.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            )
    })
    @GetMapping
    public ResponseEntity<MultiResponseDto<NotificationDto.Response>> getNotifications(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal,
                                           @Parameter(description = "페이지 번호(1 이상)", example = "1") @Positive @RequestParam int page,
                                           @Parameter(description = "페이지 크기(1 이상)", example = "10") @Positive @RequestParam int size){
        Page<Notification> notificationPage = notificationService.getNotifications(
                customPrincipal.getMemberId(), PageRequest.of(page -1, size, Sort.by("createdAt").descending())
        );
        List<NotificationDto.Response> responses = mapper.notificationsToResponses(notificationPage.getContent());
        return new ResponseEntity<>(new MultiResponseDto<>(responses, notificationPage), HttpStatus.OK);
    }

    @Operation(summary = "읽지 않은 알림 수 조회", description = "현재 로그인한 사용자의 읽지 않은 알림 개수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<SingleResponseDto<Long>> getUnreadCount(@AuthenticationPrincipal CustomPrincipal customPrincipal){
        long count = notificationService.countUnread(customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(count), HttpStatus.OK);
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "현재 로그인한 사용자의 모든 알림을 읽음 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공")
    })
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        notificationService.markAllAsRead(customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 알림 선택삭제
    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @DeleteMapping("/{notification-id}")
    public ResponseEntity<Void> deleteNotification(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal,
                                                   @Parameter(description = "알림 ID", example = "10") @PathVariable("notification-id") long notificationId){
        Notification notification = notificationService.findVerifiedNotification(notificationId);
        AuthorizationUtils.isAdminOrOwner(notification.getMember().getMemberId(), customPrincipal.getMemberId());
        return ResponseEntity.noContent().build();
    }

    // 특정 회원의 알림 전체삭제
    @Operation(summary = "모든 알림 삭제", description = "현재 로그인한 사용자의 모든 알림을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotification(@Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        notificationService.deleteAllNotification(customPrincipal.getMemberId());
        return ResponseEntity.noContent().build();
    }


}
