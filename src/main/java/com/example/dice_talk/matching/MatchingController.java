package com.example.dice_talk.matching;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.chatroom.service.ChatRoomService;
import com.example.dice_talk.member.entity.Member;
import com.example.dice_talk.member.service.MemberService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "채팅방 매칭 요청",
            description = "로그인한 사용자가 채팅방 매칭을 요청합니다. 매칭이 완료되면 채팅방 ID가 반환되며, 그렇지 않으면 대기 중 상태가 응답됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "매칭 성공 또는 대기 상태 응답",
                            content = @Content(examples = {
                                    @ExampleObject(name = "매칭 성공", value = "{ \"message\": \"채팅방이 생성되었습니다.\", \"chatRoomId\": 123 }"),
                                    @ExampleObject(name = "매칭 대기중", value = "{ \"message\": \"매칭 대기중입니다.\" }")})
                    ),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))
                    )}
    )
    @PostMapping("/join")
    public ResponseEntity<?> joinMatching(@Parameter(hidden = true) Authentication authentication) {
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

    @Operation(summary = "랜덤 닉네임 할당", description = "회원 ID를 기준으로 아직 사용되지 않은 닉네임 중 하나를 랜덤으로 할당합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "닉네임 할당 성공",
                            content = @Content(examples = @ExampleObject(value = "{ \"nickname\": \"한가로운 하나\" }"))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "409", description = "사용 가능한 닉네임이 모두 소진됨",
                            content = @Content(examples = @ExampleObject(value = "{\"error\": \"CONFLICT\", \"message\": \"No available nicknames remaining.\"}")))}
    )
    @PostMapping("/nickname/{memberId}")
    public ResponseEntity<Map<String, String>> assignNickname(@Parameter(name = "memberId", description = "닉네임을 할당할 회원의 ID", example = "42")
                                                                  @PathVariable Long memberId) {
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

    @Operation(summary = "사용 닉네임 회수", description = "사용 중인 닉네임을 회수하여 다시 사용할 수 있도록 만듭니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "닉네임 회수 성공"),
                    @ApiResponse(responseCode = "400", description = "존재하지 않는 닉네임",
                            content = @Content(examples = @ExampleObject(
                                            value = "{\"error\": \"BAD_REQUEST\", \"message\": \"Nickname is invalid or does not exist in the pool.\"}")))}
    )
    // 선택: 나갔을 때 호출해서 사용 닉네임 회수
    @PostMapping("/nickname/release")
    public ResponseEntity<Void> releaseNickname(@Parameter(name = "nickname", description = "회수할 닉네임 문자열", example = "호이가 계속되면 두리")
                                                    @RequestBody String nickname) {
        usedNicknames.remove(nickname);
        return ResponseEntity.ok().build();
    }

}