package kr.co.ongil.domain.patient.health.service;

import kr.co.ongil.domain.patient.health.dto.request.HealthDataUploadRequest;
import kr.co.ongil.domain.patient.health.dto.response.HealthDataListResponse;
import kr.co.ongil.domain.patient.health.dto.response.HealthDataRecordResponse;
import kr.co.ongil.domain.patient.health.dto.response.HealthDataSummaryItemResponse;
import kr.co.ongil.domain.patient.health.dto.response.HealthDataSummaryResponse;
import kr.co.ongil.domain.patient.health.entity.HealthData;
import kr.co.ongil.domain.patient.health.entity.HealthDataType;
import kr.co.ongil.domain.patient.health.repository.HealthDataRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.util.PatientAccessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 건강 데이터 서비스
 * Samsung Health SDK에서 수집한 생체 데이터 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthDataService {

    private final HealthDataRepository healthDataRepository;
    private final PatientAccessValidator patientAccessValidator;

    /**
     * 생체 데이터 업로드
     * Samsung Health에서 수집한 데이터를 일괄 저장
     *
     * @param patientId 환자 ID
     * @param request 업로드 요청 (여러 개의 건강 데이터)
     * @param callerId 호출자 ID (권한 검증용)
     * @return 저장된 데이터 개수
     */
    @Transactional
    public Integer uploadHealthData(Integer patientId, HealthDataUploadRequest request, Integer callerId) {
        log.info("생체 데이터 업로드 요청: patientId={}, recordCount={}", patientId, request.records().size());

        // 1. 권한 검증 (본인 또는 보호자)
        patientAccessValidator.validateAccess(patientId, callerId);

        // 2. 날짜 범위 검증
        validateDateRange(request);

        // 3. Entity 변환 및 저장
        List<HealthData> entities = request.records().stream()
            .map(r -> HealthData.builder()
                .patientId(patientId)
                .type(r.type())
                .average(r.average())
                .max(r.max())
                .min(r.min())
                .unit(r.unit())
                .measuredAt(r.measuredAt())
                .build()
            )
            .toList();

        List<HealthData> savedEntities = healthDataRepository.saveAll(entities);
        log.info("생체 데이터 {}개 저장 완료", savedEntities.size());

        return savedEntities.size();
    }

    /**
     * 생체 데이터 조회
     *
     * @param patientId 환자 ID
     * @param type 데이터 타입 (null이면 전체)
     * @param from 시작 날짜 (null이면 1일 전)
     * @param to 종료 날짜 (null이면 현재)
     * @param sort 정렬 (null이면 measuredAt desc)
     * @param callerId 호출자 ID
     * @return 건강 데이터 목록
     */
    public HealthDataListResponse getHealthData(
        Integer patientId,
        HealthDataType type,
        LocalDate from,
        LocalDate to,
        Sort sort,
        Integer callerId
    ) {
        log.info("생체 데이터 조회: patientId={}, type={}, from={}, to={}", patientId, type, from, to);

        // 1. 권한 검증
        patientAccessValidator.validateAccess(patientId, callerId);

        // 2. 날짜 범위 기본값 설정
        LocalDateTime fromDt = (from != null)
            ? from.atStartOfDay()
            : LocalDate.now().minusDays(1).atStartOfDay();

        LocalDateTime toDt = (to != null)
            ? to.plusDays(1).atStartOfDay()
            : LocalDateTime.now();

        // 3. 날짜 범위 검증
        if (fromDt.isAfter(toDt)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 4. 정렬 기본값 설정
        if (sort == null) {
            sort = Sort.by(Sort.Direction.DESC, "measuredAt");
        }

        // 5. 데이터 조회
        List<HealthData> entities = (type != null)
            ? healthDataRepository.findByPatientIdAndTypeAndMeasuredAtBetween(
                patientId, type, fromDt, toDt, sort)
            : healthDataRepository.findByPatientIdAndMeasuredAtBetween(
                patientId, fromDt, toDt, sort);

        // 6. Response 변환
        List<HealthDataRecordResponse> records = entities.stream()
            .map(HealthDataRecordResponse::from)
            .toList();

        log.info("생체 데이터 {}개 조회 완료", records.size());

        return HealthDataListResponse.of(patientId, type, records);
    }

    /**
     * 생체 데이터 요약 통계 조회
     * 일별로 그룹핑하여 평균/최대/최소/개수 계산
     *
     * @param patientId 환자 ID
     * @param type 데이터 타입 (null이면 전체)
     * @param from 시작 날짜 (null이면 7일 전)
     * @param to 종료 날짜 (null이면 현재)
     * @param callerId 호출자 ID
     * @return 일별 요약 통계
     */
    public HealthDataSummaryResponse getHealthDataSummary(
        Integer patientId,
        HealthDataType type,
        LocalDate from,
        LocalDate to,
        Integer callerId
    ) {
        log.info("생체 데이터 요약 통계 조회: patientId={}, type={}, from={}, to={}", patientId, type, from, to);

        // 1. 권한 검증
        patientAccessValidator.validateAccess(patientId, callerId);

        // 2. 날짜 범위 기본값 설정
        LocalDateTime fromDt = (from != null)
            ? from.atStartOfDay()
            : LocalDate.now().minusDays(7).atStartOfDay();

        LocalDateTime toDt = (to != null)
            ? to.plusDays(1).atStartOfDay()
            : LocalDate.now().plusDays(1).atStartOfDay();

        // 3. 날짜 범위 검증
        if (fromDt.isAfter(toDt)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 4. 데이터 조회
        List<HealthData> entities = (type != null)
            ? healthDataRepository.findByPatientIdAndTypeAndMeasuredAtBetween(
                patientId, type, fromDt, toDt, Sort.by(Sort.Direction.ASC, "measuredAt"))
            : healthDataRepository.findByPatientIdAndMeasuredAtBetween(
                patientId, fromDt, toDt, Sort.by(Sort.Direction.ASC, "measuredAt"));

        // 5. 데이터가 없는 경우
        if (entities.isEmpty()) {
            return HealthDataSummaryResponse.of(patientId, type, null, List.of());
        }

        // 6. 날짜별로 그룹핑
        Map<LocalDate, List<HealthData>> byDate = entities.stream()
            .collect(Collectors.groupingBy(
                e -> e.getMeasuredAt().toLocalDate(),
                TreeMap::new,
                Collectors.toList()
            ));

        // 7. 일별 통계 계산
        List<HealthDataSummaryItemResponse> summaryItems = byDate.entrySet().stream()
            .map(entry -> {
                LocalDate date = entry.getKey();
                List<HealthData> dayData = entry.getValue();

                double avg = dayData.stream()
                    .mapToDouble(HealthData::getAverage)
                    .average()
                    .orElse(0);

                double max = dayData.stream()
                    .mapToDouble(HealthData::getMax)
                    .max()
                    .orElse(0);

                double min = dayData.stream()
                    .mapToDouble(HealthData::getMin)
                    .min()
                    .orElse(0);

                long count = dayData.size();

                return HealthDataSummaryItemResponse.of(date, avg, max, min, count);
            })
            .toList();

        // 8. 단위 추출 (첫 번째 데이터의 단위 사용)
        String unit = entities.get(0).getUnit();

        log.info("생체 데이터 요약 통계 {}일 분량 조회 완료", summaryItems.size());

        return HealthDataSummaryResponse.of(patientId, type, unit, summaryItems);
    }

    /**
     * 생체 데이터 삭제
     *
     * @param patientId 환자 ID
     * @param healthDataId 건강 데이터 ID
     * @param callerId 호출자 ID
     */
    @Transactional
    public void deleteHealthData(Integer patientId, Integer healthDataId, Integer callerId) {
        log.info("생체 데이터 삭제 요청: patientId={}, healthDataId={}", patientId, healthDataId);

        // 1. 권한 검증
        patientAccessValidator.validateAccess(patientId, callerId);

        // 2. 데이터 존재 확인
        HealthData entity = healthDataRepository.findById(healthDataId)
            .orElseThrow(() -> new BusinessException(ErrorCode.HEALTH_DATA_NOT_FOUND));

        // 3. 환자 ID 일치 확인
        if (!entity.getPatientId().equals(patientId)) {
            throw new BusinessException(ErrorCode.HEALTH_DATA_ACCESS_DENIED);
        }

        // 4. 삭제
        healthDataRepository.delete(entity);
        log.info("생체 데이터 삭제 완료: healthDataId={}", healthDataId);
    }

    /**
     * 날짜 범위 검증
     */
    private void validateDateRange(HealthDataUploadRequest request) {
        request.records().forEach(record -> {
            if (record.measuredAt().isAfter(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
            }
        });
    }
}
