package kr.co.ongil.domain.call.service;

import kr.co.ongil.domain.call.dto.request.CreateCallLogRequest;
import kr.co.ongil.domain.call.dto.response.CallLogResponse;
import kr.co.ongil.domain.call.entity.CallLog;
import kr.co.ongil.domain.call.entity.CallType;
import kr.co.ongil.domain.call.repository.CallLogRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 통화 로그 서비스
 * - 기본 전화 통화 로그 생성 (클라이언트 콜백)
 * - 통화 로그 조회 및 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallLogService {

    private final CallLogRepository callLogRepository;
    private final UserRepository userRepository;

    /**
     * 기본 전화 통화 로그 생성 (클라이언트 콜백)
     */
    @Transactional
    public CallLogResponse createCallLog(Integer callerId, CreateCallLogRequest request) {
        log.info("기본 전화 통화 로그 생성: callerId={}, receiverId={}", callerId, request.receiverId());

        // 1. 발신자/수신자 조회
        User caller = userRepository.findById(callerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User receiver = userRepository.findById(request.receiverId())
            .orElseThrow(() -> new BusinessException(ErrorCode.RECEIVER_NOT_FOUND));

        // 2. 통화 로그 생성
        CallLog callLog = CallLog.builder()
            .caller(caller)
            .receiver(receiver)
            .callType(request.callType())
            .source(request.source())
            .patientState(request.patientState())
            .patientLocation(request.patientLocation())
            .startedAt(request.startedAt())
            .endedAt(request.endedAt())
            .duration(request.duration())
            .memo(request.memo())
            .build();

        CallLog savedCallLog = callLogRepository.save(callLog);
        log.info("통화 로그 생성 완료: callLogId={}", savedCallLog.getId());

        return CallLogResponse.from(savedCallLog);
    }

    /**
     * 사용자의 통화 로그 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<CallLogResponse> getCallLogs(Integer userId, Pageable pageable) {
        log.info("통화 로그 목록 조회: userId={}, page={}, size={}", userId, pageable.getPageNumber(), pageable.getPageSize());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<CallLog> callLogs = callLogRepository.findByUser(user, pageable);

        return callLogs.map(CallLogResponse::from);
    }

    /**
     * 통화 로그 단건 조회
     */
    @Transactional(readOnly = true)
    public CallLogResponse getCallLog(Integer callLogId) {
        log.info("통화 로그 조회: callLogId={}", callLogId);

        CallLog callLog = callLogRepository.findById(callLogId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_LOG_NOT_FOUND));

        return CallLogResponse.from(callLog);
    }

    /**
     * 긴급 통화 기록 조회
     */
    @Transactional(readOnly = true)
    public List<CallLogResponse> getEmergencyCallLogs(Integer userId) {
        log.info("긴급 통화 기록 조회: userId={}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<CallLog> emergencyLogs = callLogRepository.findByUserAndCallType(user, CallType.EMERGENCY);

        return emergencyLogs.stream()
            .map(CallLogResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 특정 기간의 통화 로그 조회
     */
    @Transactional(readOnly = true)
    public List<CallLogResponse> getCallLogsByDateRange(
        Integer userId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        log.info("기간별 통화 로그 조회: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<CallLog> callLogs = callLogRepository.findByUserAndDateRange(user, startDate, endDate);

        return callLogs.stream()
            .map(CallLogResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 통화 로그 메모 추가/수정
     */
    @Transactional
    public CallLogResponse updateMemo(Integer callLogId, String memo) {
        log.info("통화 로그 메모 수정: callLogId={}", callLogId);

        CallLog callLog = callLogRepository.findById(callLogId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_LOG_NOT_FOUND));

        callLog.updateMemo(memo);
        CallLog updatedCallLog = callLogRepository.save(callLog);

        log.info("통화 로그 메모 수정 완료: callLogId={}", callLogId);

        return CallLogResponse.from(updatedCallLog);
    }

    /**
     * 통화 로그 삭제
     */
    @Transactional
    public void deleteCallLog(Integer callLogId) {
        log.info("통화 로그 삭제: callLogId={}", callLogId);

        CallLog callLog = callLogRepository.findById(callLogId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CALL_LOG_NOT_FOUND));

        callLogRepository.delete(callLog);
        log.info("통화 로그 삭제 완료: callLogId={}", callLogId);
    }
}
