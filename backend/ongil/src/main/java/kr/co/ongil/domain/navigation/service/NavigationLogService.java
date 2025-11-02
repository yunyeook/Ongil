package kr.co.ongil.domain.navigation.service;

import kr.co.ongil.domain.map.dto.response.RouteResponse;
import kr.co.ongil.domain.navigation.entity.InitiatedBy;
import kr.co.ongil.domain.navigation.entity.NavigationLog;
import kr.co.ongil.domain.navigation.repository.NavigationLogRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NavigationLogService {

    private final NavigationLogRepository logRepository;

    /**
     * 길안내 로그 생성
     */
    @Transactional
    public NavigationLog createLog(Long patientId, RouteResponse route, LocalDateTime startedAt, String initiatedBy
    ) {
        NavigationLog navigationLog = NavigationLog.of(patientId, route, startedAt, initiatedBy);

        NavigationLog saved = logRepository.save(navigationLog);
        log.info("길안내 로그 생성: id={}", saved.getId());
        return saved;
    }

    /**
     * 로그 완료 처리
     */
    @Transactional
    public NavigationLog completeLog(Long navigationId, Boolean isSuccessful) {
        NavigationLog navigationLog = logRepository.findById(navigationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NAVIGATION_LOG_NOT_FOUND));

        navigationLog.complete(LocalDateTime.now(), isSuccessful);

        log.info("길안내 로그 완료: navigationId={}, isSuccessful={}", navigationId, isSuccessful);
        return navigationLog;
    }
}