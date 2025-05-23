package com.example.dice_talk.roomevent.repository;

import com.example.dice_talk.roomevent.entity.RoomEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomEventRepository extends JpaRepository<RoomEvent, Long> {
    List<RoomEvent> findAllByChatRoom_ChatRoomId(long chatRoomId);

    Page<RoomEvent> findAllByEvent_EventNameAndReceiverId(String eventName, long receiverId, Pageable pageable);
}
