package kr.co.ongil.domain.notification.service;

import java.util.List;
import java.util.Map;
import kr.co.ongil.domain.notification.dto.response.NotificationListResponse;
import kr.co.ongil.domain.notification.dto.response.NotificationReadResponse;
import kr.co.ongil.domain.notification.dto.response.NotificationResponse;
import kr.co.ongil.domain.notification.entity.Notification;
import kr.co.ongil.domain.notification.repository.NotificationRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationListResponse getNotifications(Integer userId, int page, int size, Boolean read) {
        validatePagingParameters(page, size);

        int pageIndex = page - 1;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage;
        if (read == null) {
            notificationPage = notificationRepository.findByUserId(userId, pageable);
        } else {
            notificationPage = notificationRepository.findByUserIdAndIsRead(userId, read, pageable);
        }

        Page<NotificationResponse> responsePage = notificationPage.map(NotificationResponse::from);
        log.info("알림 목록 조회 완료 - userId: {}, page: {}, size: {}, read: {}, totalElements: {}",
            userId, page, size, read, responsePage.getTotalElements());

        return NotificationListResponse.of(responsePage);
    }

    @Transactional
    public NotificationReadResponse markAsRead(Integer userId, Integer notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();
        log.info("알림 읽음 처리 완료 - notificationId: {}", notificationId);

        return NotificationReadResponse.from(notification);
    }
    @Transactional
    public List<NotificationResponse> markAsReadAll(Integer userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);

        if (notifications.isEmpty()) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notifications.forEach(Notification::markAsRead);
        log.info("알림 {}건 읽음 처리 완료 - userId: {}", notifications.size(), userId);

        return notifications.stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @Transactional
    public Map<String, Integer> deleteNotification(Integer userId, Integer notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notificationRepository.delete(notification);
        log.info("알림 삭제 완료 - notificationId: {}", notificationId);

        return Map.of("deletedNotificationId", notification.getId());
    }


    @Transactional
    public Map<String, Integer> deleteAllNotifications(Integer userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);

        if (notifications.isEmpty()) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notificationRepository.deleteAll(notifications);
        log.info("알림 전체 삭제 완료 - userId: {}, 삭제된 알림 수: {}", userId, notifications.size());
        return Map.of("deleteCount", notifications.size());

    }



    private void validatePagingParameters(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1 이상 100 이하여야 합니다.");
        }
    }
}