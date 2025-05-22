package com.example.dice_talk.notification.mapper;

import com.example.dice_talk.notification.dto.NotificationDto;
import com.example.dice_talk.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "member.memberId", source = "receiverId")
    @Mapping(target = "read", constant = "false")
    Notification postToNotification(NotificationDto.Post postDto);

    @Mapping(target = "receiverId", source = "member.memberId")
    NotificationDto.Response notificationToResponse(Notification notification);

    List<NotificationDto.Response> notificationsToResponses(List<Notification> notifications);
}
