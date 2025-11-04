package kr.co.ongil.domain.notification.entity;

import jakarta.persistence.*;
import kr.co.ongil.domain.notification.dto.request.NotificationRequest;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "notification_type_enum")
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * 알림을 읽음 처리하는 메서드
     */
    public void markAsRead() {
        this.isRead = true;
    }

    public static Notification from(NotificationRequest request, User user) {
        return Notification.builder()
            .user(user)
            .title(request.title())
            .content(request.content())
            .type(request.type())
            .isRead(false)
            .build();
    }
}