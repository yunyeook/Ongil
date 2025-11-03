package kr.co.ongil.domain.navigation.service;

import kr.co.ongil.domain.map.dto.response.RouteResponse;
import kr.co.ongil.domain.map.service.MapService;
import kr.co.ongil.domain.navigation.dto.request.EndNavigationRequest;
import kr.co.ongil.domain.navigation.dto.request.StartNavigationRequest;
import kr.co.ongil.domain.navigation.dto.response.EndNavigationResponse;
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
            request.patientId(), request.initiatedBy());

        if (redisService.hasActiveSession(request.patientId())) {
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
        NavigationLog navigationLog = logService.createLog(
            request.patientId(),
            route,
            now,
            request.initiatedBy()
        );

        // 4. navigationId 생성
        String navigationId = navigationLog.getId().toString();

        // 5. Redis에 세션 저장 (patientId를 키로 사용)
        redisService.saveNavigationSession(request.patientId(), navigationId, route);

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
    public EndNavigationResponse endNavigation(EndNavigationRequest request) {

        log.info("길안내 종료: patientId={}, navigationId={}",
            request.patientId(), request.navigationId());

        // 1. DB 로그 완료 처리
        NavigationLog completedLog = logService.completeLog(
            request.navigationId(),
            request.isSuccessful()
        );

        // 2. Redis 세션 삭제
        redisService.endSession(request.patientId());

        log.info("길안내 종료 완료: navigationId={}, isSuccessful={}",
            request.navigationId(), completedLog.getIsSuccessful());

        // 3. 응답
        return EndNavigationResponse.of(
            request.navigationId().toString(),
            completedLog.getStartedAt(),
            completedLog.getEndedAt(),
            completedLog.getIsSuccessful()
        );
    }

}