# coturn 서버 설정 가이드

## 🔴 현재 문제

coturn 로그에서 다음 에러 발생:
```
session 003000000000000001: realm <43.202.55.40> user <>: incoming packet message processed, error 401: Unauthorized
```

**원인**: coturn이 REST API 인증 방식으로 제대로 설정되지 않았습니다.

---

## ✅ 해결 방법

### 1단계: coturn 서버 SSH 접속

```bash
ssh ubuntu@43.202.55.40
```

### 2단계: 현재 설정 백업

```bash
sudo cp /etc/turnserver.conf /etc/turnserver.conf.backup
```

### 3단계: coturn 설정 파일 수정

```bash
sudo vi /etc/turnserver.conf
```

**다음 내용으로 수정:**

```bash
# === 기본 리스닝 설정 ===
listening-port=3478
listening-ip=0.0.0.0

# === IP 설정 ===
external-ip=43.202.55.40/172.26.9.243

# === 릴레이 포트 범위 ===
min-port=49152
max-port=65535

# === REST API 인증 설정 (중요!) ===
use-auth-secret
static-auth-secret=OngilTurnSecretKey2025!@#$

# ⚠️ 아래 user/password 방식은 주석 처리 또는 제거!
# user=myuser:mypassword123

# === Long-term Credential Mechanism ===
lt-cred-mech

# === Realm ===
realm=43.202.55.40

# === 보안 설정 ===
fingerprint
no-multicast-peers
no-cli

# no-loopback-peers는 오타가 있어서 제거 (Bad configuration format 에러 원인)
# no-loopback-peers  <- 이 라인 제거!

# === 로그 설정 ===
verbose
log-file=/var/log/turnserver/turnserver.log

# === 성능 최적화 ===
max-bps=0
bps-capacity=0
```

**핵심 변경 사항:**
1. ✅ `use-auth-secret` 추가 (REST API 방식 활성화)
2. ✅ `static-auth-secret=OngilTurnSecretKey2025!@#$` 추가 (공유 비밀키)
3. ❌ `user=myuser:mypassword123` 제거 또는 주석 처리
4. ❌ `no-loopback-peers` 제거 (설정 오류 발생 원인)

### 4단계: 백엔드 환경변수 설정

`.env.dev` 또는 `.env.prod` 파일에 추가:

```properties
# TURN/STUN Server Configuration
TURN_SERVER_HOST=43.202.55.40
TURN_SERVER_PORT=3478
TURN_SHARED_SECRET=OngilTurnSecretKey2025!@#$  # coturn의 static-auth-secret과 동일!
TURN_TTL=3600
```

⚠️ **중요**: `TURN_SHARED_SECRET` 값은 coturn의 `static-auth-secret` 값과 **정확히 일치**해야 합니다!

### 5단계: coturn 재시작

```bash
sudo systemctl restart coturn
sudo systemctl status coturn
```

**정상 실행 확인:**
```bash
● turnserver.service - coturn TURN Server
     Loaded: loaded (/lib/systemd/system/turnserver.service; enabled; vendor preset: enabled)
     Active: active (running) since ...
```

### 6단계: 로그 확인

```bash
# 실시간 로그 모니터링
sudo tail -f /var/log/turnserver/turnserver.log

# 에러 확인
sudo grep -i error /var/log/turnserver/turnserver.log

# 인증 실패 확인
sudo grep -i "401" /var/log/turnserver/turnserver.log
```

**정상 동작 시 로그:**
```
0: : session xxx: realm <43.202.55.40> user <1762479652:ongil>: incoming packet ALLOCATE processed, success
```

**비정상 시 로그:**
```
0: : session xxx: realm <43.202.55.40> user <>: incoming packet message processed, error 401: Unauthorized
```

---

## 🧪 테스트 방법

### 1. 백엔드 서버에서 TURN 자격증명 발급 테스트

