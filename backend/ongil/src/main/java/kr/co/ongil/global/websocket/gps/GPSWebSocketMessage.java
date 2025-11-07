package kr.co.ongil.global.websocket.gps;

import kr.co.ongil.domain.map.dto.response.CoordinateInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * - type
 *   - "GPS_UPDATE" : 환자가 자신의 위치를 서버로 전송
 *   - "CONNECTION_ACK" : 서버가 연결 성공을 알림
 *
 * - coordinate
 *   - GPS 좌표 정보 (latitude, longitude)
 *   - type이 "CONNECTION_ACK"일 경우 null
 *
 *  통신 방향
 * - 환자 → 서버 : type="GPS_UPDATE"
 * - 서버 → 환자 : type="CONNECTION_ACK"
 *
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GPSWebSocketMessage {
    private String type;
    private CoordinateInfo coordinate;
}
