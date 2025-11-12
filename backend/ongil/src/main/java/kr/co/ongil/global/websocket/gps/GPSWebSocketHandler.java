package kr.co.ongil.global.websocket.gps;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import kr.co.ongil.domain.map.dto.response.CoordinateInfo;
import kr.co.ongil.domain.patient.location.service.LocationRedisService;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.sse.event.LocationUpdatedEvent;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * GPS 추적용 WebSocket Handler (길찾기 모드)
 *
 * 연결 정책:
 * - 환자만 연결 가능 (userType=PATIENT)
 * - 길찾기 시작 시 연결, 종료 시 연결 해제
 * - 10초마다 GPS 전송 권장
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GPSWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final LocationRedisService locationRedisService;
    private final ApplicationEventPublisher eventPublisher;

    // patientId -> WebSocketSession (환자 연결)
    private final ConcurrentHashMap<Integer, WebSocketSession> patientSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        try {
            Integer userId = (Integer) session.getAttributes().get("userId");
            String userType = (String) session.getAttributes().get("userType");

            if (!"PATIENT".equals(userType)) {
                log.warn("WebSocket 연결 거부: 환자만 연결 가능 (userType={})", userType);
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }

            // 기존 세션 종료 후 새 세션 등록
            WebSocketSession existingSession = patientSessions.get(userId);
            if (existingSession != null && existingSession.isOpen()) {
                existingSession.close(CloseStatus.NORMAL);
            }

            patientSessions.put(userId, session);
            log.info("환자 WebSocket 연결 완료: patientId={}", userId);

            sendConnectionAck(session);
        } catch (Exception e) {
            log.error("WebSocket 연결 실패", e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            //  SecurityUtil 대신 HandshakeInterceptor에서 넣어준 세션 속성 사용
            Integer patientId = (Integer) session.getAttributes().get("userId");
            String userType = (String) session.getAttributes().get("userType");

            if (patientId == null || !"PATIENT".equals(userType)) {
                log.warn("WebSocket 메시지 처리 실패: 인증이 필요합니다. (userType={})", userType);
                return;
            }

            GPSWebSocketMessage wsMessage = objectMapper.readValue(
                message.getPayload(),
                GPSWebSocketMessage.class
            );

            if ("GPS_UPDATE".equals(wsMessage.getType())) {
                CoordinateInfo coordinate = wsMessage.getCoordinate();

                if (coordinate == null || coordinate.latitude() == null || coordinate.longitude() == null) {
                    log.warn("WebSocket 메시지 처리 실패: 좌표 정보 없음 (patientId={})", patientId);
                    return;
                }

                //  Redis에 환자 위치 저장
                locationRedisService.saveLocation(patientId, coordinate);

                //  이벤트 발행 → LocationEventListener가 SSE로 보호자에게 전송
                eventPublisher.publishEvent(new LocationUpdatedEvent(patientId, coordinate));

                log.debug("GPS 위치 업데이트 완료: patientId={}, lat={}, lng={}",
                    patientId, coordinate.latitude(), coordinate.longitude());
            }  else if ("GPS_DISCONNECT".equals(wsMessage.getType())) {
                // GPS 추적 종료 요청 처리
                log.info("GPS 추적 종료 요청: patientId={}", patientId);

                // 세션 정상 종료
                session.close(CloseStatus.NORMAL);
                patientSessions.remove(patientId);

                log.info("GPS WebSocket 연결 종료 완료: patientId={}", patientId);

            } else {
                log.warn("알 수 없는 메시지 타입: type={}", wsMessage.getType());
            }
        } catch (Exception e) {
            log.error("WebSocket 메시지 처리 실패", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            // SecurityUtil로 userId 추출
            Integer userId = SecurityUtil.getCurrentUserId();
            patientSessions.remove(userId);
            log.info("환자 WebSocket 연결 종료: patientId={}, status={}", userId, status);
        } catch (BusinessException e) {
            // 인증 정보 없이 종료된 경우 (드문 경우)
            patientSessions.values().remove(session);
            log.info("환자 WebSocket 연결 종료: status={}", status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        try {
            Integer userId = SecurityUtil.getCurrentUserId();
            log.error("WebSocket 전송 오류: patientId={}", userId, exception);
        } catch (BusinessException e) {
            log.error("WebSocket 전송 오류", exception);
        }
    }

    /**
     * 연결 확인 메시지 전송
     */
    private void sendConnectionAck(WebSocketSession session) {
        try {
            GPSWebSocketMessage ackMessage = new GPSWebSocketMessage("CONNECTION_ACK", null);
            String json = objectMapper.writeValueAsString(ackMessage);
            session.sendMessage(new TextMessage(json));
            log.debug("연결 확인 메시지 전송 완료");
        } catch (IOException e) {
            log.error("연결 확인 메시지 전송 실패", e);
        }
    }

    /**
     * 환자 연결 여부 확인
     */
    public boolean isPatientConnected(Integer patientId) {
        WebSocketSession session = patientSessions.get(patientId);
        return session != null && session.isOpen();
    }

    /**
     * 특정 환자의 WebSocket 연결 강제 종료
     */
    public void closeConnection(Integer patientId) {
        WebSocketSession session = patientSessions.remove(patientId);
        if (session != null && session.isOpen()) {
            try {
                session.close(CloseStatus.NORMAL);
                log.info("WebSocket 연결 강제 종료: patientId={}", patientId);
            } catch (IOException e) {
                log.error("WebSocket 연결 종료 실패: patientId={}", patientId, e);
            }
        }
    }

    /**
     * 현재 연결된 환자 수
     */
    public int getConnectedPatientCount() {
        return patientSessions.size();
    }
}