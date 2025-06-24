package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import com.example.dice_talk.chatroom.entity.QChatRoom;
import com.example.dice_talk.dashboard.dto.DailyCountDto;
import com.example.dice_talk.dashboard.dto.QDailyCountDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        //변수로 선언 : 	남은 핵심은 Expressions.dateTemplate(...)을 변수로 추출해서 select/groupBy/orderBy에 중복으로 새로 생성
//        DateExpression<LocalDate> dateOnly = Expressions.dateTemplate(LocalDate.class, "DATE({0})", chatRoom.createdAt);

        DateExpression<Date> dateOnly = Expressions.dateTemplate(Date.class, "DATE({0})", chatRoom.createdAt);
        List<Tuple> tuples = queryFactory
                .select(dateOnly, chatRoom.chatRoomId.count())
                .from(chatRoom)
                .where(chatRoom.createdAt.between(start, end),
                        chatRoom.roomStatus.eq(ChatRoom.RoomStatus.ROOM_ACTIVE))
                .groupBy(dateOnly)
                .orderBy(dateOnly.asc())
                .fetch();

        return tuples.stream()
                .map(t -> new DailyCountDto(
                        t.get(dateOnly).toLocalDate(),
                        t.get(chatRoom.chatRoomId.count())
                )).collect(Collectors.toList());
    }
//        return queryFactory
//                .select(new QDailyCountDto(dateOnly, chatRoom.count()))
//                .from(chatRoom)
//                .where(
//                        chatRoom.createdAt.between(start, end),
//                        chatRoom.roomStatus.eq(ChatRoom.RoomStatus.ROOM_ACTIVE)
//                )
//                .groupBy(dateOnly)
//                .orderBy(dateOnly.asc())
//                .fetch();
//    }

}
