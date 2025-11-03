package kr.co.ongil.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.notification.entity.Notification;

import java.time.LocalDateTime;

@Schema(name = "NotificationReadResponse", description = "알림 읽음 상태 응답")
public record NotificationReadResponse(
    @Schema(description = "알림 ID", example = "123")
    Integer notificationId,

    @Schema(description = "읽음 여부", example = "true")
    Boolean isRead,

    @Schema(description = "수정 시각(읽음 처리 시각)", type = "string", format = "date-time", example = "2025-11-03T15:32:10")
    LocalDateTime updatedAt
) {
    public static NotificationReadResponse from(Notification notification) {
        return new NotificationReadResponse(
            notification.getId(),
            Boolean.TRUE.equals(notification.getIsRead()),
            notification.getUpdatedAt()
        );
    }
}
