package kr.co.ongil.domain.patient.safezone.service;

import kr.co.ongil.domain.patient.safezone.dto.request.SafeZonePatchRequest;
import kr.co.ongil.domain.patient.safezone.dto.request.SafeZoneUpsertRequest;
import kr.co.ongil.domain.patient.safezone.dto.response.SafeZoneResponse;
import kr.co.ongil.domain.patient.safezone.entity.SafeZone;
import kr.co.ongil.domain.patient.safezone.repository.SafeZoneRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafezoneService {

    private final SafeZoneRepository safeZoneRepository;
    private final UserRepository userRepository;

    // 상수 정의
    private static final double FIRST_MIN = 50.0;
    private static final double FIRST_MAX = 150.0;
    private static final double SECOND_MIN_BASE = 200.0;
    private static final double SECOND_MAX = 500.0;
    private static final double THIRD_MIN_BASE = 550.0;
    private static final double THIRD_MAX = 1000.0;
    private static final double BOUNDARY_GAP = 50.0;

    /**
     * 안전범위 생성 또는 전체 교체 (Upsert)
     */
    @Transactional
    public SafeZoneResponse upsertSafeZone(Integer patientId, SafeZoneUpsertRequest request) {
        log.info("안전범위 생성/교체 요청: patientId={}", patientId);

        // 1. 환자 조회
        User patient = userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 유효성 검증
        validateBoundaries(request.firstBoundary(), request.secondBoundary(), request.thirdBoundary());

        // 3. 기존 안전범위 조회 또는 신규 생성
        SafeZone safeZone = safeZoneRepository.findByPatientId(patientId)
            .orElseGet(() -> SafeZone.builder()
                .patient(patient)
                .build());

        // 4. 값 업데이트
        safeZone.updateBoundaries(
            request.firstBoundary(),
            request.secondBoundary() != null ? request.secondBoundary() : 500.0,
            request.thirdBoundary() != null ? request.thirdBoundary() : 1000.0
        );

        safeZone.updateTimes(
            request.firstTime(),
            request.secondTime() != null ? request.secondTime() : 30,
            request.thirdTime() != null ? request.thirdTime() : 15
        );

        // 5. 저장
        SafeZone saved = safeZoneRepository.save(safeZone);
        log.info("안전범위 저장 완료: id={}", saved.getId());

        return SafeZoneResponse.from(saved);
    }

    /**
     * 안전범위 기본값으로 복원
     */
    @Transactional
    public SafeZoneResponse resetSafeZone(Integer patientId) {
        log.info("안전범위 기본값 복원 요청: patientId={}", patientId);

        // 1. 환자 조회
        User patient = userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 기존 안전범위 조회 또는 신규 생성
        SafeZone safeZone = safeZoneRepository.findByPatientId(patientId)
            .orElseGet(() -> SafeZone.builder()
                .patient(patient)
                .build());

        // 3. 기본값으로 리셋
        safeZone.resetToDefault();

        // 4. 저장
        SafeZone saved = safeZoneRepository.save(safeZone);
        log.info("안전범위 기본값 복원 완료: id={}", saved.getId());

        return SafeZoneResponse.from(saved);
    }

    /**
     * 안전범위 부분 수정
     */
    @Transactional
    public SafeZoneResponse patchSafeZone(Integer patientId, SafeZonePatchRequest request) {
        log.info("안전범위 부분 수정 요청: patientId={}", patientId);

        // 1. 기존 안전범위 조회
        SafeZone safeZone = safeZoneRepository.findByPatientId(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SAFEZONE_SETTING_NOT_FOUND));

        // 2. 수정할 값 결정
        Double newFirst = request.firstBoundary() != null ? request.firstBoundary() : safeZone.getFirstBoundary();
        Double newSecond = request.secondBoundary() != null ? request.secondBoundary() : safeZone.getSecondBoundary();
        Double newThird = request.thirdBoundary() != null ? request.thirdBoundary() : safeZone.getThirdBoundary();

        // 3. 유효성 검증
        validateBoundaries(newFirst, newSecond, newThird);

        // 4. 업데이트
        safeZone.updateBoundaries(
            request.firstBoundary(),
            request.secondBoundary(),
            request.thirdBoundary()
        );

        safeZone.updateTimes(
            request.firstTime(),
            request.secondTime(),
            request.thirdTime()
        );

        log.info("안전범위 부분 수정 완료: id={}", safeZone.getId());

        return SafeZoneResponse.from(safeZone);
    }

    /**
     * 안전범위 조회
     */
    @Transactional(readOnly = true)
    public SafeZoneResponse getSafeZone(Integer patientId) {
        log.info("안전범위 조회 요청: patientId={}", patientId);

        // 1. 환자 존재 확인
        if (!userRepository.existsById(patientId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 안전범위 조회 (없으면 기본값 반환)
        SafeZone safeZone = safeZoneRepository.findByPatientId(patientId)
            .orElseGet(() -> {
                User patient = userRepository.findById(patientId).get();
                return SafeZone.builder()
                    .patient(patient)
                    .build();
            });

        return SafeZoneResponse.from(safeZone);
    }

    /**
     * 안전범위 경계값 유효성 검증 (동적 검증)
     */
    private void validateBoundaries(Double first, Double second, Double third) {
        // 1단계 검증
        if (first < FIRST_MIN || first > FIRST_MAX) {
            throw new BusinessException(ErrorCode.SAFEZONE_BOUNDARY_OUT_OF_RANGE);
        }

        // 2단계 검증 (동적 최소값: 1단계 + 50)
        double secondMin = Math.max(SECOND_MIN_BASE, first + BOUNDARY_GAP);
        if (second < secondMin || second > SECOND_MAX) {
            throw new BusinessException(ErrorCode.SAFEZONE_BOUNDARY_OUT_OF_RANGE);
        }

        // 3단계 검증 (동적 최소값: 2단계 + 50)
        double thirdMin = Math.max(THIRD_MIN_BASE, second + BOUNDARY_GAP);
        if (third < thirdMin || third > THIRD_MAX) {
            throw new BusinessException(ErrorCode.SAFEZONE_BOUNDARY_OUT_OF_RANGE);
        }
    }
}
