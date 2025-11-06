# VoIP 통화 시스템 테스트 가이드

## 📋 목차
1. [환경 설정](#1-환경-설정)
2. [TURN/STUN 서버 테스트](#2-turnstun-서버-테스트)
3. [REST API 테스트](#3-rest-api-테스트)
4. [WebSocket 시그널링 테스트](#4-websocket-시그널링-테스트)
5. [통합 테스트 (HTML)](#5-통합-테스트-html)

---

## 1. 환경 설정

### 1.1 coturn 서버 설정 완료 확인

**현재 설정:**
- **서버 IP**: 43.202.55.40
- **포트**: 3478
- **프로토콜**: UDP/TCP
- **릴레이 포트**: 49152-65535

**coturn 설정 파일에 추가 필요:**
```bash
# /etc/turnserver.conf

# REST API 방식 사용 (필수)
use-auth-secret
static-auth-secret=YOUR_SHARED_SECRET_KEY_HERE

# 기존 user/password 방식 제거 또는 주석 처리
# user=myuser:mypassword123
```

**coturn 재시작:**
```bash
sudo systemctl restart coturn
sudo systemctl status coturn
```

### 1.2 백엔드 환경변수 설정

`.env.dev` 또는 `.env.prod` 파일에 추가:
```properties
# TURN/STUN Server
TURN_SERVER_HOST=43.202.55.40
TURN_SERVER_PORT=3478
TURN_SHARED_SECRET=YOUR_SHARED_SECRET_KEY_HERE
TURN_TTL=3600
```

⚠️ **중요**: `TURN_SHARED_SECRET`은 coturn의 `static-auth-secret`과 동일해야 합니다!

### 1.3 방화벽 설정 확인

**AWS Security Group에서 다음 포트 허용:**
```
- 3478 (UDP/TCP) - TURN/STUN 시그널링
- 49152-65535 (UDP) - TURN 릴레이
- 8080 (TCP) - 백엔드 서버 (또는 설정된 포트)
```

---

## 2. TURN/STUN 서버 테스트

### 2.1 Trickle ICE 도구로 테스트

**온라인 도구:** https://webrtc.github.io/samples/src/content/peerconnection/trickle-ice/

1. **STUN 서버 테스트**
   - STUN or TURN URI: `stun:43.202.55.40:3478`
   - 결과에서 `srflx` (Server Reflexive) 타입 확인

2. **TURN 서버 테스트**
   - 먼저 백엔드 API에서 자격증명 발급:
     ```bash
     curl -X GET http://localhost:8080/api/v1/calls/rtc/turn-credentials \
       -H "Authorization: Bearer YOUR_JWT_TOKEN"
     ```

   - 응답 예시:
     ```json
     {
       "message": "통화 요청이 수락되었습니다.",
       "data": {
         "username": "1736847890:ongil",
         "credential": "wZ3q/HmLxJ4tR9pV2kN8sQ==",
         "ttl": 3600,
         "uris": [
           "turn:43.202.55.40:3478?transport=udp",
           "turn:43.202.55.40:3478?transport=tcp",
           "stun:43.202.55.40:3478"
         ]
       }
     }
     ```

   - Trickle ICE에 입력:
     - TURN URI: `turn:43.202.55.40:3478?transport=udp`
     - Username: `1736847890:ongil` (응답의 username)
     - Credential: `wZ3q/HmLxJ4tR9pV2kN8sQ==` (응답의 credential)

   - 결과에서 `relay` 타입 확인 (TURN 성공)

### 2.2 coturn 로그 확인

```bash
# 실시간 로그 모니터링
sudo tail -f /var/log/turnserver/turnserver.log

# 최근 에러 확인
sudo grep -i error /var/log/turnserver/turnserver.log
```

---

## 3. REST API 테스트

### 3.1 필요 도구
- **Postman** 또는 **Thunder Client** (VS Code Extension)
- **curl** 명령어

### 3.2 JWT 토큰 발급

```bash
# 로그인
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "01012345678",
    "password": "password123"
  }'

# 응답에서 accessToken 복사
# 예: "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3.3 TURN 자격증명 발급 테스트

```bash
curl -X GET http://localhost:8080/api/v1/calls/rtc/turn-credentials \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**성공 응답:**
```json
{
  "message": "통화 요청이 수락되었습니다.",
  "data": {
    "username": "1736847890:ongil",
    "credential": "wZ3q/HmLxJ4tR9pV2kN8sQ==",
    "ttl": 3600,
    "uris": [
      "turn:43.202.55.40:3478?transport=udp",
      "turn:43.202.55.40:3478?transport=tcp",
      "stun:43.202.55.40:3478"
    ]
  }
}
```

### 3.4 통화 요청 생성 테스트

```bash
curl -X POST http://localhost:8080/api/v1/calls \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receiverId": 2,
    "callType": "NORMAL"
  }'
```

**성공 응답:**
```json
{
  "message": "통화 요청이 생성되었습니다.",
  "data": {
    "callId": 1,
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "caller": {
      "userId": 1,
      "name": "홍길동",
      "phoneNumber": "01012345678"
    },
    "receiver": {
      "userId": 2,
      "name": "김철수",
      "phoneNumber": "01087654321"
    },
    "callType": "NORMAL",
    "status": "CREATED",
    "startedAt": "2025-01-14T10:30:00"
  }
}
```

---

## 4. WebSocket 시그널링 테스트

### 4.1 필요 도구
- **wscat** (WebSocket 클라이언트)
- 또는 **브라우저 개발자 도구**

### 4.2 wscat 설치 및 사용

```bash
# wscat 설치
npm install -g wscat

# WebSocket 연결 (SockJS 사용)
# 주의: SockJS는 일반 WebSocket과 다르므로 브라우저에서 테스트하는 것을 권장
```

### 4.3 브라우저 콘솔에서 테스트

```javascript
// 1. SockJS + STOMP 라이브러리 로드 (HTML에서)
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

// 2. WebSocket 연결
const socket = new SockJS('http://localhost:8080/api/ws');
const stompClient = Stomp.over(socket);

// 3. 연결 (JWT 토큰 포함)
stompClient.connect(
  { Authorization: 'Bearer YOUR_ACCESS_TOKEN' },
  (frame) => {
    console.log('Connected:', frame);

    // 4. 메시지 구독
    stompClient.subscribe('/user/queue/calls/1', (message) => {
      console.log('Received:', JSON.parse(message.body));
    });
  },
  (error) => {
    console.error('Connection error:', error);
  }
);

// 5. 시그널 메시지 전송 (예: OFFER)
stompClient.send('/app/calls/1/signal', {}, JSON.stringify({
  type: 'OFFER',
  sdp: 'v=0\r\no=- 1234567890 1234567890 IN IP4 127.0.0.1\r\n...',
  fromUserId: 1,
  toUserId: 2,
  callId: 1
}));
```

### 4.4 간단한 테스트용 시그널링 (인증 없음)

```javascript
// 테스트용 SignalingController 사용 (인증 불필요)
const socket = new SockJS('http://localhost:8080/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
  console.log('Connected:', frame);

  // 룸 구독
  stompClient.subscribe('/topic/room/test123', (message) => {
    console.log('Received:', JSON.parse(message.body));
  });

  // 메시지 발행
  stompClient.send('/app/signal/test123', {}, JSON.stringify({
    type: 'test',
    message: 'Hello WebSocket!'
  }));
});
```

---

## 5. 통합 테스트 (HTML)

### 5.1 간단한 테스트 페이지

`test.html` 파일을 생성하고 브라우저에서 열기:

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>VoIP 시그널링 테스트</title>
  <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
  <h1>VoIP WebSocket 시그널링 테스트</h1>

  <div>
    <label>서버 URL:</label>
    <input type="text" id="serverUrl" value="http://localhost:8080/api/ws" style="width: 300px;">
  </div>

  <div>
    <label>Room ID:</label>
    <input type="text" id="roomId" value="test123">
  </div>

  <div>
    <button onclick="connect()">연결</button>
    <button onclick="disconnect()">연결 해제</button>
  </div>

  <div>
    <label>메시지:</label>
    <input type="text" id="message" value="Hello!">
    <button onclick="sendMessage()">전송</button>
  </div>

  <h3>수신 메시지:</h3>
  <div id="messages" style="border: 1px solid #ccc; padding: 10px; height: 300px; overflow-y: scroll;"></div>

  <script>
    let stompClient = null;

    function connect() {
      const serverUrl = document.getElementById('serverUrl').value;
      const roomId = document.getElementById('roomId').value;

      const socket = new SockJS(serverUrl);
      stompClient = Stomp.over(socket);

      stompClient.connect({}, (frame) => {
        log('✅ 연결 성공: ' + frame);

        // 룸 구독
        stompClient.subscribe('/topic/room/' + roomId, (message) => {
          log('📩 수신: ' + message.body);
        });
      }, (error) => {
        log('❌ 연결 실패: ' + error);
      });
    }

    function disconnect() {
      if (stompClient !== null) {
        stompClient.disconnect();
        log('🔌 연결 해제됨');
      }
    }

    function sendMessage() {
      const roomId = document.getElementById('roomId').value;
      const messageText = document.getElementById('message').value;

      if (stompClient && stompClient.connected) {
        const message = {
          type: 'test',
          content: messageText,
          timestamp: new Date().toISOString()
        };

        stompClient.send('/app/signal/' + roomId, {}, JSON.stringify(message));
        log('📤 전송: ' + JSON.stringify(message));
      } else {
        log('❌ 연결되지 않음');
      }
    }

    function log(message) {
      const messagesDiv = document.getElementById('messages');
      const p = document.createElement('p');
      p.textContent = `[${new Date().toLocaleTimeString()}] ${message}`;
      messagesDiv.appendChild(p);
      messagesDiv.scrollTop = messagesDiv.scrollHeight;
    }
  </script>
</body>
</html>
```

### 5.2 테스트 시나리오

1. **브라우저 2개 열기** (또는 시크릿 모드)
2. 둘 다 `test.html` 파일 열기
3. 같은 Room ID 입력 (예: `test123`)
4. 둘 다 "연결" 버튼 클릭
5. 한쪽에서 메시지 입력 후 "전송" 클릭
6. 다른 브라우저에서 메시지 수신 확인

---

## 6. 문제 해결 (Troubleshooting)

### 6.1 WebSocket 연결 실패

**증상:** `Connection refused` 또는 `404 Not Found`

**해결:**
```bash
# 1. 백엔드 서버 실행 확인
curl http://localhost:8080/actuator/health

# 2. WebSocket 엔드포인트 확인
curl -I http://localhost:8080/api/ws

# 3. 로그 확인
# application.yml에서 로깅 레벨 조정
logging:
  level:
    org.springframework.web.socket: DEBUG
    org.springframework.messaging: DEBUG
```

### 6.2 TURN 자격증명 생성 실패

**증상:** `Failed to generate TURN credentials`

**해결:**
1. `.env` 파일에 `TURN_SHARED_SECRET` 확인
2. coturn 설정에 `static-auth-secret` 일치 확인
3. 백엔드 로그 확인:
   ```bash
   tail -f logs/spring.log | grep TURN
   ```

### 6.3 시그널링 메시지 전송 실패

**증상:** `CALL_PERMISSION_DENIED` 에러

**해결:**
- JWT 토큰이 유효한지 확인
- callId가 올바른지 확인
- 발신자/수신자 권한 확인 (CallSignalController에서 검증)

### 6.4 TURN 서버 연결 실패

**증상:** Trickle ICE에서 `relay` 타입 미출현

**해결:**
```bash
# 1. coturn 프로세스 확인
ps aux | grep turnserver

# 2. 포트 리스닝 확인
sudo netstat -tulnp | grep 3478

# 3. 방화벽 확인
sudo ufw status
sudo iptables -L -n

# 4. coturn 디버그 모드 실행
turnserver -v -L 0.0.0.0 -L 43.202.55.40 --log-file=stdout
```

---

## 7. 체크리스트

### 백엔드 설정
- [ ] coturn 서버 실행 중
- [ ] coturn에 `use-auth-secret`, `static-auth-secret` 설정
- [ ] `.env` 파일에 TURN 환경변수 설정
- [ ] 백엔드 서버 실행 중
- [ ] 방화벽에서 필요 포트 오픈

### API 테스트
- [ ] JWT 토큰 발급 성공
- [ ] TURN 자격증명 API 응답 정상
- [ ] 통화 요청 생성 API 응답 정상

### WebSocket 테스트
- [ ] SockJS 연결 성공
- [ ] STOMP CONNECT 프레임 전송 성공
- [ ] 메시지 구독 성공
- [ ] 메시지 전송/수신 성공

### TURN 서버 테스트
- [ ] STUN 바인딩 성공 (srflx 타입)
- [ ] TURN 릴레이 성공 (relay 타입)
- [ ] coturn 로그에 에러 없음

---

## 8. 다음 단계

✅ 백엔드 테스트 완료 후:
1. **안드로이드/iOS 클라이언트 구현**
   - WebRTC PeerConnection 설정
   - SockJS + STOMP 클라이언트 연동
   - ICE candidate 교환

2. **FCM 푸시 알림 연동**
   - 앱이 백그라운드일 때 INCOMING call 알림

3. **통화 품질 모니터링**
   - WebRTC getStats() API 사용
   - 패킷 손실률, 레이턴시, 지터 측정

---

## 참고 자료

- **WebRTC 공식 문서**: https://webrtc.org/getting-started/overview
- **coturn 설정 가이드**: https://github.com/coturn/coturn/wiki
- **STOMP 프로토콜**: https://stomp.github.io/
- **Trickle ICE 테스트**: https://webrtc.github.io/samples/src/content/peerconnection/trickle-ice/
