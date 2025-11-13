# 전화번호 인증 시스템 가이드

## 📋 개요

전화번호 인증 시스템은 **인증번호 발송 → 인증번호 검증 → 1회용 토큰 발급** 흐름으로 작동합니다.

현재 구현은 다음을 포함합니다:

* 인증번호를 생성하고 Redis에 저장합니다. (TTL 3분)
* CoolSMS API를 통해 실제로 SMS를 발송합니다.
* 사용자가 입력한 인증번호를 검증하면, 목적 제한 1회용 토큰(verificationToken, JWT)을 발급합니다.
* 발급된 토큰은 5분 동안만 유효하며, 회원가입 등 특정 목적(grant)에만 사용할 수 있습니다.

※ SMS 발송은 `SmsService` 인터페이스로 추상화되어 있어, 추후 카카오 알림톡 등 다른 채널로 교체 가능합니다.

---

## 🏗️ 구조

```text
domain/verification/
├── controller/
│   └── PhoneVerificationController.java       # REST API 엔드포인트
├── service/
│   └── PhoneVerificationService.java          # 비즈니스 로직 (Redis 기반 제한 + 토큰 발급)
├── dto/
│   ├── request/
│   │   ├── SendVerificationRequest.java       # 인증번호 발송 요청
│   │   └── VerifyCodeRequest.java             # 인증번호 검증 요청
│   └── response/
│       └── VerificationResponse.java          # 인증 성공 응답 (토큰 포함)
└── entity/
    └── VerificationGrant.java                 # 토큰 사용 목적 enum (SELF / RELATIONSHIP)

global/sms/
├── SmsService.java                            # SMS 전송 추상화 인터페이스
└── CoolSmsService.java                        # CoolSMS SDK 기반 실제 문자 발송 구현

global/util/
├── VerificationCodeGenerator.java             # 6자리 랜덤 인증번호 생성
└── JwtUtil.java                               # verificationToken(JWT) 발급/검증
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

발송되는 실제 SMS 내용 예시:

```
[온길] 인증번호는 [839201]입니다.
인증번호를 입력해주세요.
유효시간: 3분
```

* 인증번호는 6자리 숫자로 생성됩니다.
* Redis에 3분간 유효한 상태로 저장됩니다.
* CoolSMS API를 통해 `coolsms.from`에 설정된 발신 번호로 전송됩니다.
* 동일한 전화번호/동일한 IP에 대해 요청 빈도 제한이 걸립니다 (아래 제한 정책 참고).

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

검증 성공 시 처리 흐름:

1. Redis에서 해당 번호에 저장된 코드(`verify:code:{phone}`)를 조회합니다.
2. 코드가 만료되었거나 없으면 실패 처리합니다.
3. 코드가 다르면 실패 시도 횟수를 증가시키고 차단 정책에 반영합니다.
4. 코드가 일치하면:

   * Redis에서 즉시 삭제 (1회성 인증번호)
   * 실패 시도 카운터 초기화
   * 전화번호와 grant 정보를 담은 `verificationToken`(JWT) 발급

**발급된 토큰 내용(JWT claim):**

* `sub`: `"phone-verification"`
* `phoneNumber`: `"01012345678"`
* `grant`: `"SELF"` 또는 `"RELATIONSHIP"`
* `jti`: 고유 토큰 ID (UUID)
* `exp`: 현재 시각 기준 5분 후 만료

이 토큰은 이후 회원가입/연결 API에서 "이 번호/이 관계가 검증된 상태인지"를 증명하는 용도로만 사용됩니다.

---

## ⚙️ 제한 정책 (Redis 기반)

다음은 악용/도배를 막기 위한 보호 정책입니다.

| 제한 항목             | 기준          | 제한 값         | TTL              |
| ----------------- | ----------- | ------------ | ---------------- |
| **전화번호당 요청 횟수**   | 동일 전화번호     | 5회 / 1시간     | 1시간 유지           |
| **IP당 요청 횟수**     | 동일 IP       | 20회 / 1시간    | 1시간 유지           |
| **재요청 최소 간격**     | 동일 전화번호     | 1분 이내 재요청 불가 | 1분 유지            |
| **인증번호 유효 시간**    | 인증번호 자체 TTL | 3분 (180초)    | 3분 후 자동 만료       |
| **인증번호 입력 시도 횟수** | 동일 전화번호     | 최대 5회        | 인증번호 TTL과 동일(3분) |

### Redis Key 구조

| Key 형태                         | 설명                       | TTL               |
| ------------------------------ | ------------------------ | ----------------- |
| `verify:code:{phoneNumber}`    | 해당 번호로 발송된 6자리 인증번호      | 180초 (3분)         |
| `verify:count:{phoneNumber}`   | 전화번호 기준 요청 횟수 카운터        | 3600초 (1시간)       |
| `verify:ip:{ipAddress}`        | IP 기준 요청 횟수 카운터          | 3600초 (1시간)       |
| `verify:last:{phoneNumber}`    | 마지막 인증번호 요청 타임스탬프(ms 단위) | 60초 (1분)          |
| `verify:attempt:{phoneNumber}` | 잘못된 인증번호 입력 시도 횟수        | 180초 (3분, 코드 TTL) |

이 값들은 `PhoneVerificationService` 내에서 RedisTemplate으로 관리됩니다.

---

## 🎯 VerificationGrant (토큰 사용 목적)

인증 토큰은 `SELF`와 `RELATIONSHIP` 두 가지 목적 중 하나로 발급됩니다.
요청 바디의 `grants` 필드로 전달되며, 비어있거나 잘못된 값이면 기본적으로 `SELF`로 처리됩니다.

### 🔹 SELF (본인 인증)

**목적**: "이 전화번호가 내 것임을 증명"

**사용 시나리오**:

* ✅ 회원가입 (해당 번호를 내 계정으로 등록)
* ✅ 비밀번호 재설정 (본인임을 증명하고 비밀번호 변경 허용)
* ✅ 전화번호 변경 (새 번호가 본인 번호인지 확인)

**흐름**:

1. 사용자가 자신의 휴대전화 번호를 입력
2. 해당 번호로 온 SMS 인증번호를 직접 입력
3. 성공 시 `grant: SELF` 토큰 발급

**특징**:

* 인증번호를 받은 사람 = 요청한 사람 = 본인
* 패스워드 찾기, 회원가입 등 “본인 확인” 플로우에 공통으로 재사용 가능

---

### 🔹 RELATIONSHIP (관계 연결 인증)

**목적**: "상대방이 나와의 연결(보호자-환자 관계 등)을 동의했다는 증명"

**사용 시나리오**:

* ✅ 보호자-환자 연결
* ✅ 가족 구성원 등록/초대

**흐름 (보호자-환자 예시)**:

1. 보호자 A가 환자 B의 전화번호를 입력해 인증번호 발송 요청
2. 인증번호는 환자 B의 번호로 전송됨
3. 보호자 A가 B에게 받은 인증번호를 앱에 입력
4. 인증 성공 → `grant: RELATIONSHIP` 토큰 발급
5. 이후 이 토큰으로 “보호자 A ↔ 환자 B 연결” API를 호출해 관계를 생성

**특징**:

* 인증번호를 받은 사람 ≠ 요청자일 수 있음
  (B가 받은 번호를 A가 입력함)
* 즉, “상대방이 동의했다”는 증거로 쓰임

**향후 확장 가능**:

* 토큰 Payload에 `actor`(요청자), `target`(인증 대상) 번호를 모두 담는 식으로 확장 가능
  → 관계 연결 API에서 위조 없이 연결 여부를 판별 가능

---

### 📊 SELF vs RELATIONSHIP 비교표

| 구분           | SELF                  | RELATIONSHIP          |
| ------------ | --------------------- | --------------------- |
| **인증 대상**    | 본인                    | 타인                    |
| **인증번호 수신자** | 요청자 본인                | 연결 대상자(예: 환자)         |
| **사용 목적**    | 본인 확인                 | 타인 동의 증명              |
| **주요 활용**    | 회원가입 / 비번 재설정 / 번호 변경 | 보호자-환자 연결 / 가족 구성원 연결 |
| **기본값 여부**   | ✅ 기본값                 | ❌ 명시적으로 요청해야 함        |

`grants` 필드를 요청에서 생략하면 자동으로 `SELF`로 처리됩니다.

---

## 🚨 에러 응답

아래 예시는 `ErrorCode` 기반 공통 에러 응답 메시지입니다.

### 1. 요청 횟수 초과 (전화번호 또는 IP 기준)

```json
{
  "message": "인증번호 요청 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."
}
```

**HTTP 상태 코드:** `429 Too Many Requests`

### 2. 재요청 간격 미달 (1분 이내 재요청)

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

### 4. 인증번호 만료 / 없음

```json
{
  "message": "인증번호가 만료되었습니다."
}
```

**HTTP 상태 코드:** `410 Gone`

### 5. 인증 시도 횟수 초과 (잘못된 코드 계속 입력)

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

### 8. SMS 전송 실패 (CoolSMS 에러 등)

```json
{
  "message": "SMS 전송 중 오류가 발생했습니다."
}
```

**HTTP 상태 코드:** `500 Internal Server Error`

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

#### 2. 인증번호 검증 (SELF: 본인 인증)

```bash
curl -X POST http://localhost:8080/api/v1/phone-verifications/verify \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "01012345678",
    "verificationCode": "123456",
    "grants": "SELF"
  }'
