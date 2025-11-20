package kr.co.ongil.domain.patient.insight.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.ongil.domain.patient.abnormal.entity.Abnormal;
import kr.co.ongil.domain.patient.abnormal.entity.AbnormalType;
import kr.co.ongil.domain.patient.abnormal.repository.AbnormalRepository;
import kr.co.ongil.domain.patient.dashboard.entity.DashboardCalc;
import kr.co.ongil.domain.patient.dashboard.repository.DashboardRepository;
import kr.co.ongil.domain.patient.health.entity.HealthData;
import kr.co.ongil.domain.patient.health.entity.HealthDataType;
import kr.co.ongil.domain.patient.health.repository.HealthDataRepository;
import kr.co.ongil.domain.patient.insight.dto.internal.ActivityStats;
import kr.co.ongil.domain.patient.insight.dto.internal.HealthStats;
import kr.co.ongil.domain.patient.insight.dto.internal.PeriodInfo;
import kr.co.ongil.domain.patient.sos.entity.Sos;
import kr.co.ongil.domain.patient.sos.repository.SosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 환자 인사이트 데이터 집계 서비스
 * DashboardCalc, Abnormal, Sos, HealthData에서 데이터를 수집하여 ActivityStats와 HealthStats로 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsightAggregatorService {

    private final DashboardRepository dashboardRepository;
    private final AbnormalRepository abnormalRepository;
    private final SosRepository sosRepository;
    private final HealthDataRepository healthDataRepository;
    private final ObjectMapper objectMapper;

    /**
     * 특정 기간의 활동 통계 집계
     */
    @Transactional(readOnly = true)
    public ActivityStats aggregateActivityStats(Integer patientId, PeriodInfo periodInfo) {
        log.info("활동 통계 집계 시작 - patientId: {}, periodType: {}, current: {} ~ {}",
            patientId, periodInfo.periodType(), periodInfo.currentStart(), periodInfo.currentEnd());

        // 1. DashboardCalc에서 현재 기간 데이터 조회
        DashboardCalc currentDashboard = getLatestDashboardInPeriod(patientId, periodInfo.currentStart(), periodInfo.currentEnd());
        DashboardCalc previousDashboard = getLatestDashboardInPeriod(patientId, periodInfo.previousStart(), periodInfo.previousEnd());

        // 2. Abnormal 데이터 조회
        List<Abnormal> currentAbnormals = getAbnormalsInPeriod(patientId, periodInfo.currentStart(), periodInfo.currentEnd());
        List<Abnormal> previousAbnormals = getAbnormalsInPeriod(patientId, periodInfo.previousStart(), periodInfo.previousEnd());

        // 3. Sos 데이터 조회
        List<Sos> currentSos = getSosInPeriod(patientId, periodInfo.currentStart(), periodInfo.currentEnd());
        List<Sos> previousSos = getSosInPeriod(patientId, periodInfo.previousStart(), periodInfo.previousEnd());

        // 4. 각 통계 계산
        ActivityStats.SafezoneExitStats safezoneExit = calculateSafezoneExitStats(
            currentDashboard, previousDashboard, currentAbnormals);

        ActivityStats.RouteStats route = calculateRouteStats(
            currentDashboard, previousDashboard);

        ActivityStats.WanderStats wander = calculateWanderStats(
            currentAbnormals, previousAbnormals);

        ActivityStats.EmergencyStats emergency = calculateEmergencyStats(
            currentDashboard, previousDashboard, currentSos, previousSos);

        ActivityStats.FavoriteStats favorite = calculateFavoriteStats(
            currentDashboard, previousDashboard);

        return ActivityStats.builder()
            .safezoneExit(safezoneExit)
            .route(route)
            .wander(wander)
            .emergency(emergency)
            .favorite(favorite)
            .build();
    }

    /**
     * 특정 기간의 건강 통계 집계
     */
    @Transactional(readOnly = true)
    public HealthStats aggregateHealthStats(Integer patientId, PeriodInfo periodInfo) {
        log.info("건강 통계 집계 시작 - patientId: {}, periodType: {}", patientId, periodInfo.periodType());

        // 현재 기간 건강 데이터
        List<HealthData> currentHealth = getHealthDataInPeriod(patientId, periodInfo.currentStart(), periodInfo.currentEnd());
        // 이전 기간 건강 데이터
        List<HealthData> previousHealth = getHealthDataInPeriod(patientId, periodInfo.previousStart(), periodInfo.previousEnd());

        // 타입별로 그룹화
        Map<HealthDataType, List<HealthData>> currentByType = currentHealth.stream()
            .collect(Collectors.groupingBy(HealthData::getType));
        Map<HealthDataType, List<HealthData>> previousByType = previousHealth.stream()
            .collect(Collectors.groupingBy(HealthData::getType));

        // 각 통계 계산
        HealthStats.SleepStats sleep = calculateSleepStats(
            currentByType.get(HealthDataType.SLEEP),
            previousByType.get(HealthDataType.SLEEP));

        HealthStats.StepStats steps = calculateStepStats(
            currentByType.get(HealthDataType.STEP_COUNT),
            previousByType.get(HealthDataType.STEP_COUNT));

        HealthStats.HeartRateStats heartRate = calculateHeartRateStats(
            currentByType.get(HealthDataType.HEART_RATE));

        HealthStats.OxygenStats oxygen = calculateOxygenStats(
            currentByType.get(HealthDataType.OXYGEN_SATURATION));

        HealthStats.DataAvailability availability = HealthStats.DataAvailability.builder()
            .sleep(currentByType.containsKey(HealthDataType.SLEEP) && !currentByType.get(HealthDataType.SLEEP).isEmpty())
            .steps(currentByType.containsKey(HealthDataType.STEP_COUNT) && !currentByType.get(HealthDataType.STEP_COUNT).isEmpty())
            .heartRate(currentByType.containsKey(HealthDataType.HEART_RATE) && !currentByType.get(HealthDataType.HEART_RATE).isEmpty())
            .oxygen(currentByType.containsKey(HealthDataType.OXYGEN_SATURATION) && !currentByType.get(HealthDataType.OXYGEN_SATURATION).isEmpty())
            .build();

        return HealthStats.builder()
            .sleep(sleep)
            .steps(steps)
            .heartRate(heartRate)
            .oxygenSaturation(oxygen)
            .dataAvailability(availability)
            .build();
    }

    // ==================== 내부 조회 메서드 ====================

    private DashboardCalc getLatestDashboardInPeriod(Integer patientId, LocalDate startDate, LocalDate endDate) {
        // DashboardCalc는 매일 갱신되므로, 최신 2개를 조회해서 기간 내의 것을 찾는다
        List<DashboardCalc> dashboards = dashboardRepository.findTop2ByPatientIdOrderByCreatedAtDesc(patientId);

        LocalDateTime periodStart = startDate.atStartOfDay();
        LocalDateTime periodEnd = endDate.atTime(LocalTime.MAX);

        return dashboards.stream()
            .filter(d -> !d.getCreatedAt().isBefore(periodStart) && !d.getCreatedAt().isAfter(periodEnd))
            .findFirst()
            .orElse(null);
    }

    private List<Abnormal> getAbnormalsInPeriod(Integer patientId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(LocalTime.MAX);

        return abnormalRepository.findAbnormalsByPatientAndFilters(
            patientId, null, null, from, to, org.springframework.data.domain.Pageable.unpaged())
            .getContent();
    }

    private List<Sos> getSosInPeriod(Integer patientId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(LocalTime.MAX);

        return sosRepository.findAll().stream()
            .filter(s -> s.getPatient().getId().equals(patientId))
            .filter(s -> !s.getCreatedAt().isBefore(from) && !s.getCreatedAt().isAfter(to))
            .toList();
    }

    private List<HealthData> getHealthDataInPeriod(Integer patientId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = endDate.atTime(LocalTime.MAX);

        return healthDataRepository.findByPatientIdAndMeasuredAtBetween(
            patientId, from, to, org.springframework.data.domain.Sort.by("measuredAt").descending());
    }

    // ==================== 활동 통계 계산 메서드 ====================

    private ActivityStats.SafezoneExitStats calculateSafezoneExitStats(
        DashboardCalc current, DashboardCalc previous, List<Abnormal> currentAbnormals) {

        int currentCount = current != null ? (current.getSafezoneEmer() != null ? current.getSafezoneEmer().intValue() : 0) : 0;
        int previousCount = previous != null ? (previous.getSafezoneEmer() != null ? previous.getSafezoneEmer().intValue() : 0) : 0;

        // 레벨별 통계
        Map<String, Integer> byLevel = new HashMap<>();
        currentAbnormals.stream()
            .filter(a -> a.getAbnormalType() == AbnormalType.SAFEZONE_EXIT)
            .filter(a -> a.getSafeZoneLevel() != null)
            .forEach(a -> byLevel.merge(a.getSafeZoneLevel().name(), 1, Integer::sum));

        // 시간대별 패턴
        List<ActivityStats.TimeSlotPattern> timePatterns = calculateTimeSlotPatterns(
            currentAbnormals.stream()
                .filter(a -> a.getAbnormalType() == AbnormalType.SAFEZONE_EXIT)
                .toList());

        return ActivityStats.SafezoneExitStats.builder()
            .current(currentCount)
            .previous(previousCount)
            .byLevel(byLevel)
            .timePatterns(timePatterns)
            .build();
    }

    private ActivityStats.RouteStats calculateRouteStats(DashboardCalc current, DashboardCalc previous) {
        int currentCount = current != null ? (current.getRouteLost() != null ? current.getRouteLost().intValue() : 0) : 0;
        int previousCount = previous != null ? (previous.getRouteLost() != null ? previous.getRouteLost().intValue() : 0) : 0;

        double changeRate = previousCount == 0 ? 0.0 :
            ((double) (currentCount - previousCount) / previousCount) * 100.0;

        return ActivityStats.RouteStats.builder()
            .current(currentCount)
            .previous(previousCount)
            .changeRate(Math.round(changeRate * 10.0) / 10.0)
            .build();
    }

    private ActivityStats.WanderStats calculateWanderStats(List<Abnormal> current, List<Abnormal> previous) {
        List<Abnormal> currentWander = current.stream()
            .filter(a -> a.getAbnormalType() == AbnormalType.WANDER)
            .toList();

        List<Abnormal> previousWander = previous.stream()
            .filter(a -> a.getAbnormalType() == AbnormalType.WANDER)
            .toList();

        double avgDuration = currentWander.stream()
            .filter(a -> a.getElapsedTime() != null)
            .mapToDouble(a -> a.getElapsedTime() / 60.0)
            .average()
            .orElse(0.0);

        int nightOccurrences = (int) currentWander.stream()
            .filter(a -> {
                int hour = a.getCreatedAt().getHour();
                return hour >= 22 || hour < 6;  // 밤 10시 ~ 새벽 6시
            })
            .count();

        return ActivityStats.WanderStats.builder()
            .current(currentWander.size())
            .previous(previousWander.size())
            .avgDurationMinutes(Math.round(avgDuration * 10.0) / 10.0)
            .nightOccurrences(nightOccurrences)
            .build();
    }

    private ActivityStats.EmergencyStats calculateEmergencyStats(
        DashboardCalc current, DashboardCalc previous, List<Sos> currentSos, List<Sos> previousSos) {

        int emerCallCurrent = current != null ? (current.getEmerCall() != null ? current.getEmerCall().intValue() : 0) : 0;
        int emerCallPrevious = previous != null ? (previous.getEmerCall() != null ? previous.getEmerCall().intValue() : 0) : 0;

        int sosSignCurrent = current != null ? (current.getSosSign() != null ? current.getSosSign().intValue() : 0) : 0;
        int sosSignPrevious = previous != null ? (previous.getSosSign() != null ? previous.getSosSign().intValue() : 0) : 0;

        int sosNotResponded = (int) currentSos.stream()
            .filter(s -> !s.getIsResponsed())
            .count();

        return ActivityStats.EmergencyStats.builder()
            .emerCallCurrent(emerCallCurrent)
            .emerCallPrevious(emerCallPrevious)
            .sosSignCurrent(sosSignCurrent)
            .sosSignPrevious(sosSignPrevious)
            .sosNotResponded(sosNotResponded)
            .build();
    }

    private ActivityStats.FavoriteStats calculateFavoriteStats(DashboardCalc current, DashboardCalc previous) {
        List<ActivityStats.PlaceFrequency> topCurrent = parseFavoriteJson(current);
        List<ActivityStats.PlaceFrequency> topPrevious = parseFavoriteJson(previous);

        // Diversity Index: 방문한 장소의 다양성 (0~1)
        double diversityIndex = topCurrent.isEmpty() ? 0.0 :
            Math.min(1.0, topCurrent.size() / 5.0);

        // 야간 외출 횟수는 favorite JSON에 없으므로 0으로 설정
        int nightOutingCount = 0;

        return ActivityStats.FavoriteStats.builder()
            .topCurrent(topCurrent)
            .topPrevious(topPrevious)
            .diversityIndex(Math.round(diversityIndex * 100.0) / 100.0)
            .nightOutingCount(nightOutingCount)
            .build();
    }

    private List<ActivityStats.PlaceFrequency> parseFavoriteJson(DashboardCalc dashboard) {
        if (dashboard == null || dashboard.getFavorite() == null) {
            return Collections.emptyList();
        }

        try {
            // JSON 파싱: {"place1": 5, "place2": 3, ...}
            Map<String, Integer> favoriteMap = objectMapper.readValue(
                dashboard.getFavorite(),
                new TypeReference<Map<String, Integer>>() {}
            );

            return favoriteMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> new ActivityStats.PlaceFrequency(e.getKey(), e.getValue()))
                .toList();
        } catch (Exception e) {
            log.warn("Favorite JSON 파싱 실패 - patientId: {}, error: {}", dashboard.getPatient().getId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<ActivityStats.TimeSlotPattern> calculateTimeSlotPatterns(List<Abnormal> abnormals) {
        if (abnormals.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Integer> slotCounts = new HashMap<>();
        abnormals.forEach(a -> {
            int hour = a.getCreatedAt().getHour();
            String slot = getTimeSlot(hour);
            slotCounts.merge(slot, 1, Integer::sum);
        });

        int total = abnormals.size();

        return slotCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(3)
            .map(e -> new ActivityStats.TimeSlotPattern(
                e.getKey(),
                e.getValue(),
                Math.round((double) e.getValue() / total * 1000.0) / 10.0
            ))
            .toList();
    }

    private String getTimeSlot(int hour) {
        if (hour >= 6 && hour < 12) return "아침(06-12시)";
        if (hour >= 12 && hour < 18) return "오후(12-18시)";
        if (hour >= 18 && hour < 22) return "저녁(18-22시)";
        return "밤(22-06시)";
    }

    // ==================== 건강 통계 계산 메서드 ====================

    private HealthStats.SleepStats calculateSleepStats(List<HealthData> current, List<HealthData> previous) {
        Double avgCurrent = calculateAverage(current);
        Double avgPrevious = calculateAverage(previous);

        String trend = calculateTrend(avgCurrent, avgPrevious, 0.5);

        return HealthStats.SleepStats.builder()
            .avgHoursCurrent(avgCurrent)
            .avgHoursPrevious(avgPrevious)
            .trend(trend)
            .build();
    }

    private HealthStats.StepStats calculateStepStats(List<HealthData> current, List<HealthData> previous) {
        Double avgCurrent = calculateAverage(current);
        Double avgPrevious = calculateAverage(previous);

        double changeRate = (avgPrevious == null || avgPrevious == 0.0) ? 0.0 :
            ((avgCurrent != null ? avgCurrent : 0.0) - avgPrevious) / avgPrevious * 100.0;

        String trend = calculateTrend(avgCurrent, avgPrevious, 500.0);

        return HealthStats.StepStats.builder()
            .avgStepsCurrent(avgCurrent)
            .avgStepsPrevious(avgPrevious)
            .trend(trend)
            .changeRate(Math.round(changeRate * 10.0) / 10.0)
            .build();
    }

    private HealthStats.HeartRateStats calculateHeartRateStats(List<HealthData> current) {
        if (current == null || current.isEmpty()) {
            return HealthStats.HeartRateStats.builder()
                .avgCurrent(null)
                .maxCurrent(null)
                .variabilityCurrent(null)
                .build();
        }

        Double avgCurrent = calculateAverage(current);
        Double maxCurrent = current.stream()
            .mapToDouble(HealthData::getMax)
            .max()
            .orElse(0.0);
        Double minCurrent = current.stream()
            .mapToDouble(HealthData::getMin)
            .min()
            .orElse(0.0);

        return HealthStats.HeartRateStats.builder()
            .avgCurrent(avgCurrent)
            .maxCurrent(maxCurrent)
            .variabilityCurrent(Math.round((maxCurrent - minCurrent) * 10.0) / 10.0)
            .build();
    }

    private HealthStats.OxygenStats calculateOxygenStats(List<HealthData> current) {
        Double avgCurrent = calculateAverage(current);

        return HealthStats.OxygenStats.builder()
            .avgCurrent(avgCurrent)
            .build();
    }

    private Double calculateAverage(List<HealthData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }

        return Math.round(dataList.stream()
            .mapToDouble(HealthData::getAverage)
            .average()
            .orElse(0.0) * 10.0) / 10.0;
    }

    private String calculateTrend(Double current, Double previous, double threshold) {
        if (current == null || previous == null) {
            return "UNKNOWN";
        }

        double diff = current - previous;
        if (Math.abs(diff) < threshold) {
            return "STABLE";
        }
        return diff > 0 ? "INCREASE" : "DECREASE";
    }
}
