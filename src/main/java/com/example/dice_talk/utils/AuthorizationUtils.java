package com.example.dice_talk.utils;

;
import com.example.dice_talk.exception.BusinessLogicException;
import com.example.dice_talk.exception.ExceptionCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthorizationUtils {

    // 관리자인지 확인하는 메서드
    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    public static void verifyAdmin(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))){
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
    }

    // 관리자인지 또는 동일한 사용자인지 확인하고 아니면 예외 던지는 메서드
    public static void isAdminOrOwner(long ownerId, long authenticatedId) {
        if (!isAdmin() && ownerId != authenticatedId) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
    }

    // 로그인한 사용자와 작성자가 동일한지 확인하는 메서드
    public static boolean isOwner(long ownerId, long authenticatedId) {
        if (ownerId != authenticatedId) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
        return true;
    }
}
