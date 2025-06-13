package com.example.dice_talk.pushNotification.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import com.example.dice_talk.pushNotification.Dto.PushTokenRequestDto;
import com.example.dice_talk.pushNotification.service.PushNotificationTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/push-notifications")
public class PushNotificationTokenController {
    private final PushNotificationTokenService pushNotificationTokenService;

    public PushNotificationTokenController(PushNotificationTokenService pushNotificationTokenService) {
        this.pushNotificationTokenService = pushNotificationTokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<String> registerOrUpdatePushToken(@RequestBody PushTokenRequestDto requestDto,
                                                            @AuthenticationPrincipal CustomPrincipal customPrincipal ) {
        try {
            if (customPrincipal == null) {
                // 일반적으로 Spring Security 필터 체인에서 인증되지 않은 요청은 여기까지 오기 전에 차단되지만,
                // 방어적으로 체크하거나, customPrincipal이 null일 수 있는 특별한 설정이 있다면 필요합니다.
                throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED); // 또는 다른 적절한 예외
            }

            Long memberId = customPrincipal.getMemberId(); // CustomPrincipal에서 memberId 가져오기

            if (requestDto.getExpoPushToken() == null || requestDto.getExpoPushToken().isEmpty()) {
                return ResponseEntity.badRequest().body("Expo Push Token is required.");
            }

            pushNotificationTokenService.saveOrUpdateUserPushToken(
                    memberId, // 인증된 사용자의 memberId 사용
                    requestDto.getExpoPushToken(),
                    requestDto.getDeviceType()
            );
            return ResponseEntity.ok("Push token registered/updated successfully for member: " + memberId);
        } catch (BusinessLogicException e) { // BusinessLogicException 처리
            // ExceptionCode에 따라 적절한 HTTP 상태 코드와 메시지 반환
            // 예: return ResponseEntity.status(e.getExceptionCode().getStatus()).body(e.getExceptionCode().getMessage());
            return ResponseEntity.status(HttpStatus.valueOf(e.getExceptionCode().getStatus())).body(e.getMessage());
        }
        catch (RuntimeException e) {
            // MemberNotFoundException 등 PushNotificationTokenService에서 발생할 수 있는 다른 RuntimeException 처리
            // 로깅을 추가하는 것이 좋습니다.
            System.err.println("Error processing push token: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing push token."); // 좀 더 일반적인 오류 메시지
        }
    }
}
