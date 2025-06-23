package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.entity.QChatRoom;
import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.time.LocalDate;
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

    //웹페이지 : 금일 진행중인 채팅방 수
    @Override
    public int countActiveRoomTotal(LocalDateTime start, LocalDateTime end) {
        QChatRoom chatRoom = QChatRoom.chatRoom;

        Long count = queryFactory
                .select(chatRoom.count())
                .from(chatRoom)
                .where(
                        chatRoom.createdAt.between(start, end),
                        chatRoom.roomStatus.eq(ChatRoom.RoomStatus.ROOM_ACTIVE)
                )
                .fetchOne();

        return count != null ? count.intValue() : 0;
    }

    //웹페이지 : 진행중인 채팅방 수 조회
    @Override
    public List<DailyCountDto> countActiveRoomsByDate(LocalDateTime start, LocalDateTime end) {
        QChatRoom chatRoom = QChatRoom.chatRoom;

        return queryFactory
                .select(Projections.constructor(
                        DailyCountDto.class,
                        Expressions.dateTemplate(LocalDate.class, "DATE({0})", chatRoom.createdAt),
                        chatRoom.count().intValue()
                ))
                .from(chatRoom)
                .where(
                        chatRoom.createdAt.between(start, end),
                        chatRoom.roomStatus.eq(ChatRoom.RoomStatus.ROOM_ACTIVE)
                )
                .groupBy(Expressions.dateTemplate(LocalDate.class, "DATE({0})", chatRoom.createdAt))
                .orderBy(Expressions.dateTemplate(LocalDate.class, "DATE({0})", chatRoom.createdAt).asc())
                .fetch();
    }

}
