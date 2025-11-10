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
import kr.co.ongil.global.util.PatientAccessValidator;
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
    private final PatientAccessValidator patientAccessValidator;

    /**
     * 안전범위 생성 또는 전체 교체 (Upsert)
     */
    @Transactional
    public SafeZoneResponse upsertSafeZone(Integer patientId, SafeZoneUpsertRequest request, Integer callerId) {
        log.info("안전범위 생성/교체 요청: patientId={}, callerId={}", patientId, callerId);

        // 1. 환자 조회
        User patient = userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 권한 검증 (본인 또는 보호자)
        validateAccess(patientId, callerId);

        // 3. 기존 안전범위 조회 또는 신규 생성
        SafeZone safeZone = safeZoneRepository.findByPatientId(patientId)
            .orElseGet(() -> SafeZone.builder()
                .patient(patient)
                .build());

        // 4. 값 업데이트
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

        // 6. 저장
        SafeZone saved = safeZoneRepository.save(safeZone);
        log.info("안전범위 저장 완료: id={}", saved.getId());

        return SafeZoneResponse.from(saved);
    }

    /**
     * 안전범위 기본값으로 복원
     */
    @Transactional
    public SafeZoneResponse resetSafeZone(Integer patientId, Integer callerId) {
        log.info("안전범위 기본값 복원 요청: patientId={}, callerId={}", patientId, callerId);

        // 1. 환자 조회
        User patient = userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 권한 검증 (본인 또는 보호자)
        validateAccess(patientId, callerId);

        // 3. 기존 안전범위 조회 또는 신규 생성
        SafeZone safeZone = safeZoneRepository.findByPatientId(patientId)
            .orElseGet(() -> SafeZone.builder()
                .patient(patient)
                .build());

        // 4. 기본값으로 리셋
        safeZone.resetToDefault();

        // 5. 저장
        SafeZone saved = safeZoneRepository.save(safeZone);
        log.info("안전범위 기본값 복원 완료: id={}", saved.getId());

        return SafeZoneResponse.from(saved);
    }

    /**
     * 안전범위 부분 수정
     */
    @Transactional
    public SafeZoneResponse patchSafeZone(Integer patientId, SafeZonePatchRequest request, Integer callerId) {
        log.info("안전범위 부분 수정 요청: patientId={}, callerId={}", patientId, callerId);

        // 1. 권한 검증 (본인 또는 보호자)
        validateAccess(patientId, callerId);

        // 2. 기존 안전범위 조회
        SafeZone safeZone = safeZoneRepository.findByPatientId(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SAFEZONE_SETTING_NOT_FOUND));

        // 3. 업데이트
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
    public SafeZoneResponse getSafeZone(Integer patientId, Integer callerId) {
        log.info("안전범위 조회 요청: patientId={}, callerId={}", patientId, callerId);

        // 1. 환자 존재 확인
        if (!userRepository.existsById(patientId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 권한 검증 (본인 또는 보호자)
        validateAccess(patientId, callerId);

        // 3. 안전범위 조회 (없으면 기본값 반환)
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
     * 권한 검증 (본인 또는 보호자)
     */
    private void validateAccess(Integer patientId, Integer callerId) {
        patientAccessValidator.validateAccess(patientId, callerId, ErrorCode.SAFEZONE_ACCESS_DENIED);
    }
}
