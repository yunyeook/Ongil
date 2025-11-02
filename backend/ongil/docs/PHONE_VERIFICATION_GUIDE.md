# 전화번호 인증 시스템 가이드

## 📋 개요

전화번호 인증 시스템은 **인증번호 발송 → 인증번호 검증 → 1회용 토큰 발급** 흐름으로 작동합니다.

**현재는 SMS 발송 대신 콘솔에 인증번호를 출력하는 방식**으로 구현되어 있으며,
나중에 Twilio, CoolSMS 등의 실제 SMS 서비스로 쉽게 교체할 수 있습니다.

---

## 🏗️ 구조

```
domain/verification/
├── controller/
│   └── PhoneVerificationController.java       # REST API 엔드포인트
├── service/
│   └── PhoneVerificationService.java          # 비즈니스 로직 (Redis 기반 제한)
├── dto/
│   ├── request/
│   │   ├── SendVerificationRequest.java       # 인증번호 발송 요청
│   │   └── VerifyCodeRequest.java             # 인증번호 검증 요청
│   └── response/
│       └── VerificationResponse.java          # 인증 성공 응답 (토큰 포함)
└── entity/
    └── VerificationGrant.java                 # 토큰 사용 목적 enum

global/util/
├── VerificationCodeGenerator.java             # 랜덤 코드 생성 (재사용 가능)
└── JwtUtil.java                                # JWT 토큰 발급/검증 (통합)
```

---

## 🔐 인증 흐름

### 1️⃣ 인증번호 발송

```http
POST /api/v1/phone-verifications
Content-Type: application/json

{
  "phoneNumber": "01012345678"
}
```

**응답 (201 Created):**
```json
{
  "message": "전화번호 인증 요청이 성공적으로 완료되었습니다.",
  "data": ""
}
```

**콘솔 출력 (개발 환경):**
```
====================================
📱 [SMS 발송 시뮬레이션]
전화번호: 01012345678
인증번호: 839201
유효시간: 3분
====================================
```

---

### 2️⃣ 인증번호 검증 및 토큰 발급

```http
POST /api/v1/phone-verifications/verify
Content-Type: application/json

{
  "phoneNumber": "01012345678",
  "verificationCode": "839201",
  "grants": "SELF"
}
```