```bash
curl -X GET https://staging.on-gil.co.kr/api/v1/calls/rtc/turn-credentials \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**정상 응답:**
```json
{
  "message": "통화 요청이 수락되었습니다.",
  "data": {
    "username": "1762479652:ongil",
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

### 2. Trickle ICE로 TURN 서버 테스트

**온라인 도구:** https://webrtc.github.io/samples/src/content/peerconnection/trickle-ice/

1. 위 API에서 받은 응답 사용:
   - **TURN URI**: `turn:43.202.55.40:3478?transport=udp`
   - **TURN username**: `1762479652:ongil` (응답의 username)
   - **TURN password**: `wZ3q/HmLxJ4tR9pV2kN8sQ==` (응답의 credential)

2. **Gather candidates** 버튼 클릭

3. **결과 확인:**
   - ✅ `srflx` 타입: STUN 성공
   - ✅ `relay` 타입: TURN 성공 🎉

**성공 예시:**
```
Time    Type        Protocol    Address
0.123   srflx       udp         222.107.238.22:54585
0.456   relay       udp         43.202.55.40:51234   ← 이게 나와야 성공!
```

**실패 시 (relay 타입 없음):**
- coturn 로그 확인: `sudo tail -f /var/log/turnserver/turnserver.log`
- 401 Unauthorized 에러 확인
- `static-auth-secret` 설정 다시 확인

---

## 🐛 문제 해결

### 문제 1: "Bad configuration format: no-loopback-peers"

**원인**: `no-loopback-peers` 옵션이 오타이거나 지원되지 않음

**해결**: 해당 라인 제거
```bash
# no-loopback-peers  <- 주석 처리 또는 삭제
```

### 문제 2: "error 401: Unauthorized"

**원인**: REST API 인증 설정 누락 또는 불일치

**해결**:
1. coturn 설정에 `use-auth-secret` 있는지 확인
2. `static-auth-secret` 값이 백엔드 `.env` 파일의 `TURN_SHARED_SECRET`과 동일한지 확인
3. `user=myuser:mypassword123` 주석 처리 (충돌 방지)

### 문제 3: "WARNING: cannot find certificate file"

**원인**: TLS/DTLS 인증서 없음

**해결**: UDP/TCP만 사용하므로 무시해도 됨 (HTTPS 없이 HTTP만 사용할 경우)

운영 환경에서는 인증서 설정 권장:
```bash
cert=/etc/letsencrypt/live/turn.ongil.app/fullchain.pem
pkey=/etc/letsencrypt/live/turn.ongil.app/privkey.pem
```

### 문제 4: 방화벽 포트 차단

**확인**:
```bash
# 포트 리스닝 확인
sudo netstat -tulnp | grep 3478

# 방화벽 규칙 확인
sudo ufw status
sudo iptables -L -n | grep 3478
```

**해결** (AWS Security Group):
```
Inbound Rules:
- 3478 (TCP/UDP) - 0.0.0.0/0 (TURN/STUN)
- 49152-65535 (UDP) - 0.0.0.0/0 (TURN Relay)
```

---

## 📋 체크리스트

### coturn 서버
- [ ] `use-auth-secret` 설정
- [ ] `static-auth-secret` 설정 (비밀키 설정)
- [ ] `user=myuser:mypassword123` 제거 또는 주석
- [ ] `no-loopback-peers` 제거 (오타 에러 방지)
- [ ] coturn 재시작
- [ ] 로그에서 401 에러 없는지 확인

### 백엔드 서버
- [ ] `.env` 파일에 `TURN_SHARED_SECRET` 설정
- [ ] `TURN_SHARED_SECRET` = coturn `static-auth-secret` (값 일치 확인)
- [ ] 백엔드 서버 재시작
- [ ] TURN 자격증명 API 테스트 (`/api/v1/calls/rtc/turn-credentials`)

### 네트워크
- [ ] AWS Security Group에서 3478 포트 오픈 (TCP/UDP)
- [ ] AWS Security Group에서 49152-65535 포트 오픈 (UDP)
- [ ] 방화벽 규칙 확인

### 테스트
- [ ] TURN 자격증명 API 응답 정상
- [ ] Trickle ICE에서 `srflx` 타입 나옴 (STUN)
- [ ] Trickle ICE에서 `relay` 타입 나옴 (TURN) ← **가장 중요!**
- [ ] coturn 로그에 401 에러 없음

---

## 🎯 최종 확인

모든 설정이 완료되면:

1. **백엔드 서버 재시작**
```bash
# Docker 사용 시
docker-compose restart backend

# 또는 일반 실행
./gradlew bootRun
```

2. **HTML 테스트 페이지로 통화 테스트**
   - 브라우저 2개 열기 (또는 컴퓨터 2대)
   - 각각 다른 계정으로 로그인
   - 통화 요청/수락
   - 비디오/오디오 확인

3. **로그 확인**
```bash
# 백엔드 로그
docker logs -f backend

# coturn 로그
sudo tail -f /var/log/turnserver/turnserver.log
```

---

## 📚 참고 자료

- **coturn 공식 문서**: https://github.com/coturn/coturn/wiki/turnserver
- **REST API 설정**: https://github.com/coturn/coturn/wiki/turnserver#lt-cred-mech
- **Trickle ICE 테스트**: https://webrtc.github.io/samples/src/content/peerconnection/trickle-ice/
- **WebRTC 공식 문서**: https://webrtc.org/

---

## 💡 보안 권장사항

운영 환경에서는 다음을 추가로 설정하세요:

1. **특정 IP만 허용** (선택사항):
```bash
# /etc/turnserver.conf
denied-peer-ip=0.0.0.0-0.255.255.255
denied-peer-ip=10.0.0.0-10.255.255.255
denied-peer-ip=172.16.0.0-172.31.255.255
denied-peer-ip=192.168.0.0-192.168.255.255
allowed-peer-ip=YOUR_CLIENT_IP_RANGE
```

2. **사용량 제한**:
```bash
# 사용자당 대역폭 제한 (bytes/sec)
user-quota=100
total-quota=1000
```

3. **로그 로테이션**:
```bash
# /etc/logrotate.d/turnserver
/var/log/turnserver/*.log {
    daily
    missingok
    rotate 7
    compress
    delaycompress
    notifempty
    create 0640 turnserver turnserver
    sharedscripts
    postrotate
        systemctl reload turnserver > /dev/null 2>&1 || true
    endscript
}
```

4. **강력한 비밀키 사용**:
```bash
# 현재: OngilTurnSecretKey2025!@#$
# 권장: openssl rand -base64 32로 생성한 랜덤 문자열
```
