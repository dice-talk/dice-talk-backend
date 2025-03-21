package com.example.dice_talk.chatroom.repository;

import com.example.dice_talk.chatroom.entity.ChatRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*특정 채팅방 조회 시 연관된 메세지도 즉시 로딩*/
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /*특정 채팅방 조회 시, 메세지도 함께 로딩

    - @EntityGraph(attributePaths = {"messages"} 사용
    - 기본적으로 JPA는 @OneToMany 관계를 지연 로딩(LAZY) 하지만
    이 설정을 적용하면 `chatRooms.getChats` 호출 시 쿼리를 추가 실행하지 않고 즉시 로딩한다.
    - 채팅방을 조회할 때 연관된 메세지 목록을 함께 가져와야 하는 경우에 유용하다.
    @param id : 조회할 채팅방의 ID
    @return 해당 ID의 채팅방 객체(Optional)*/
    @EntityGraph(attributePaths = {"chats"}) // messages 컬렉션을 즉시 로딩
    Optional<ChatRoom> findById(Long id);
}