**응답 (200 OK):**
```json
{
  "message": "인증이 성공적으로 완료되었습니다.",
  "data": {
    "verified": true,
    "verificationToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**발급된 토큰 내용:**
- `sub`: `"phone-verification"`
- `phoneNumber`: `"01012345678"`
- `grant`: `"SELF"` or `"RELATIONSHIP"`
- `jti`: 고유 토큰 ID (UUID)
- `exp`: 5분 후 만료

---

## ⚙️ 제한 정책 (Redis 기반)

| 제한 항목 | 기준 | 제한 값 | TTL |
|---------|------|--------|-----|
| **전화번호당 요청 횟수** | 동일 전화번호 | 5회 / 1시간 | 1시간 |
| **IP당 요청 횟수** | 동일 IP | 20회 / 1시간 | 1시간 |
| **재요청 최소 간격** | 동일 전화번호 | 1분 | 1분 |
| **인증번호 유효 시간** | Redis TTL | 3분 (180초) | 3분 |
| **인증번호 입력 시도 횟수** | 동일 전화번호 | 5회 | 3분 |

### Redis Key 구조

| Key 형태 | 설명 | TTL |
|---------|------|-----|
| `verify:code:{phoneNumber}` | 발송된 인증번호 | 180초 (3분) |
| `verify:count:{phoneNumber}` | 전화번호 기준 요청 횟수 | 3600초 (1시간) |
| `verify:ip:{ipAddress}` | IP 기준 요청 횟수 | 3600초 (1시간) |
| `verify:last:{phoneNumber}` | 마지막 요청 시간 | 60초 (1분) |
| `verify:attempt:{phoneNumber}` | 인증번호 입력 시도 횟수 | 180초 (3분) |

---

## 🎯 VerificationGrant (토큰 사용 목적)

인증 토큰은 **SELF**와 **RELATIONSHIP** 두 가지 타입으로 제한되어 발급됩니다.

### 🔹 SELF (본인 인증)

**목적**: "이 전화번호가 내 것임을 증명"

**사용 시나리오**:
- ✅ 회원가입 (본인 번호로 계정 생성)
- ✅ 비밀번호 재설정 (본인 확인 후 비번 변경)
- ✅ 전화번호 변경 (새 번호가 본인 것인지 확인)

**흐름**:
1. 사용자가 자신의 전화번호 입력
2. 본인이 받은 인증번호 입력
3. 본인 확인 완료 → `grant: SELF` 토큰 발급

**특징**:
- 인증번호를 받은 사람 = 요청한 사람 = 본인
- 프론트/백엔드 로직 단순화 (회원가입이든 비번 재설정이든 동일한 인증 플로우)

---

### 🔹 RELATIONSHIP (관계 연결 인증)

**목적**: "상대방이 나와의 관계 연결에 동의했음을 증명"

**사용 시나리오**:
- ✅ 보호자-환자 연결 (타인 동의 필요)
- ✅ 가족 구성원 추가 (타인 동의 필요)

**흐름 (보호자-환자 연결 예시)**:
1. 보호자 A가 환자 B의 전화번호 입력
2. **환자 B의 전화번호로 인증번호 전송** (B가 받음)
3. 보호자 A가 B로부터 받은 인증번호 입력
4. B의 동의 증명 완료 → `grant: RELATIONSHIP` 토큰 발급
5. 토큰으로 A-B 관계 연결 API 호출

**특징**:
- 인증번호를 받은 사람 = 연결 대상 (타인)
- 요청한 사람 = 연결 주체 (본인)
- 상대방의 동의를 증명하는 용도

**향후 확장 가능**:
- 토큰에 `actorPhoneNumber`(요청자)와 `targetPhoneNumber`(인증 대상) 모두 포함
- 관계 연결 API에서 이 두 정보로 정확한 매칭 보장

---

### 📊 SELF vs RELATIONSHIP 비교표

| 구분 | SELF | RELATIONSHIP |
|-----|------|--------------|
| **인증 대상** | 본인 | 타인 |
| **인증번호 수신자** | 요청자 본인 | 연결 대상 (타인) |
| **사용 목적** | 본인 확인 | 타인 동의 증명 |
| **토큰 활용** | 회원가입, 비번 재설정, 전화번호 변경 | 보호자-환자 연결 |
| **기본값** | ✅ (생략 시 SELF) | ❌ |

**요청 시 `grants` 필드 생략 시 기본값: `SELF`**

---

## 🚨 에러 응답

### 1. 요청 횟수 초과

```json
{
  "message": "인증번호 요청 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."
}
```
**HTTP 상태 코드:** `429 Too Many Requests`

### 2. 재요청 간격 미달 (1분 이내)

```json
{
  "message": "인증번호 재요청은 1분 후에 가능합니다."
}
```
**HTTP 상태 코드:** `429 Too Many Requests`

### 3. 인증번호 불일치

```json
{
  "message": "인증번호가 올바르지 않습니다."
}
```
**HTTP 상태 코드:** `400 Bad Request`

### 4. 인증번호 만료

```json
{
  "message": "인증번호가 만료되었습니다."
}
```
**HTTP 상태 코드:** `410 Gone`

### 5. 인증 시도 횟수 초과

```json
{
  "message": "인증번호 입력 횟수를 초과했습니다. 다시 요청해주세요."
}
```
**HTTP 상태 코드:** `401 Unauthorized`

### 6. IP 요청 횟수 초과

```json
{
  "message": "요청 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."
}
```
**HTTP 상태 코드:** `429 Too Many Requests`

### 7. 잘못된 전화번호 형식

```json
{
  "message": "입력값이 유효하지 않습니다. 형식을 다시 확인해주세요."
}
```
**HTTP 상태 코드:** `422 Unprocessable Entity`

---

## 🧪 테스트 방법

### Swagger UI 사용
```
http://localhost:8080/swagger-ui.html
→ Phone Verification API
```

### cURL 사용

#### 1. 인증번호 발송
```bash
curl -X POST http://localhost:8080/api/v1/phone-verifications \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "01012345678"
  }'
```

#### 2. 콘솔에서 인증번호 확인
```
====================================
📱 [SMS 발송 시뮬레이션]
전화번호: 01012345678
인증번호: 123456  ← 이 번호를 복사
유효시간: 3분
====================================
```

#### 3. 인증번호 검증 (본인 인증)
```bash
curl -X POST http://localhost:8080/api/v1/phone-verifications/verify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "01012345678",
    "verificationCode": "123456",
    "grants": "SELF"
  }'
```

#### 4. 인증번호 검증 (관계 연결)
```bash
curl -X POST http://localhost:8080/api/v1/phone-verifications/verify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "01098765432",
    "verificationCode": "654321",
    "grants": "RELATIONSHIP"
  }'
```

---

## 🔧 설정 (application.yml)

```yaml
# JWT 설정
jwt:
  secret: ${JWT_SECRET}
  verification:
    expiration: 300000 # 5분 (밀리초)

# Redis 설정
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

### .env 파일 (필수)

```env
# JWT (256비트 이상의 안전한 키 사용)
JWT_SECRET=your-super-secret-key-at-least-256-bits-long

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

---

## 📦 의존성 (build.gradle)

```gradle
// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