```

#### 3. 인증번호 검증 (RELATIONSHIP: 동의 인증)

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

# CoolSMS 설정
coolsms:
  api:
    key: ${COOLSMS_API_KEY}        # CoolSMS API KEY
    secret: ${COOLSMS_API_SECRET}  # CoolSMS API SECRET
  from: ${COOLSMS_FROM_NUMBER}     # 발신번호 (010xxxxxxxx 형태)
  domain: https://api.coolsms.co.kr
```

### .env 예시

```env
# JWT (256비트 이상의 안전한 키 사용)
JWT_SECRET=your-super-secret-key-at-least-256-bits-long

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# CoolSMS
COOLSMS_API_KEY=xxxxxxx
COOLSMS_API_SECRET=yyyyyyy
COOLSMS_FROM_NUMBER=01012345678
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

// CoolSMS SDK
implementation 'net.nurigo:sdk:4.3.2'
```

---

## 🚀 SMS 전송 구조 (CoolSMS)

### 1. `SmsService` 인터페이스

```java
public interface SmsService {
    void sendVerificationCode(String phoneNumber, String verificationCode);
    void sendMessage(String phoneNumber, String messageText);
}
```

### 2. `CoolSmsService` 구현

```java
@Slf4j
@Service
public class CoolSmsService implements SmsService {

    private final DefaultMessageService messageService;
    private final String fromNumber;

    public CoolSmsService(
        @Value("${coolsms.api.key}") String apiKey,
        @Value("${coolsms.api.secret}") String apiSecret,
        @Value("${coolsms.from}") String fromNumber,
        @Value("${coolsms.domain}") String domain
    ) {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, domain);
        this.fromNumber = fromNumber;
        log.info("CoolSMS 서비스 초기화 완료 (발신번호: {})", fromNumber);
    }

    @Override
    public void sendVerificationCode(String phoneNumber, String verificationCode) {
        String messageText = String.format(
            "[온길] 인증번호는 [%s]입니다.\n인증번호를 입력해주세요.\n유효시간: 3분",
            verificationCode
        );
        sendMessage(phoneNumber, messageText);
        log.info("인증번호 SMS 발송 완료: phoneNumber={}, code={}", phoneNumber, verificationCode);
    }

    @Override
    public void sendMessage(String phoneNumber, String messageText) {
        try {
            Message message = new Message();
            message.setFrom(fromNumber);
            message.setTo(phoneNumber);
            message.setText(messageText);

            messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("SMS 발송 성공: to={}, from={}", phoneNumber, fromNumber);

        } catch (Exception e) {
            log.error("SMS 발송 실패: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }
    }
}
```

