package kr.co.ongil.domain.call.service;

import kr.co.ongil.domain.call.dto.request.CreateCallRequest;
import kr.co.ongil.domain.call.dto.request.UpdateCallStatusRequest;
import kr.co.ongil.domain.call.dto.response.CallResponse;
import kr.co.ongil.domain.call.entity.Call;
import kr.co.ongil.domain.call.entity.CallLog;
import kr.co.ongil.domain.call.entity.CallSource;
import kr.co.ongil.domain.call.entity.CallStatus;
import kr.co.ongil.domain.call.repository.CallLogRepository;
import kr.co.ongil.domain.call.repository.CallRepository;
import kr.co.ongil.domain.patient.entity.PatientState;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * VoIP 통화 서비스
 * - VoIP 세션 생성 및 상태 관리
 * - 통화 종료 시 자동으로 CallLog 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final CallRepository callRepository;
    private final CallLogRepository callLogRepository;
    private final UserRepository userRepository;
    private final kr.co.ongil.domain.call.controller.CallSignalController callSignalController;

    /**
     * VoIP 통화 요청 생성
     */
    @Transactional
    public CallResponse createCall(Integer callerId, CreateCallRequest request) {
        log.info("VoIP 통화 요청 생성: callerId={}, receiverId={}", callerId, request.receiverId());

        // 1. 발신자/수신자 조회
        User caller = userRepository.findById(callerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User receiver = userRepository.findById(request.receiverId())
            .orElseThrow(() -> new BusinessException(ErrorCode.RECEIVER_NOT_FOUND));

        // 2. 자기 자신에게 전화 금지
        if (caller.getId().equals(receiver.getId())) {
            throw new BusinessException(ErrorCode.CANNOT_CALL_SELF);
        }

        // 3. 보호자-환자 관계 검증
        // TODO: RelationshipRepository를 통해 두 사용자 간 관계가 존재하는지 확인
        // validateRelationship(caller, receiver);

        // 4. 발신자가 이미 통화 중인지 확인
        if (callRepository.findActiveCallByUser(caller).isPresent()) {
            throw new BusinessException(ErrorCode.USER_ALREADY_IN_CALL);
        }

        // 5. 수신자가 이미 통화 중인지 확인
        if (callRepository.findActiveCallByUser(receiver).isPresent()) {
            throw new BusinessException(ErrorCode.USER_ALREADY_IN_CALL);
        }

        // 6. 서버에서 세션 ID 생성 (UUID)
        String sessionId = UUID.randomUUID().toString();

        // 7. VoIP 통화 세션 생성
        Call call = Call.builder()
            .caller(caller)
            .receiver(receiver)
            .callType(request.callType())
            .status(CallStatus.CREATED)
            .sessionId(sessionId)
            .startedAt(LocalDateTime.now())
            .build();

        Call savedCall = callRepository.save(call);
        log.info("VoIP 통화 세션 생성 완료: callId={}, sessionId={}", savedCall.getId(), sessionId);

        // 8. 수신자에게 INCOMING 시그널 전송 (WebSocket + TODO: FCM)
        callSignalController.sendIncomingCall(
            savedCall.getId(),
            caller.getId(),
            receiver.getId()
        );

        // 9. INCOMING 시그널 전송 후 자동으로 RINGING 상태로 전환
        savedCall.ring();
        savedCall = callRepository.save(savedCall);
        log.info("통화 상태 RINGING으로 전환: callId={}", savedCall.getId());

        return CallResponse.from(savedCall);
    }

    /**
     * VoIP 통화 상태 업데이트
     */
    @Transactional
    public CallResponse updateCallStatus(Integer callId, UpdateCallStatusRequest request) {
        log.info("VoIP 통화 상태 업데이트: callId={}, status={}", callId, request.status());

        // 1. 통화 세션 조회
        Call call = callRepository.findById(callId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_NOT_FOUND));

        // 2. 이미 종료된 통화인지 확인
        if (call.isEnded()) {
            throw new BusinessException(ErrorCode.CALL_ALREADY_ENDED);
        }

        // 3. 상태 업데이트
        CallStatus newStatus = request.status();

        switch (newStatus) {
            case RINGING -> call.ring();
            case CONNECTED -> call.connect();
            case ENDED, CANCELED, REJECTED, FAILED, MISSED, DROPPED -> {
                call.end(newStatus);
                // 통화 종료 시 자동으로 CallLog 생성
                createCallLogFromCall(call);
            }
            default -> throw new BusinessException(ErrorCode.INVALID_CALL_STATUS);
        }

        Call updatedCall = callRepository.save(call);
        log.info("VoIP 통화 상태 업데이트 완료: callId={}, newStatus={}", callId, newStatus);

        return CallResponse.from(updatedCall);
    }

    /**
     * 세션 ID로 통화 조회
     */
    @Transactional(readOnly = true)
    public CallResponse getCallBySessionId(String sessionId) {
        log.info("세션 ID로 통화 조회: sessionId={}", sessionId);

        Call call = callRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_NOT_FOUND));

        return CallResponse.from(call);
    }

    /**
     * 통화 ID로 조회
     */
    @Transactional(readOnly = true)
    public CallResponse getCallById(Integer callId) {
        log.info("통화 ID로 조회: callId={}", callId);

        Call call = callRepository.findById(callId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_NOT_FOUND));

        return CallResponse.from(call);
    }

    /**
     * VoIP 통화 종료 시 CallLog 자동 생성
     */
    private void createCallLogFromCall(Call call) {
        log.info("VoIP 통화 종료 - CallLog 자동 생성: callId={}", call.getId());

        // TODO: 실제로는 환자의 현재 위치와 상태를 조회해야 함
        // 지금은 기본값으로 설정
        String patientLocation = null;  // 환자 위치 정보
        PatientState patientState = PatientState.NORMAL;  // 환자 상태

        CallLog callLog = CallLog.builder()
            .caller(call.getCaller())
            .receiver(call.getReceiver())
            .callType(call.getCallType())
            .source(CallSource.APP)  // VoIP는 항상 APP
            .patientState(patientState)
            .patientLocation(patientLocation)
            .startedAt(call.getStartedAt())
            .endedAt(call.getEndedAt())
            .duration(call.getDuration())
            .build();

        callLogRepository.save(callLog);
        log.info("CallLog 자동 생성 완료: callLogId={}", callLog.getId());
    }
}