// Redis
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

---

## 🚀 실제 SMS 서비스로 전환하기

### 현재 코드 (콘솔 출력)

```java
// PhoneVerificationService.java - sendVerificationCode() 메서드 내

// 9. SMS 발송 (콘솔 출력으로 대체)
log.info("====================================");
log.info("📱 [SMS 발송 시뮬레이션]");
log.info("전화번호: {}", phoneNumber);
log.info("인증번호: {}", code);
log.info("유효시간: 3분");
log.info("====================================");
```

### 실제 SMS 서비스로 변경 (예: CoolSMS)

```java
// SmsService 인터페이스 작성
public interface SmsService {
    void sendSms(String phoneNumber, String message);
}

// CoolSmsService 구현
@Service
@RequiredArgsConstructor
public class CoolSmsService implements SmsService {

    @Value("${coolsms.api-key}")
    private String apiKey;

    @Value("${coolsms.api-secret}")
    private String apiSecret;

    @Override
    public void sendSms(String phoneNumber, String message) {
        // CoolSMS API 호출
        // ... 구현
    }
}

// PhoneVerificationService에서 사용
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final SmsService smsService; // 주입

    public void sendVerificationCode(SendVerificationRequest request, String ipAddress) {
        // ... 기존 로직

        // SMS 발송
        String message = String.format("[Ongil] 인증번호: %s (3분간 유효)", code);
        smsService.sendSms(phoneNumber, message);
    }
}
```

---

## 📝 사용 예시

### 예시 1: 회원가입 (SELF)

#### 1. 사용자가 "회원가입" 클릭 → 전화번호 입력
#### 2. 프론트엔드 → 백엔드
```javascript
POST /api/v1/phone-verifications
{
  "phoneNumber": "01012345678"
}
```

#### 3. 사용자가 인증번호 입력 → 프론트엔드 → 백엔드
```javascript
POST /api/v1/phone-verifications/verify
{
  "phoneNumber": "01012345678",
  "verificationCode": "123456",
  "grants": "SELF"
}
```

#### 4. 응답으로 받은 `verificationToken` 사용
```javascript
POST /api/v1/auth/signup
Authorization: Bearer {verificationToken}
{
  "name": "홍길동",
  "password": "password123!",
  "userType": "CAREGIVER"
}
```

---

### 예시 2: 보호자-환자 연결 (RELATIONSHIP)

#### 1. 보호자 A가 "환자 추가" → 환자 B의 전화번호 입력
#### 2. 프론트엔드 → 백엔드 (B의 번호로 인증번호 발송)
```javascript
POST /api/v1/phone-verifications
{
  "phoneNumber": "01098765432"  // 환자 B의 번호
}
```

#### 3. 보호자 A가 환자 B로부터 받은 인증번호 입력
```javascript
POST /api/v1/phone-verifications/verify
{
  "phoneNumber": "01098765432",  // 환자 B의 번호
  "verificationCode": "654321",
  "grants": "RELATIONSHIP"
}
```

#### 4. 응답으로 받은 `verificationToken`으로 관계 연결
```javascript
POST /api/v1/relationships
Authorization: Bearer {verificationToken}
{
  "patientPhoneNumber": "01098765432",
  "patientName": "김영희",
  "relationship": "부모"
}
```

---

## ✅ 체크리스트

- [x] ErrorCode에 인증 관련 에러 코드 추가
- [x] ResponseMessage에 성공 메시지 추가
- [x] VerificationGrant enum SELF/RELATIONSHIP로 단순화
- [x] VerificationCodeGenerator를 global/util로 이동 (재사용 가능)
- [x] VerificationTokenProvider를 JwtUtil에 통합
- [x] PhoneVerificationService 작성 (Redis 기반 제한)
- [x] PhoneVerificationController 작성
- [x] application.yml 설정 추가
- [x] build.gradle 의존성 추가
- [x] Swagger 문서 자동 생성

---

## 🔒 보안 고려사항

1. **JWT Secret Key는 최소 256비트 이상 사용** (환경변수로 관리)
2. **Redis는 프로덕션에서 비밀번호 설정 필수**
3. **IP 기반 제한으로 대량 요청 공격 방어**
4. **인증번호는 3분 후 자동 만료**
5. **인증 성공 시 즉시 Redis에서 삭제**
6. **1회용 토큰은 5분 후 만료**
7. **SELF와 RELATIONSHIP 토큰은 용도별로 명확히 구분**

---

## 📚 참고

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API 문서: `http://localhost:8080/v3/api-docs`
- JwtUtil: `global/util/JwtUtil.java` (Access/Refresh Token도 여기서 관리 예정)
- VerificationCodeGenerator: `domain/verification/util/VerificationCodeGenerator.java` (범용 코드 생성기)
