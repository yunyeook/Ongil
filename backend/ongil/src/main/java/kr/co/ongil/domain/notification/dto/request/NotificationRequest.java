package kr.co.ongil.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.notification.entity.NotificationType;

@Schema(name = "NotificationRequest", description = "알림 생성 요청")
public record NotificationRequest(

    @Schema(description = "알림 제목", example = "보호자 요청 알림")
    String title,

    @Schema(description = "알림 내용", example = "도움 요청이 접수되었습니다.")
    String content,

    @Schema(description = "알림 유형", example = "HELP_REQUEST")
    NotificationType type,

    @Schema(description = "발신자 유저 ID", example = "101")
    Integer senderId,

   @Schema(description = "수신자 유저 ID", example = "101")
        Integer receiverId

) {
    // 수신자가 있을 때
    public static NotificationRequest of(String title, String content, NotificationType type, Integer senderId, Integer receiverId) {
        return new NotificationRequest(title, content, type, senderId, receiverId);
    }

    // 수신자가 없을 때
    public static NotificationRequest of(String title, String content, NotificationType type, Integer senderId) {
        return new NotificationRequest(title, content, type, senderId, null);
    }
}
