package kr.co.ongil.domain.notification.service;

import java.util.List;
import java.util.Map;
import kr.co.ongil.domain.notification.dto.request.NotificationRequest;
import kr.co.ongil.domain.notification.dto.response.NotificationListResponse;
import kr.co.ongil.domain.notification.dto.response.NotificationReadResponse;
import kr.co.ongil.domain.notification.dto.response.NotificationResponse;
import kr.co.ongil.domain.notification.entity.Notification;
import kr.co.ongil.domain.notification.entity.NotificationType;
import kr.co.ongil.domain.fcm.service.FcmService;
import kr.co.ongil.domain.notification.repository.NotificationRepository;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final FcmService fcmService;



    public NotificationListResponse getNotifications(Integer receiverId, int page, int size, Boolean read) {
        validatePagingParameters(page, size);

        int pageIndex = page - 1;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage;
        if (read == null) {
            notificationPage = notificationRepository.findByReceiverId(receiverId, pageable);
        } else {
            notificationPage = notificationRepository.findByReceiverIdAndIsRead(receiverId, read, pageable);
        }

        Page<NotificationResponse> responsePage = notificationPage.map(NotificationResponse::from);
        log.info("알림 목록 조회 완료 - userId: {}, page: {}, size: {}, read: {}, totalElements: {}",
            receiverId, page, size, read, responsePage.getTotalElements());

        return NotificationListResponse.of(responsePage);
    }

    @Transactional
    public NotificationReadResponse markAsRead(Integer receiverId, Integer notificationId) {
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, receiverId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();
        log.info("알림 읽음 처리 완료 - notificationId: {}", notificationId);

        return NotificationReadResponse.from(notification);
    }
    @Transactional
    public List<NotificationResponse> markAsReadAll(Integer receiverId) {
        List<Notification> notifications = notificationRepository.findByReceiverId(receiverId);

        if (notifications.isEmpty()) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notifications.forEach(Notification::markAsRead);
        log.info("알림 {}건 읽음 처리 완료 - userId: {}", notifications.size(), receiverId);

        return notifications.stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @Transactional
    public Map<String, Integer> deleteNotification(Integer receiverId, Integer notificationId) {
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, receiverId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notificationRepository.delete(notification);
        log.info("알림 삭제 완료 - notificationId: {}", notificationId);

        return Map.of("deletedNotificationId", notification.getId());
    }


    @Transactional
    public Map<String, Integer> deleteAllNotifications(Integer receiverId) {
        List<Notification> notifications = notificationRepository.findByReceiverId(receiverId);

        if (notifications.isEmpty()) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notificationRepository.deleteAll(notifications);
        log.info("알림 전체 삭제 완료 - userId: {}, 삭제된 알림 수: {}", receiverId, notifications.size());
        return Map.of("deleteCount", notifications.size());

    }

    @Transactional
    public List<NotificationResponse> createNotifications(NotificationRequest request) {
        Integer senderId = request.senderId();
        Integer receiverId = request.receiverId();
        NotificationType type = request.type();

        // Sender 조회
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<User> targetUsers;

        if (receiverId != null) {
            // 수신자가 지정된 경우
            User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            targetUsers = List.of(receiver);
        } else {
            // 수신자가 없으면, 관계등록시 연결된 보호자들
            targetUsers = relationshipRepository.findGuardiansByPatientId(senderId);
            if (targetUsers.isEmpty()) {
                throw new BusinessException(ErrorCode.NO_GUARDIAN_FOUND);
            }
        }

        //본인도 포함
        targetUsers.add(sender);
        List<Notification> notifications = targetUsers.stream()
            .map(receiver -> Notification.from(request, sender, receiver))
            .toList();

        notificationRepository.saveAll(notifications);

        // FCM 알림 전송 (주석 처리된 부분 수정)
        // targetUsers.forEach(user -> {
        //     String token = fcmTokenRedisService.getToken(user.getId());
        //     if (token != null && !token.isBlank()) {
        //         fcmService.sendNotification(
        //             token,
        //             type.name(),  // 영어 타입명
        //             type.getDescription()  // 한글 설명
        //         );
        //     }
        // });

        log.info("알림 생성 완료 - type: {}, senderId: {}, receiverCount: {}",
            type, senderId, notifications.size());

        return notifications.stream()
            .map(NotificationResponse::from)
            .toList();
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