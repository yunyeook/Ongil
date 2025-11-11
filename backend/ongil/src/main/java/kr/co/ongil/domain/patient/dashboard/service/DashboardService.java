package kr.co.ongil.domain.patient.dashboard.service;

import jakarta.transaction.Transactional;
import kr.co.ongil.domain.call.entity.CallType;
import kr.co.ongil.domain.call.repository.CallLogRepository;
import kr.co.ongil.domain.patient.abnormal.repository.AbnormalRepository;
import kr.co.ongil.domain.patient.dashboard.dto.AbnormalStatisticsDto;
import kr.co.ongil.domain.patient.dashboard.dto.CallStatisticsDto;
import kr.co.ongil.domain.patient.dashboard.dto.DashboardResponseDto;
import kr.co.ongil.domain.patient.dashboard.dto.FavoriteStatisticsDto;
import kr.co.ongil.domain.patient.dashboard.entity.DashboardCalc;
import kr.co.ongil.domain.patient.dashboard.repository.DashboardRepository;
import kr.co.ongil.domain.patient.favorite.repository.FavoriteRepository;
import kr.co.ongil.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final AbnormalRepository abnormalRepository;
    private final CallLogRepository callLogRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    
    public DashboardResponseDto getDashboardResponseDto(Integer patientId) {
        Optional<DashboardCalc> dashboardCalc = dashboardRepository.findByPatientId(patientId);
        if(dashboardCalc.isPresent()) return DashboardResponseDto.from(dashboardCalc.get());
        else return null;
    }

    @Transactional
    public void saveDashboards() {
        LocalDateTime startDate = LocalDate.now().minusDays(7).atStartOfDay();

        List<AbnormalStatisticsDto> abnormalStats =
                abnormalRepository.getStatistics(startDate);
        List<CallStatisticsDto> callStats =
                callLogRepository.findCallStatisticsByUser(startDate, CallType.EMERGENCY);
        List<FavoriteStatisticsDto> favoriteStats=
                favoriteRepository.getFavoriteStatistics(startDate);

        // Map 두 개 생성
        Map<Long, AbnormalStatisticsDto> abnormalMap = abnormalStats.stream()
                .collect(Collectors.toMap(
                        AbnormalStatisticsDto::getPatientId,
                        Function.identity()
                ));

        Map<Long, CallStatisticsDto> callMap = callStats.stream()
                .collect(Collectors.toMap(
                        CallStatisticsDto::getPatientId,
                        Function.identity()
                ));

        Map<Long, FavoriteStatisticsDto> favMap = favoriteStats.stream()
                .collect(Collectors.toMap(
                        FavoriteStatisticsDto::getPatientId,
                        Function.identity()
                ));

        // 모든 patient ID
        Set<Long> allPatientIds = new HashSet<>();
        allPatientIds.addAll(abnormalMap.keySet());
        allPatientIds.addAll(callMap.keySet());

        // 합쳐서 저장
        List<DashboardCalc> dashboards = allPatientIds.stream()
                .map(patientId -> {
                    AbnormalStatisticsDto abnormal = abnormalMap.get(patientId);
                    CallStatisticsDto call = callMap.get(patientId);
                    FavoriteStatisticsDto favorite = favMap.get(patientId);
                    return DashboardCalc.builder()
                            .patient(userRepository.getReferenceById(Math.toIntExact(patientId)))  // 프록시만 생성
                            .routeLost(abnormal != null ? abnormal.getPathCount() : 0)
                            .safezoneEmer(abnormal != null ? abnormal.getWanderCount() : 0)
                            .emerCall(call != null ? call.getCallCount() : 0)
                            .sosSign(0L)
                            .favorite(favorite != null ? favorite.getFavorites() : null)
                            .safezoneExit(abnormal != null ? abnormal.getSafezoneExitByLevel() : null)
                            .build();
                })
                .collect(Collectors.toList());

        dashboardRepository.saveAll(dashboards);
    }
}
