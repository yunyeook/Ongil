package kr.co.ongil.domain.patient.location.service;

import kr.co.ongil.global.eventListener.LocationUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GPS 위치 SSE 스트리밍 서비스
 */
@Service
@Slf4j
public class LocationSSEService {

    // guardianId -> SseEmitter
    private final ConcurrentHashMap<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE Emitter 등록
     */
    public SseEmitter createEmitter(Integer guardianId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.put(guardianId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(guardianId);
            log.info("SSE 연결 종료: guardianId={}", guardianId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(guardianId);
            emitter.complete();
            log.warn("SSE 타임아웃: guardianId={}", guardianId);
        });

        emitter.onError((e) -> {
            emitters.remove(guardianId);
            emitter.complete();
            log.error("SSE 에러: guardianId={}", guardianId, e);
        });

        log.info("SSE 연결됨: guardianId={}", guardianId);

        // 초기 연결 확인 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("SSE connection established"));
        } catch (IOException e) {
            log.error("SSE 초기 메시지 전송 실패: guardianId={}", guardianId, e);
        }

        return emitter;
    }

    /**
     * 보호자에게 GPS 업데이트 전송
     */
    public void sendGPSUpdate(Integer guardianId, LocationUpdatedEvent event) {
        SseEmitter emitter = emitters.get(guardianId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("gps-update")
                    .data(event));
                log.debug("SSE GPS 전송: guardianId={}", guardianId);
            } catch (IOException e) {
                log.error("SSE 전송 실패: guardianId={}", guardianId, e);
                emitters.remove(guardianId);
                emitter.completeWithError(e);
            }
        } else {
            log.debug("보호자 SSE 미연결: guardianId={}", guardianId);
        }
    }

    /**
     * 연결 여부 확인
     */
    public boolean isConnected(Integer guardianId) {
        return emitters.containsKey(guardianId);
    }

    /**
     * 특정 연결 종료
     */
    public void closeConnection(Integer guardianId) {
        SseEmitter emitter = emitters.remove(guardianId);
        if (emitter != null) {
            emitter.complete();
            log.info("SSE 연결 강제 종료: guardianId={}", guardianId);
        }
    }
}