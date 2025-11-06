package kr.co.ongil.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.notification.entity.Notification;
import kr.co.ongil.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

@Schema(name = "NotificationResponse", description = "알림 상세 응답")
public record NotificationResponse(
    @Schema(description = "알림 ID", example = "123")
    Integer notificationId,

    @Schema(description = "제목", example = "보호자 요청 알림")
    String title,

    @Schema(description = "내용", example = "도움 요청이 접수되었습니다.")
    String content,

    @Schema(description = "알림 유형", implementation = NotificationType.class, example = "HELP_REQUEST")
    NotificationType type,

    @Schema(description = "읽음 여부", example = "false")
    Boolean isRead,

    @Schema(description = "생성 시각", type = "string", format = "date-time", example = "2025-11-03T15:32:10")
    LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getTitle(),
            notification.getContent(),
            notification.getType(),
            Boolean.TRUE.equals(notification.getIsRead()),
            notification.getCreatedAt()
        );
    }
}
