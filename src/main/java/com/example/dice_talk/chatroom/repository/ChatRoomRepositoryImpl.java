package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.entity.QChatRoom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public ChatRoomRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<ChatRoom> searchChatRooms(Long themeId, ChatRoom.RoomStatus roomStatus, ChatRoom.RoomType roomType, Long chatRoomId, String createdAtStart, String createdAtEnd, Pageable pageable) {
        QChatRoom chatRoom = QChatRoom.chatRoom;
        BooleanBuilder builder = new BooleanBuilder();

        if (themeId != null) {
            builder.and(chatRoom.theme.themeId.eq(themeId));
        }
        if (roomStatus != null) {
            builder.and(chatRoom.roomStatus.eq(roomStatus));
        }
        if (roomType != null) {
            builder.and(chatRoom.roomType.eq(roomType));
        }
        if (chatRoomId != null) {
            builder.and(chatRoom.chatRoomId.eq(chatRoomId));
        }
        if (createdAtStart != null && !createdAtStart.isEmpty()) {
            builder.and(chatRoom.createdAt.goe(LocalDateTime.parse(createdAtStart)));
        }
        if (createdAtEnd != null && !createdAtEnd.isEmpty()) {
            builder.and(chatRoom.createdAt.loe(LocalDateTime.parse(createdAtEnd)));
        }

        List<ChatRoom> content = queryFactory
                .selectFrom(chatRoom)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(chatRoom.createdAt.desc())
                .fetch();

        long total = queryFactory
                .selectFrom(chatRoom)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }
}
