package kr.co.ongil.domain.call.service;

import kr.co.ongil.domain.call.entity.Call;
import kr.co.ongil.domain.call.entity.CallLog;
import kr.co.ongil.domain.call.entity.CallSource;
import kr.co.ongil.domain.call.repository.CallLogRepository;
import kr.co.ongil.domain.call.repository.CallRepository;
import kr.co.ongil.domain.patient.entity.PatientState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 통화 타임아웃 정리 스케줄러
 * - 주기적으로 만료된 통화를 찾아서 MISSED로 변경
 * - 응답 없는 통화가 계속 활성 상태로 남지 않도록 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallCleanupScheduler {

    private final CallRepository callRepository;
    private final CallLogRepository callLogRepository;

    @Value("${call.timeout-seconds:60}")
    private int timeoutSeconds;

    /**
     * 매 10초마다 만료된 통화 정리
     * - CREATED, RINGING 상태이면서 타임아웃된 통화를 MISSED로 변경
     * - CallLog 자동 생성
     */
    @Scheduled(fixedRate = 10000)  // 10초마다 실행
    @Transactional
    public void cleanupExpiredCalls() {
        LocalDateTime expiryTime = LocalDateTime.now().minusSeconds(timeoutSeconds);
        List<Call> expiredCalls = callRepository.findExpiredCalls(expiryTime);

        if (expiredCalls.isEmpty()) {
            return;
        }

        log.info("만료된 통화 {}건 발견 (타임아웃: {}초)", expiredCalls.size(), timeoutSeconds);

        for (Call call : expiredCalls) {
            try {
                log.info("통화 타임아웃 처리: callId={}, caller={}, receiver={}, status={}, startedAt={}",
                    call.getId(),
                    call.getCaller().getId(),
                    call.getReceiver().getId(),
                    call.getStatus(),
                    call.getStartedAt());

                // 통화를 MISSED로 종료
                call.expireCall();
                callRepository.save(call);

                // CallLog 생성
                createCallLogFromExpiredCall(call);

                log.info("통화 타임아웃 처리 완료: callId={}", call.getId());

            } catch (Exception e) {
                log.error("통화 타임아웃 처리 중 오류 발생: callId={}", call.getId(), e);
            }
        }
    }

    /**
     * 만료된 통화에서 CallLog 생성
     */
    private void createCallLogFromExpiredCall(Call call) {
        // TODO: 실제로는 환자의 현재 위치와 상태를 조회해야 함
        String patientLocation = null;
        PatientState patientState = PatientState.NORMAL;

        CallLog callLog = CallLog.builder()
            .caller(call.getCaller())
            .receiver(call.getReceiver())
            .callType(call.getCallType())
            .source(CallSource.APP)
            .patientState(patientState)
            .patientLocation(patientLocation)
            .startedAt(call.getStartedAt())
            .endedAt(call.getEndedAt())
            .duration(call.getDuration())
            .build();

        callLogRepository.save(callLog);
        log.info("만료된 통화 CallLog 생성 완료: callLogId={}, callId={}", callLog.getId(), call.getId());
    }
}
