package com.example.dice_talk.matching;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingQueue matchingQueue;
    private final ChatRoomService chatRoomService;
    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<?> joinMatching(Authentication authentication) {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        Member member = memberService.findVerifiedMember(principal.getMemberId());

        return matchingQueue.addToQueue(member)
                .map(result -> ResponseEntity.ok().body(Map.of(
                        "message", "채팅방이 생성되었습니다.",
                        "chatRoomId", result.getChatRoomId()
                )))
                .orElse(ResponseEntity.ok(Map.of(
                        "message", "매칭 대기중입니다."
                )));
    }

    private final Set<String> usedNicknames = new HashSet<>();
    private final List<String> nicknamePool = List.of(
            "한가로운 하나", "호이가 계속되면 두리", "새침한데 솔직한 세찌", "네모지만 부드러운 네몽",
            "단호하지만 다정한 다오", "육감적인 직감파 육댕"
    );

    @PostMapping("/nickname/{memberId}")
    public ResponseEntity<Map<String, String>> assignNickname(@PathVariable Long memberId) {
        List<String> available = nicknamePool.stream()
                .filter(nick -> !usedNicknames.contains(nick))
                .collect(Collectors.toList());

        if (available.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "닉네임 부족"));
        }

        String assigned = available.get(new Random().nextInt(available.size()));
        usedNicknames.add(assigned);
        return ResponseEntity.ok(Map.of("nickname", assigned));
    }


    // 선택: 나갔을 때 호출해서 사용 닉네임 회수
    @PostMapping("/nickname/release")
    public ResponseEntity<Void> releaseNickname(@RequestParam String nickname) {
        usedNicknames.remove(nickname);
        return ResponseEntity.ok().build();
    }

}