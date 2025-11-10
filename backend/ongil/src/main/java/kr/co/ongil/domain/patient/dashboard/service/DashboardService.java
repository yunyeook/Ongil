package kr.co.ongil.domain.patient.dashboard.service;

import jakarta.transaction.Transactional;
import kr.co.ongil.domain.call.entity.CallType;
import kr.co.ongil.domain.call.repository.CallLogRepository;
import kr.co.ongil.domain.patient.abnormal.repository.AbnormalRepository;
import kr.co.ongil.domain.patient.dashboard.dto.AbnormalStatisticsDto;
import kr.co.ongil.domain.patient.dashboard.dto.CallStatisticsDto;
import kr.co.ongil.domain.patient.dashboard.dto.DashboardResponseDto;
import kr.co.ongil.domain.patient.dashboard.entity.DashboardCalc;
import kr.co.ongil.domain.patient.dashboard.repository.DashboardRepository;
import kr.co.ongil.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    
    public DashboardResponseDto getDashboardResponseDto(Integer patientId) {
        Optional<DashboardCalc> dashboardCalc = dashboardRepository.findById(patientId);
        if(dashboardCalc.isPresent()) return DashboardResponseDto.from(dashboardCalc.get());
        else return null;
    }

    @Transactional
    public void saveDashboards() {
        LocalDate startDate = LocalDate.now().minusWeeks(1);

        List<AbnormalStatisticsDto> abnormalStats =
                abnormalRepository.getStatistics(startDate);
        List<CallStatisticsDto> callStats =
                callLogRepository.findCallStatisticsByUser(startDate, CallType.EMERGENCY);

        // Map 두 개 생성
        Map<Integer, AbnormalStatisticsDto> abnormalMap = abnormalStats.stream()
                .collect(Collectors.toMap(
                        AbnormalStatisticsDto::getPatientId,
                        Function.identity()
                ));

        Map<Integer, CallStatisticsDto> callMap = callStats.stream()
                .collect(Collectors.toMap(
                        CallStatisticsDto::getPatientId,
                        Function.identity()
                ));

        // 모든 patient ID
        Set<Integer> allPatientIds = new HashSet<>();
        allPatientIds.addAll(abnormalMap.keySet());
        allPatientIds.addAll(callMap.keySet());

        // 합쳐서 저장
        List<DashboardCalc> dashboards = allPatientIds.stream()
                .map(patientId -> {
                    AbnormalStatisticsDto abnormal = abnormalMap.get(patientId);
                    CallStatisticsDto call = callMap.get(patientId);

                    return DashboardCalc.builder()
                            .patient(userRepository.getReferenceById(patientId))  // 프록시만 생성
                            .routeLost(abnormal != null ? abnormal.getPathCount() : 0)
                            .safezoneEmer(abnormal != null ? abnormal.getWanderCount() : 0)
                            .emerCall(call != null ? call.getCallCount() : 0)
                            .sosSign(0)
                            .safezoneExit(abnormal != null ? abnormal.getSafezoneExitByLevel() : null)
                            .build();
                })
                .collect(Collectors.toList());

        dashboardRepository.saveAll(dashboards);
    }
}
