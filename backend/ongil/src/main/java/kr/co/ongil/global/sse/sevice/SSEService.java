package kr.co.ongil.global.sse.sevice;

import kr.co.ongil.global.sse.event.LocationUpdatedEvent;
import kr.co.ongil.global.sse.event.NavigationEvent;
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
public class SSEService {

    private final ConcurrentHashMap<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE Emitter 등록
     */
    public SseEmitter createEmitter(Integer userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("SSE 연결 종료: userId={}", userId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId);
            emitter.complete();
            log.warn("SSE 타임아웃: userId={}", userId);
        });

        emitter.onError((e) -> {
            emitters.remove(userId);
            emitter.complete();
            log.error("SSE 에러: userId={}", userId, e);
        });

        log.info("SSE 연결됨: userId={}", userId);

        // 초기 연결 확인 메시지 전송
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("SSE connection established"));
        } catch (IOException e) {
            log.error("SSE 초기 메시지 전송 실패: userId={}", userId, e);
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
     * 환자 및 보호자에게 길안내 경로 전송
     */
    public void sendNavigationUpdate(Integer receiverId, NavigationEvent event) {
        SseEmitter emitter = emitters.get(receiverId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("navigation-update")
                    .data(event));
                log.debug("SSE 길안내 전송: receiverId={}, status={}, initiator={}",
                    receiverId, event.status(), event.userType());
            } catch (IOException e) {
                emitters.remove(receiverId);
                emitter.completeWithError(e);
                log.error("SSE 전송 실패: receiverId={}", receiverId, e);
            }
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