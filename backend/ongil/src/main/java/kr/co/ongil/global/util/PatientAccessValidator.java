package kr.co.ongil.global.util;

import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 환자 관련 리소스 접근 권한 검증 유틸리티
 *
 * 환자의 개인정보나 설정에 접근할 때 본인 또는 연결된 보호자만 접근 가능하도록 검증합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PatientAccessValidator {

    private final RelationshipRepository relationshipRepository;

    /**
     * 환자 리소스 접근 권한 검증
     *
     * @param patientId 환자 ID
     * @param callerId 요청자 ID (본인 또는 보호자)
     * @param errorCode 권한이 없을 경우 발생시킬 ErrorCode
     * @throws BusinessException 권한이 없을 경우
     */
    public void validateAccess(Integer patientId, Integer callerId, ErrorCode errorCode) {
        log.debug("환자 리소스 접근 권한 검증 - patientId: {}, callerId: {}", patientId, callerId);

        // 1. 본인인 경우 허용
        if (patientId.equals(callerId)) {
            log.debug("본인 접근 - 권한 허용");
            return;
        }

        // 2. 보호자 관계 확인
        boolean isGuardian = relationshipRepository.existsByPatientIdAndGuardianId(patientId, callerId);
        if (isGuardian) {
            log.debug("보호자 접근 - 권한 허용 (guardianId: {})", callerId);
            return;
        }

        // 3. 권한 없음
        log.warn("접근 권한 없음 - patientId: {}, callerId: {}", patientId, callerId);
        throw new BusinessException(errorCode);
    }

    /**
     * 환자 리소스 접근 권한 검증 (기본 에러 코드 사용)
     *
     * @param patientId 환자 ID
     * @param callerId 요청자 ID
     * @throws BusinessException 권한이 없을 경우 ACCESS_DENIED 발생
     */
    public void validateAccess(Integer patientId, Integer callerId) {
        validateAccess(patientId, callerId, ErrorCode.ACCESS_DENIED);
    }

    /**
     * 환자 리소스 접근 가능 여부 확인 (예외 발생 안 함)
     *
     * @param patientId 환자 ID
     * @param callerId 요청자 ID
     * @return 접근 가능하면 true, 아니면 false
     */
    public boolean hasAccess(Integer patientId, Integer callerId) {
        if (patientId.equals(callerId)) {
            return true;
        }
        return relationshipRepository.existsByPatientIdAndGuardianId(patientId, callerId);
    }
}