### 3. `PhoneVerificationService`에서 사용

```java
@RequiredArgsConstructor
@Service
public class PhoneVerificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final SmsService smsService;

    public void sendVerificationCode(SendVerificationRequest request, String ipAddress) {
        String phoneNumber = request.phoneNumber();

        // 1. rate limit (IP, phone, 재요청 간격 등) 검사
        // 2. 인증번호 생성 (6자리)
        String code = VerificationCodeGenerator.generate();

        // 3. Redis에 인증번호 저장 (TTL 3분)
        redisTemplate
            .opsForValue()
            .set("verify:code:" + phoneNumber, code, Duration.ofSeconds(180));

        // 4. 카운터/쿨타임 갱신 (요청 횟수, last request 등)
        // ...

        // 5. SMS 발송 (CoolSMS)
        smsService.sendVerificationCode(phoneNumber, code);

        log.info("인증번호 발송 완료: phoneNumber={}", phoneNumber);
    }

    // verifyCode(...)에서는
    // - Redis에서 코드 비교
    // - 시도횟수 초과 여부 확인
    // - 성공 시 verificationToken(JWT) 발급
}
```

---

## 📝 사용 예시

### 예시 1: 회원가입 (SELF)

1. 사용자가 "회원가입" 화면에서 휴대전화 번호 입력
2. 프론트엔드 → 백엔드

