package com.example.dice_talk.notification.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.notification.dto.NotificationDto;
import com.example.dice_talk.notification.entity.Notification;
import com.example.dice_talk.notification.mapper.NotificationMapper;
import com.example.dice_talk.notification.service.NotificationService;
import com.example.dice_talk.utils.AuthorizationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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

    @PostMapping
    public ResponseEntity createNotification(@Valid @RequestBody NotificationDto.Post postDto){
        Notification created = notificationService.createNotification(
                mapper.postToNotification(postDto)
        );
        NotificationDto.Response response = mapper.notificationToResponse(created);
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.CREATED);
    }

    // 특정회원의 알림리스트 조회
    @GetMapping
    public ResponseEntity getNotifications(@AuthenticationPrincipal CustomPrincipal customPrincipal,
                                           @RequestParam("page") int page,
                                           @RequestParam("size") int size){
        Page<Notification> notificationPage = notificationService.getNotifications(
                customPrincipal.getMemberId(), PageRequest.of(page -1, size, Sort.by("createdAt").descending())
        );
        List<NotificationDto.Response> responses = mapper.notificationsToResponses(notificationPage.getContent());
        return new ResponseEntity<>(new MultiResponseDto<>(responses, notificationPage), HttpStatus.OK);
    }

    @GetMapping("/unread-count")
    public ResponseEntity getUnreadCount(@AuthenticationPrincipal CustomPrincipal customPrincipal){
        long count = notificationService.countUnread(customPrincipal.getMemberId());
        return new ResponseEntity<>(new SingleResponseDto<>(count), HttpStatus.OK);
    }

    @PatchMapping("/read-all")
    public ResponseEntity markAllAsRead(@AuthenticationPrincipal CustomPrincipal customPrincipal){
        notificationService.markAllAsRead(customPrincipal.getMemberId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 알림 선택삭제
    @DeleteMapping("/{notification-id}")
    public ResponseEntity deleteNotification(@AuthenticationPrincipal CustomPrincipal customPrincipal,
                                             @PathVariable("notification-id") long notificationId){
        Notification notification = notificationService.findVerifiedNotification(notificationId);
        AuthorizationUtils.isAdminOrOwner(notification.getMember().getMemberId(), customPrincipal.getMemberId());
        return ResponseEntity.noContent().build();
    }

    // 특정 회원의 알림 전체삭제
    @DeleteMapping
    public ResponseEntity deleteAllNotification(@AuthenticationPrincipal CustomPrincipal customPrincipal){
        notificationService.deleteAllNotification(customPrincipal.getMemberId());
        return ResponseEntity.noContent().build();
    }


}
