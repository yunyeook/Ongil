package kr.co.ongil.domain.navigation.service;

import java.util.List;
import kr.co.ongil.domain.map.dto.response.RouteResponse;
import kr.co.ongil.domain.map.service.MapService;
import kr.co.ongil.domain.navigation.dto.request.EndNavigationRequest;
import kr.co.ongil.domain.navigation.dto.request.StartNavigationRequest;
import kr.co.ongil.domain.navigation.dto.response.EndNavigationResponse;
import kr.co.ongil.domain.navigation.dto.response.NavigationSessionResponse;
import kr.co.ongil.domain.navigation.entity.NavigationLog;
import kr.co.ongil.domain.notification.dto.request.NotificationRequest;
import kr.co.ongil.domain.notification.entity.NotificationType;
import kr.co.ongil.domain.notification.service.NotificationService;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavigationService {

    private final MapService mapService;
    private final NavigationRedisService navigaionRedisService;
    private final NavigationLogService novigationLogService;
    private final NotificationService notificationService;
    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;


    /**
     * 길안내 시작
     */
    public NavigationSessionResponse startNavigation(StartNavigationRequest request,Integer senderId) {

        log.info("길안내 시작: patientId={}, initiatedBy={}",
            request.patientId(), request.initiatedBy());

        if (navigaionRedisService.hasActiveSession(request.patientId())) {
            throw new BusinessException(ErrorCode.NAVIGATION_ALREADY_ACTIVE);
        }
        // 1. 경로 조회 (TMAP API 호출)
        RouteResponse route = mapService.getPedestrianRoute(
            request.startLocation().latitude(),
            request.startLocation().longitude(),
            request.endLocation().latitude(),
            request.endLocation().longitude(),
            request.startLocation().name(),
            request.endLocation().name()
        );

        // 2. 시간 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectedArrival = now.plusSeconds(route.totalTime());

        // 3. DB 로그 생성 (ID 생성)
        NavigationLog navigationLog = novigationLogService.createLog(
            request.patientId(),
            route,
            now,
            request.initiatedBy()
        );

        // 4. navigationId 생성
        String navigationId = navigationLog.getId().toString();

        // 5. Redis에 세션 저장 (patientId를 키로 사용)
        navigaionRedisService.saveNavigationSession(request.patientId(), navigationId, route);

        // 6. 보호자에게 알림 전송
        sendNavigationStartNotification(userRepository.findById(senderId).get());


        log.info("길안내 시작 완료: navigationId={}", navigationId);

        // 6. 응답 생성
        return NavigationSessionResponse.of(
            navigationId,
            route,
            now,
            expectedArrival,
            request.initiatedBy()
        );
    }

    /**
     * 길안내 종료
     */
    public EndNavigationResponse endNavigation(EndNavigationRequest request,Integer senderId) {

        log.info("길안내 종료: patientId={}, navigationId={}",
            request.patientId(), request.navigationId());

        // 1. DB 로그 완료 처리
        NavigationLog completedLog = novigationLogService.completeLog(
            request.navigationId(),
            request.isSuccessful()
        );

        // 2. Redis 세션 삭제
        navigaionRedisService.endSession(request.patientId());

        log.info("길안내 종료 완료: navigationId={}, isSuccessful={}",
            request.navigationId(), completedLog.getIsSuccessful());

        // 3. 보호자에게 알림 전송
        sendNavigationEndNotification(userRepository.findById(senderId).get());

        // 4. 응답
        return EndNavigationResponse.of(
            request.navigationId().toString(),
            completedLog.getStartedAt(),
            completedLog.getEndedAt(),
            completedLog.getIsSuccessful()
        );

    }

    /**
     * 길안내 시작 알림 전송
     */
    private void sendNavigationStartNotification(User sender) {
        try {
            List<User> receivers = relationshipRepository.findGuardiansByPatientId(sender.getId());

            //보내는 사람이 보호자인 경우나 등록된 관계가 없는경우
            if (receivers.isEmpty()) {
                log.warn("알림 전송 대상 없음 - senderId: {}", sender.getId());
                return;
            }

            // 각 보호자에게 알림 전송
            receivers.forEach(receiver -> {
                try {
                    NotificationRequest notificationRequest = NotificationRequest.of(
                        NotificationType.NAVIGATION_START.getDescription(),
                        sender.getName() + "님이 길안내를 시작하였습니다.",
                        NotificationType.NAVIGATION_START,
                        sender.getId(),
                        receiver.getId()
                    );
                    notificationService.createNotifications(notificationRequest,null);
                    log.info("길안내 시작 알림 전송 완료 - senderId: {}, receiverId: {}",
                        sender.getId(), receiver.getId());
                } catch (Exception e) {
                    log.error("알림 전송 실패 - receiverId: {}", receiver.getId(), e);
                }
            });

            log.info("길안내 시작 알림 전송 완료 - senderId: {}, 전송 대상: {}명",
                sender.getId(), receivers.size());
        } catch (Exception e) {
            log.error("길안내 시작 알림 전송 실패 - senderId: {}", sender.getId(), e);
        }
    }

    /**
     * 길안내 종료 알림 전송
     */
    private void sendNavigationEndNotification(User sender) {
        try {
            List<User> receivers = relationshipRepository.findGuardiansByPatientId(sender.getId());

            //보내는 사람이 보호자인 경우나 등록된 관계가 없는경우
            if (receivers.isEmpty()) {
                log.warn("알림 전송 대상 없음 - senderId: {}", sender.getId());
                return;
            }

            // 각 보호자에게 알림 전송
            receivers.forEach(receiver -> {
                try {
                    NotificationRequest notificationRequest = NotificationRequest.of(
                        NotificationType.NAVIGATION_END.getDescription(),
                        sender.getName() + "님이 길안내를 종료하였습니다.",
                        NotificationType.NAVIGATION_END,
                        sender.getId(),
                        receiver.getId()
                    );
                    notificationService.createNotifications(notificationRequest,null);
                    log.info("길안내 종료 알림 전송 완료 - senderId: {}, receiverId: {}",
                        sender.getId(), receiver.getId());
                } catch (Exception e) {
                    log.error("알림 전송 실패 - receiverId: {}", receiver.getId(), e);
                }
            });

            log.info("길안내 시작 알림 전송 완료 - senderId: {}, 전송 대상: {}명",
                sender.getId(), receivers.size());
        } catch (Exception e) {
            log.error("길안내 시작 알림 전송 실패 - senderId: {}", sender.getId(), e);
        }
    }

}