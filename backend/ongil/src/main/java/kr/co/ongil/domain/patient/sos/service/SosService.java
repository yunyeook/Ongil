package kr.co.ongil.domain.patient.sos.service;

import kr.co.ongil.domain.fcm.service.FcmService;
import kr.co.ongil.domain.notification.dto.request.NotificationRequest;
import kr.co.ongil.domain.notification.entity.NotificationType;
import kr.co.ongil.domain.notification.service.NotificationService;
import kr.co.ongil.domain.patient.sos.dto.request.SosAckRequest;
import kr.co.ongil.domain.patient.sos.dto.response.SosResponse;
import kr.co.ongil.domain.patient.sos.entity.Sos;
import kr.co.ongil.domain.patient.sos.repository.SosRepository;
import kr.co.ongil.domain.relationship.repository.RelationshipRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.entity.UserType;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SosService {

    private final SosRepository sosRepository;
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final NotificationService notificationService;

    @Transactional
    public SosResponse createSosRequest(Integer guardianId, Integer patientId) {
        // 1. 보호자 조회
        User guardian = userRepository.findById(guardianId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 환자 조회
        User patient = userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        // 3. 관계등록 여부 조회
        if(relationshipRepository.existsByPatientIdAndGuardianId(patientId,guardianId)){

            // 3-1). SOS 로그 저장
            Sos sos = Sos.builder()
                .guardian(guardian)
                .patient(patient)
                .latitude(0.0) // TODO : 환자 현재 위치는 Redis에서 가져오기
                .longitude(0.0)
                .isResponsed(false)
                .build();

            Sos savedSos = sosRepository.save(sos);

            log.info("SOS 요청 생성 완료 - sosId={}, patientId={}, guardianId={}, played={}",
                savedSos.getId(), patientId, guardian.getId());

            // 3-2) 도움요청 전송
            sendSosNotification(guardian,patient, savedSos.getId());

            return SosResponse.from(savedSos);

        }else{
            throw new BusinessException(ErrorCode.RELATIONSHIP_ACCESS_DENIED);
        }
    }
    @Transactional
    public void createSosCallbackAckRequest(Integer patientId, Integer sosId) {
        // 1. SOS 요청 조회
        Sos sos = sosRepository.findById(sosId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SOS_NOT_FOUND));

        // 2. 환자 본인 확인 (워치가 보낸 요청이므로 환자만 가능)
        if (!sos.getPatient().getId().equals(patientId)) {
            throw new BusinessException(ErrorCode.SOS_CALLBACK_ACCESS_DENIED);
        }

        // 3. 이미 처리된 요청인지 확인 (idempotent)
        if (sos.getIsResponsed()) {
            log.info("이미 재생 완료 처리된 SOS 요청 - sosId={}", sosId);
            throw new BusinessException(ErrorCode.SOS_ALREADY_ACKNOWLEDGED);
        }

        // 4. 재생 완료 처리
        sos.markAsPlayed();

        log.info("SOS 재생 완료 콜백 처리 - sosId={}, patientId={}, played={}", sosId, patientId);

        // 5. 보호자에게 재생 완료 알림 전송
            sendPlaybackCompleteNotification(sos.getGuardian(), sos.getPatient(),sosId);

    }

    @Transactional
    public void stopSosRequest(Integer guardianId, Integer patientId) {
        // 1. 보호자 조회
        User guardian = userRepository.findById(guardianId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 환자 조회
        User patient = userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        // 3. 보호자 권한 확인
        if (guardian.getUserType() != UserType.GUARDIAN) {
            throw new BusinessException(ErrorCode.SOS_STOP_ACCESS_DENIED);
        }

        // 4. 관계 확인
        if (!relationshipRepository.existsByPatientIdAndGuardianId(patientId, guardianId)) {
            throw new BusinessException(ErrorCode.RELATIONSHIP_ACCESS_DENIED);
        }
        sendSosStopNotification(guardian, patient);
    }

    /**
     * sos 요청 알림 전송
     */
    private void sendSosNotification(User sender, User receiver,Integer sosLogId) {
        try {
            NotificationRequest notificationRequest = NotificationRequest.of(
                NotificationType.SOS_REQUEST.getDescription(),
                sender.getName() + "님이 SOS 음성 재생을 요청하였습니다.",
                NotificationType.SOS_REQUEST,
                sender.getId(),
                receiver.getId()
            );
            notificationService.createNotifications(notificationRequest,sosLogId);
            log.info("SOS 알림 전송 완료 - receiver: {}", receiver.getId());
        } catch (Exception e) {
            log.error("SOS 알림 전송 실패", e);
        }
    }

    /**
     * 재생 완료 알림 전송
     */
    private void sendPlaybackCompleteNotification(User guardian, User patient,Integer sosId) {
        try {
            NotificationRequest notificationRequest = NotificationRequest.of(
                "SOS 재생 완료",
                patient.getName() + "님의 워치에서 SOS 요청 음성이 재생되었습니다.",
                NotificationType.SOS_ACK,
                patient.getId(),
                guardian.getId()
            );
            notificationService.createNotifications(notificationRequest, sosId);
            log.info("SOS 재생 완료 알림 전송 - guardian: {}", guardian.getId());
        } catch (Exception e) {
            log.error("SOS 재생 완료 알림 전송 실패", e);
        }
    }

    /**
     * 환자에게 종료 알림 전송
     */
    private void sendSosStopNotification(User guardian, User patient) {
        try {
            NotificationRequest notificationRequest = NotificationRequest.of(
                "SOS 종료",
                guardian.getName() + "님이 도움 요청 음성을 종료했습니다.",
                NotificationType.SOS_STOP,
                guardian.getId(),
                patient.getId()
            );
            notificationService.createNotifications(notificationRequest, null);
            log.info("SOS 종료 알림 전송 - patient: {}", patient.getId());
        } catch (Exception e) {
            log.error("SOS 종료 알림 전송 실패", e);
        }
    }

}