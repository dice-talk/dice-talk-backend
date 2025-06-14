package com.example.dice_talk.exception;

import lombok.Getter;

public enum ExceptionCode {
    MEMBER_NOT_FOUND(404, "Member not found"),
    MEMBER_EXISTS(409, "Member exists"),
    LOGOUT_FAILED(404, "Logout failed"),
    USERNAME_NOT_FOUND(404, "Username Not Found"),
    INVALID_MEMBER_STATUS(400, "Invalid member status"),
    UNAUTHORIZED_OPERATION(403, "Can not access this resource"),
    ANSWER_NOT_FOUND(404, "Answer not found"),
    ANSWER_EXISTS(409, "Answer exists"),
    CANNOT_CHANGE_QUESTION(403, "Question already answered"),
    QUESTION_NOT_FOUND(404, "Question not found"),
    ITEM_NOT_EXIST(404, "Item not found"),
    NOTICE_NOT_EXIST(404, "Notice not found"),
    THEME_NOT_FOUND(404, "Theme not found"),
    ITEM_NOT_FOUND(404, "Item not found"),
    PRODUCT_NOT_FOUND(404, "Product not found"),
    REPORT_NOT_FOUND(404, "Report Not found"),
    EVENT_NOT_FOUND(404, "Event not found"),
    CHATROOM_NOT_FOUND(404, "Chatroom not found"),
    DICE_LOG_NOT_FOUND(404, "DiceLog not found"),
    NOT_ENOUGH_DICE(400, "Dice not Enough"),
    CANNOT_CANCEL(400, "Cannot cancel log"),
    LOG_TYPE_INVALID(400, "LogType invalid"),
    CHAT_NOT_FOUND(404, "Chat not found"),
    UNAUTHORIZED(401, "Unauthorized"),
    REPORT_ALREADY_COMPLETED(409, "Report Already Completed"),
    REPORT_ALREADY_REJECTED(409, "Report Already Rejected"),
    AUTH_INVALID_PASSWORD(401, "Auth Invalid Password"),
    NOTIFICATION_NOT_FOUND(404, "Notification not found"),
    INVALID_TOKEN(401, "Invalid Token"),
    INVALID_IMAGE_METADATA(400, "썸네일 여부 리스트가 이미지 개수와 일치하지 않습니다."),
    NOTICE_ALREADY_CLOSED(409, "Notice is already closed"),
    TOSS_RESPONSE_ERROR(409,"Toss 응답에 success 객체가 없습니다."),
    TOSS_VERIFICATION_FAILED(401,"Toss 인증이 실패했습니다."),
    TOSS_CI_NOT_FOUND(401,"Toss CI를 찾을 수 없습니다."),
    EMAIL_MISMATCH(401,"Toss email 매칭에 실패했습니다."),
    BAD_REQUEST(400, "Invalid sort or column"),
    QUESTION_DELETED(404, "Question Deleted"),
    ALREADY_EXITED_TODAY(409, "Member Already Existed Today"),
    
    // Payment 관련 예외 코드 추가
    PAYMENT_NOT_FOUND(404, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_MASTER_NOT_FOUND(404, "Payment master not found"),
    INVALID_PAYMENT_AMOUNT(400, "유효하지 않은 결제 금액입니다."),
    PAYMENT_FAILED(400, "결제에 실패했습니다."),
    PAYMENT_ALREADY_COMPLETED(400, "이미 완료된 결제입니다."),
    PAYMENT_ALREADY_FAILED(400, "이미 실패한 결제입니다."),
    PAYMENT_ALREADY_CANCELLED(409, "Payment is already cancelled"),
    PAYMENT_ALREADY_REFUNDED(409, "Payment is already refunded"),
    PAYMENT_VERIFICATION_FAILED(401, "Payment verification failed"),
    PAYMENT_AMOUNT_MISMATCH(400, "결제 금액이 일치하지 않습니다."),
    PAYMENT_TIMEOUT(400, "결제 시간이 초과되었습니다."),
    INVALID_REFUND_STATUS(400, "환불이 불가능한 결제 상태입니다."),
    REFUND_FAILED(400, "환불 처리에 실패했습니다.");

    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int code, String message) {
        this.status = code;
        this.message = message;
    }
}
