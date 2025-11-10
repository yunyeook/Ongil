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
    public NotificationResponse createNotifications(NotificationRequest notificationRequest,Integer relatedTableId) {
        User sender = userRepository.findById(notificationRequest.senderId()).orElse(null);
        User receiver = userRepository.findById(notificationRequest.receiverId()).orElse(null);

        if(sender==null || receiver==null) return null;
        Notification notification = Notification.of(notificationRequest,sender,receiver);
        // DB 저장
        notificationRepository.save(notification);

        //FCM 알림 발송
        fcmService.sendNotification(notification,relatedTableId);

        log.info("알림 생성 완료 - type: {}, senderId: {}",
            notificationRequest.type(), sender.getId());

        return NotificationResponse.from(notification);
    }

    /**
     * 통화 알림 히스토리 저장 (DB만, FCM 없음)
     * 나중에 사용자가 "누가 전화했는지", "부재중 전화" 등을 조회할 수 있도록 기록
     */
    @Transactional
    public NotificationResponse saveCallNotificationHistory(NotificationRequest request, Integer callId) {
        User sender = userRepository.findById(request.senderId()).orElse(null);
        User receiver = userRepository.findById(request.receiverId()).orElse(null);

        if (sender == null || receiver == null) {
            log.warn("통화 알림 히스토리 저장 실패 - 사용자 없음: senderId={}, receiverId={}",
                request.senderId(), request.receiverId());
            return null;
        }

        Notification notification = Notification.of(request, sender, receiver);
        notificationRepository.save(notification);

        // FCM은 보내지 않음 (히스토리 저장만)
        log.info("통화 알림 히스토리 저장 완료 - type: {}, callId: {}, senderId: {}, receiverId: {}",
            request.type(), callId, sender.getId(), receiver.getId());

        return NotificationResponse.from(notification);
    }

    // 1) 전화가 왔어요 (수신자에게) - 히스토리만 저장
    @Transactional
    public void notifyCallIncoming(Integer callerId, Integer calleeId, Integer callId, String callerName) {
        NotificationRequest req = NotificationRequest.of(
            "전화가 왔어요",
            callerName + " 님의 전화",
            NotificationType.CALL_INCOMING,
            callerId,   // sender
            calleeId    // receiver
        );
        saveCallNotificationHistory(req, callId);
        log.info("[NOTI] CALL_INCOMING 히스토리 저장: callerId={}, calleeId={}, callId={}", callerId, calleeId, callId);
    }

    // 2) 부재중 통화 (연결 없이 종료 → 수신자에게) - DB + FCM 둘 다
    @Transactional
    public void notifyCallMissed(Integer callerId, Integer calleeId, Integer callId, String callerName) {
        NotificationRequest req = NotificationRequest.of(
            "부재중 통화",
            callerName + " 님의 전화를 받지 못했어요",
            NotificationType.CALL_MISSED,
            callerId,   // sender(이벤트 유발자 의미)
            calleeId    // receiver(못 받은 사람)
        );
        // 부재중은 FCM 푸시도 전송
        createNotifications(req, callId);
        log.info("[NOTI] CALL_MISSED 알림 전송: callerId={}, calleeId={}, callId={}", callerId, calleeId, callId);
    }

    // 3) 통화 거절 (수신자가 거절 → 발신자에게) - 히스토리만 저장
    @Transactional
    public void notifyCallRejected(Integer callerId, Integer calleeId, Integer callId, String calleeName) {
        NotificationRequest req = NotificationRequest.of(
            "통화 거절",
            calleeName + " 님이 통화를 거절했습니다",
            NotificationType.CALL_REJECTED,
            calleeId,   // sender(거절한 쪽)
            callerId    // receiver(발신자)
        );
//        // 거절 알림 발송
//        createNotifications(req, callId);
        // 거절은 히스토리만 저장
        saveCallNotificationHistory(req, callId);
        log.info("[NOTI] CALL_REJECTED 히스토리 저장: callerId={}, calleeId={}, callId={}", callerId, calleeId, callId);
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