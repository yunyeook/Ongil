package kr.co.ongil.domain.patient.abnormal.service;

import kr.co.ongil.domain.notification.dto.request.NotificationRequest;
import kr.co.ongil.domain.notification.entity.NotificationType;
import kr.co.ongil.domain.notification.service.NotificationService;
import kr.co.ongil.domain.patient.abnormal.dto.request.AbnormalCreateRequest;
import kr.co.ongil.domain.patient.abnormal.dto.request.AbnormalSearchRequest;
import kr.co.ongil.domain.patient.abnormal.dto.response.AbnormalListResponse;
import kr.co.ongil.domain.patient.abnormal.dto.response.AbnormalResponse;
import kr.co.ongil.domain.patient.abnormal.entity.Abnormal;
import kr.co.ongil.domain.patient.abnormal.repository.AbnormalRepository;
import kr.co.ongil.domain.patient.safezone.entity.SafeZoneLevel;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.util.PatientAccessValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AbnormalService {

    private final AbnormalRepository abnormalRepository;
    private final UserRepository userRepository;
    private final PatientAccessValidator patientAccessValidator;
    private final NotificationService notificationService;
    private final RelationshipRepository relationshipRepository;

    /**
     * 환자의 이상탐지 이벤트 목록 조회
     */
    public AbnormalListResponse getAbnormals(
        Integer senderId,
        Integer patientId,
        AbnormalSearchRequest searchRequest
    ) {
        // 환자 존재 여부 확인
        userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 접근 권한 확인
        patientAccessValidator.validateAccess( patientId,senderId);

        // 페이지 생성
        Pageable pageable = PageRequest.of(searchRequest.page() - 1, searchRequest.size());

        // 조회
        Page<Abnormal> abnormalsPage = abnormalRepository.findAbnormalsByPatientAndFilters(
            patientId,
            searchRequest.getAbnormalType(),
            searchRequest.getSafeZoneLevel(),
            searchRequest.getFromDateTime(),
            searchRequest.getToDateTime(),
            pageable
        );

        // DTO 변환
        Page<AbnormalResponse> responsePage = abnormalsPage.map(AbnormalResponse::from);

        // 목록 응답 생성
        return AbnormalListResponse.of(responsePage);
    }

    /**
     * 이상탐지 이벤트 상세 조회
     */
    public AbnormalResponse getAbnormalDetail(
        Integer senderId,
        Integer patientId,
        Integer abnormalId
    ) {
        // 환자 존재 여부 확인
        userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 접근 권한 확인
        patientAccessValidator.validateAccess( patientId,senderId);

        // 이상탐지 이벤트 조회
        Abnormal abnormal = abnormalRepository.findByIdAndPatientId(abnormalId, patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ABNORMAL_NOT_FOUND));

        return AbnormalResponse.from(abnormal);
    }

    /**
     * 이상탐지 이벤트 등록
     */
    @Transactional
    public AbnormalResponse createAbnormal(
        Integer senderId,
        Integer patientId,
        AbnormalCreateRequest request
    ) {
        // 접근 권한 확인
        patientAccessValidator.validateAccess( patientId,senderId);

        // 환자 조회
        User patient = userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 엔티티 생성 및 저장
        Abnormal abnormal = Abnormal.from(request, patient);
        abnormalRepository.save(abnormal);

        // 보호자 알림 전송
        sendAbnormalNotificationToAllGuardians(patient, abnormal);

        log.info("이상탐지 이벤트 등록 완료 - patientId: {}, type: {}, abnormalId: {}",
            patientId, request.abnormalType(), abnormal.getId());

        return AbnormalResponse.from(abnormal);
    }

    /**
     * 환자와 관계된 모든 보호자에게 이상탐지 알림 전송
     */
    private void sendAbnormalNotificationToAllGuardians(User patient, Abnormal abnormal) {
        try {
            List<User> guardians = relationshipRepository.findGuardiansByPatientId(patient.getId());

            if (guardians.isEmpty()) {
                log.warn("이상탐지 알림 전송 대상 없음 - patientId: {}", patient.getId());
                return;
            }

            for (User guardian : guardians) {
                try {
                    NotificationRequest notificationRequest = NotificationRequest.of(
                        abnormal.getAbnormalType().getDescription(),
                        createNotificationContent(patient, abnormal),
                        getNotificationTypeByAbnormal(abnormal),
                        patient.getId(),
                        guardian.getId()
                    );

                    notificationService.createNotifications(notificationRequest, abnormal.getId());
                    log.info("이상탐지 알림 전송 완료 - patientId: {}, guardianId: {}, abnormalId: {}",
                        patient.getId(), guardian.getId(), abnormal.getId());

                } catch (Exception e) {
                    log.error("보호자 알림 전송 실패 - guardianId: {}", guardian.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("이상탐지 알림 전송 중 오류 발생 - patientId: {}", patient.getId(), e);
        }
    }

    /**
     * 이상탐지 유형에 따른 알림 내용 생성
     */
    private String createNotificationContent(User patient, Abnormal abnormal) {
        String patientName = patient.getName();

        return switch (abnormal.getAbnormalType()) {
            case SAFEZONE_EXIT -> String.format("%s님이 %s 안전범위를 벗어났습니다.",
                patientName,
                getSafeZoneLevelText(abnormal.getSafeZoneLevel()));
            case WANDER -> String.format("%s님이 %s 안전범위에서 %d분 이상 벗어나 배회 중입니다.",
                patientName,
                getSafeZoneLevelText(abnormal.getSafeZoneLevel()),
                (int) Math.ceil(abnormal.getElapsedTime() / 60.0));
            case DEVIATE_FROM_THE_PATH -> String.format("%s님이 길안내 중 경로를 이탈했습니다.",
                patientName);
        };
    }

    /**
     * SafeZoneLevel을 텍스트로 변환
     */
    private String getSafeZoneLevelText(SafeZoneLevel level) {
        if (level == null) return "";
        return switch (level) {
            case FIRST -> "1단계";
            case SECOND -> "2단계";
            case THIRD -> "3단계";
        };
    }

    /**
     * 이상탐지 유형에 따른 NotificationType 반환
     */
    private NotificationType getNotificationTypeByAbnormal(Abnormal abnormal) {
        return switch (abnormal.getAbnormalType()) {
            case SAFEZONE_EXIT -> NotificationType.SAFEZONE_EXIT;
            case WANDER -> NotificationType.WANDER;
            case DEVIATE_FROM_THE_PATH -> NotificationType.DEVIATE_FROM_THE_PATH;
        };
    }
}
