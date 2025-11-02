package kr.co.ongil.domain.navigation.service;

import kr.co.ongil.domain.map.dto.response.RouteResponse;
import kr.co.ongil.domain.map.service.MapService;
import kr.co.ongil.domain.navigation.dto.request.StartNavigationRequest;
import kr.co.ongil.domain.navigation.dto.response.NavigationSessionResponse;
import kr.co.ongil.domain.navigation.entity.NavigationLog;
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
    private final NavigationRedisService redisService;
    private final NavigationLogService logService;

    /**
     * 길안내 시작
     */
    public NavigationSessionResponse startNavigation(StartNavigationRequest request) {

        log.info("길안내 시작: patientId={}, initiatedBy={}",
            request.getPatientId(), request.getInitiatedBy());

        if (redisService.hasActiveSession(request.getPatientId())) {
            throw new BusinessException(ErrorCode.NAVIGATION_ALREADY_ACTIVE);
        }
        // 1. 경로 조회 (TMAP API 호출)
        RouteResponse route = mapService.getPedestrianRoute(
            request.getStartLocation().latitude(),
            request.getStartLocation().longitude(),
            request.getEndLocation().latitude(),
            request.getEndLocation().longitude(),
            request.getStartLocation().name(),
            request.getEndLocation().name()
        );

        // 2. 시간 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectedArrival = now.plusSeconds(route.totalTime());
        LocalDateTime safetyDeadline = expectedArrival.plusMinutes(30);

        // 3. DB 로그 생성 (ID 생성)
        NavigationLog navigationLog = logService.createLog(
            request.getPatientId(),
            route,
            now,
            request.getInitiatedBy()
        );

        // 4. navigationId 생성
        String navigationId = navigationLog.getId().toString();

        // 5. Redis에 세션 저장 (patientId를 키로 사용)
        redisService.saveNavigationSession(request.getPatientId(), navigationId, route);

        log.info("길안내 시작 완료: navigationId={}", navigationId);

        // 6. 응답 생성
        return NavigationSessionResponse.of(
            navigationId,
            route,
            now,
            expectedArrival,
            safetyDeadline,
            request.getInitiatedBy()
        );
    }
}