```http
POST /api/v1/phone-verifications
{
  "phoneNumber": "01012345678"
}
```

3. 사용자가 문자로 받은 인증번호 입력 → 프론트엔드 → 백엔드

```http
POST /api/v1/phone-verifications/verify
{
  "phoneNumber": "01012345678",
  "verificationCode": "123456",
  "grants": "SELF"
}
```

4. 백엔드는 `verificationToken`을 내려줌
5. 이후 회원가입 API 호출 시 이 토큰을 사용해 본인인증을 증명

```http
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

1. 보호자 A가 앱에서 “환자 추가”
2. 보호자 A가 환자 B의 전화번호를 입력 → 백엔드가 해당 번호로 인증번호 전송

```http
POST /api/v1/phone-verifications
{
  "phoneNumber": "01098765432"  // 환자 B의 번호
}
```

3. 보호자 A는 B에게 받은 인증번호를 입력해서 검증

```http
POST /api/v1/phone-verifications/verify
{
  "phoneNumber": "01098765432",
  "verificationCode": "654321",
  "grants": "RELATIONSHIP"
}
```

4. 백엔드가 `grant: RELATIONSHIP` 토큰 발급
5. 그 토큰을 들고 관계 연결 API를 호출

```http
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

* [x] `PhoneVerificationController` 구현
* [x] `PhoneVerificationService` 구현 (Redis + JWT + 레이트 리밋)
* [x] `SmsService` 인터페이스 및 `CoolSmsService` 구현
* [x] Redis 제한 정책 적용 (IP/번호별 요청 횟수, 쿨타임, 시도 제한)
* [x] 인증 성공 시 verificationToken(JWT) 발급
* [x] `VerificationGrant` (SELF / RELATIONSHIP) 도입
* [x] `ErrorCode`에 SMS 전송 실패 등 예외 케이스 추가
* [x] application.yml에 `coolsms.*` 설정 추가
* [x] build.gradle에 `net.nurigo:sdk` 의존성 추가
* [x] Swagger에서 직접 호출 가능하도록 API 문서화

---

## 🔒 보안 고려사항

1. **JWT Secret Key는 최소 256비트 이상**이어야 하며, `application.yml`에 직접 쓰지 말고 환경 변수(.env 등)로 주입합니다.
2. **Redis는 운영 환경에서 반드시 비밀번호/보안 그룹 설정**이 필요합니다.
3. **IP 및 전화번호별 rate limit**으로 무차별 시도(스팸 발송, 무작위 번호 공격)를 방어합니다.
4. **인증번호는 Redis TTL(3분)으로 자동 만료**되며, 검증 성공 시 즉시 삭제합니다.
5. **verificationToken은 만료시간이 5분인 1회용 토큰**으로만 사용합니다.
6. `RELATIONSHIP` 토큰은 “상대방이 동의했다”는 근거이므로, 권한 없는 관계 등록 시도를 백엔드에서 차단하는 근거로 사용됩니다.
7. `SMS_SEND_FAILED` 등의 서버 에러는 클라이언트에 그대로 노출되지 않도록 공통 에러 응답 형태로 내려갑니다.

---

## 📚 참고

* Swagger UI: `http://localhost:8080/swagger-ui.html`
* OpenAPI 문서: `http://localhost:8080/v3/api-docs`
* `PhoneVerificationController`: `/api/v1/phone-verifications`, `/verify`
* `PhoneVerificationService`: Redis 제어, CoolSMS 호출, verificationToken 발급
* `CoolSmsService`: CoolSMS SDK 초기화 및 실제 문자 전송
* `JwtUtil`: verificationToken(1회용 목적 제한 토큰) 발급 담당
* `VerificationCodeGenerator`: 6자리 인증번호 생성 유틸 (공통 재사용 가능)
