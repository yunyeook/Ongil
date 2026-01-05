# 온길 (OnGil)

> 어르신 배회 감지 및 대응 서비스

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android%2011+-brightgreen.svg)](https://www.android.com/)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%203.5.7-green.svg)](https://spring.io/projects/spring-boot)



---

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [핵심 기능](#-핵심-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [프로젝트 구조](#-프로젝트-구조)
- [설치 및 실행](#-설치-및-실행)
- [주요 기술적 해결 과제](#-주요-기술적-해결-과제)
- [기대 효과](#-기대-효과)
- [향후 확장 계획](#-향후-확장-계획)

---

## 🎯 프로젝트 개요

**온길(OnGil)** 은 실시간 위치와 생체 데이터를 바탕으로 길안내, 신속한 대응부터 건강분석까지 제공하는 어르신 배회 감지 및 대응 서비스입니다.

<img width="187" height="382" alt="ongil2" src="https://github.com/user-attachments/assets/d9d6372e-487c-4dd7-a658-21a8badaad86" /><img width="187" height="382" alt="ongil" src="https://github.com/user-attachments/assets/a9269ad5-f78f-4cf2-939b-3e2454211508" /><img width="187" height="382" alt="ongil4" src="https://github.com/user-attachments/assets/2676cd06-f1cd-4b06-aa66-13d2dd7711ed" />
<img width="187" height="382" alt="ongil3" src="https://github.com/user-attachments/assets/0ea47ab1-078b-4bba-bf6f-3d9981267257" />

### 목표

온길은 실시간 위치 공유, 안전구역 관리, 이상 징후 알림, VoIP 통화 기능을 통해 보호자와 환자 간의 정보 단절 문제를 해결하고, 돌봄의 부담을 실질적으로 낮추는 것을 목표로 합니다.

**"조금 더 안심할 수 있는 하루"를 만드는 기술 솔루션입니다.**

---

## ⭐ 핵심 기능

### 1. 실시간 위치 기반 안전 관리

- **3단계 정확도 필터링**: GPS 좌표, 정확도(accuracy), 속도값을 활용한 고도화된 위치 검증 로직
- **스파이크 제거**: 부정확한 위치 튐 현상(위치 점프) 자동 감지 및 제거
- **속도 기반 검증**: 예상 이동거리와 실제 좌표 변화를 비교하여 신뢰도 검증
- **정지 상태 최적화**: 속도 0인 상태에서 흔들림 방지 처리
- **SSE 기반 실시간 스트리밍**: Server-Sent Events를 통한 보호자 앱으로의 실시간 위치 전송

### 2. 안전구역(SafeZone) 설정

- **다단계 범위 설정**: 반경 100m / 350m / 700m 선택 가능
- **지도 기반 UI**: 안전구역 범위 시각화 및 직관적 관리
- **진입/이탈 알림**: 안전구역 경계 이벤트 발생 시 보호자에게 즉시 푸시 알림
- **생활 반경 기반 설계**: 노인이 자주 오가는 생활 반경을 중심으로 한 안전 관리

### 3. 즐겨찾기 장소 관리

- **장소 별칭(alias) 관리**: "우리집", "병원", "마트" 등 사용자 지정 이름으로 장소 표시
- **자동 fallback**: alias가 없는 경우 원본 장소명으로 자동 표시
- **실시간 동기화**: 장소 상세 수정 시 목록과 실시간 동기화
- **백엔드 연동**: Repository-DTO 구조 기반 RESTful API 통신

### 4. VoIP 기반 실시간 통화

- **통화 상태 로깅**: 일반 전화로는 불가능한 연결/종료/부재중/거절 등 세부 상태 수집
- **WebSocket(STOMP) 신호 전송**: FCM → 앱 → WebSocket의 3단계 호출 신호 흐름
- **앱 내 UI 완전 제어**: 수신 알림, 벨 울림, 통화 화면 모두 네이티브 UI로 구성
- **긴급 상황 대응**: 보호자-환자 간 즉시 연결 가능한 긴급 통화 기능

### 5. 대시보드 및 주요 지표

- **활동 패턴 분석**: 이동량, 수면, 활동 패턴 기반 위험도 요약
- **시각화**: Bar graph 및 색상 구분 시스템을 통한 직관적 위험도 표시
- **AI 기반 인사이트**: 백엔드 API `/insights` 연동을 통한 AI 분석 결과 제공
- **맞춤형 카드 UI**: 가독성을 고려한 색상, 배경, 레이아웃 설계

### 6. FCM 기반 실시간 알림

- **이상 상황 즉시 알림**: 환자의 이상 행동 감지 시 보호자에게 푸시 알림
- **다채널 알림 지원**: VoIP 수신, 위치 이상, SafeZone 이탈 등 다양한 알림 유형
- **인증 흐름 안정화**: 404/401 문제 해결을 통한 FCM 토큰 등록 최적화

---

## 🛠 기술 스택

### Android (보호자/환자 앱)

| 구분 | 기술 스택 |
|------|----------|
| **언어** | Kotlin |
| **UI** | Jetpack Compose, Material3 |
| **아키텍처** | MVVM + Clean Architecture (Presentation / Domain / Data) |
| **의존성 주입** | Hilt (Dagger) |
| **네트워킹** | Retrofit2, OkHttp3, SSE (Server-Sent Events) |
| **실시간 통신** | WebSocket (STOMP), WebRTC |
| **이미지 로딩** | Coil |
| **지도** | Tmap SDK |
| **로컬 저장소** | DataStore Preferences, Room Database |
| **알림** | Firebase Cloud Messaging (FCM) |
| **건강 데이터** | Samsung Health Connect SDK |
| **비동기 처리** | Kotlin Coroutines, Flow |
| **최소 SDK** | Android 11 (API 30) |
| **타겟 SDK** | Android 15 (API 35) |
| **컴파일 SDK** | API 36 |

### Backend (Spring Boot)

| 구분 | 기술 스택 |
|------|----------|
| **언어** | Java 21 |
| **프레임워크** | Spring Boot 3.5.7 |
| **보안** | Spring Security, JWT |
| **데이터베이스** | PostgreSQL (AWS RDS) |
| **캐시** | Redis (AWS ElastiCache) |
| **ORM** | Spring Data JPA, Hibernate |
| **실시간 통신** | WebSocket, RabbitMQ (STOMP) |
| **파일 저장소** | AWS S3 |
| **외부 API** | Tmap API (지도/경로), CoolSMS (SMS 인증) |
| **VoIP 인프라** | TURN/STUN Server (coturn) |
| **API 문서화** | Swagger/OpenAPI 3.0 |
| **빌드 도구** | Gradle 8.x |

### 인프라 및 DevOps

| 구분 | 기술 스택 |
|------|----------|
| **클라우드** | AWS (EC2, RDS, ElastiCache, S3) |
| **컨테이너** | Docker, Docker Swarm (3-node cluster) |
| **로드 밸런서** | Traefik (Reverse Proxy + SSL) |
| **CI/CD** | Jenkins, GitLab CI/CD |
| **모니터링** | Prometheus, Grafana, Loki |
| **메트릭 수집** | cAdvisor, Node Exporter |
| **SSL/TLS** | Let's Encrypt |
| **메시지 브로커** | RabbitMQ |

---

## 🏗 시스템 아키텍처

### 전체 아키텍처

```
┌─────────────────┐         ┌─────────────────┐
│  보호자 앱       │         │   환자 앱        │
│  (Android)      │         │  (Android)      │
└────────┬────────┘         └────────┬────────┘
         │                           │
         │        FCM Push           │
         ├───────────────────────────┤
         │                           │
         │   HTTPS/WebSocket/SSE     │
         └──────────┬────────────────┘
                    │
         ┌──────────▼──────────┐
         │   Traefik Proxy     │ (SSL Termination)
         │  (Load Balancer)    │
         └──────────┬──────────┘
                    │
         ┌──────────▼──────────┐
         │   Docker Swarm      │
         │   (3 nodes)         │
         │  - Spring Boot API  │
         │  - RabbitMQ         │
         │  - TURN/STUN        │
         └──────────┬──────────┘
                    │
    ┌───────────────┼───────────────┐
    │               │               │
┌───▼───┐      ┌────▼────┐     ┌───▼───┐
│ RDS   │      │ Redis   │     │  S3   │
│(PSQL) │      │(Cache)  │     │(File) │
└───────┘      └─────────┘     └───────┘
```

### Android 앱 아키텍처 (Clean Architecture)

```
┌──────────────────────────────────────────────────┐
│            Presentation Layer                    │
│  - UI (Jetpack Compose)                          │
│  - ViewModel (StateFlow/SharedFlow)              │
│  - Navigation                                    │
└─────────────────┬────────────────────────────────┘
                  │
┌─────────────────▼────────────────────────────────┐
│            Domain Layer                          │
│  - Use Cases (Business Logic)                    │
│  - Domain Models                                 │
│  - Repository Interfaces                         │
└─────────────────┬────────────────────────────────┘
                  │
┌─────────────────▼────────────────────────────────┐
│            Data Layer                            │
│  - Repository Implementations                    │
│  - Data Sources (Remote API, Local DB, WebSocket)│
│  - DTOs & Mappers                                │
└──────────────────────────────────────────────────┘
```

### Backend 아키텍처

```
┌──────────────────────────────────────────────────┐
│            Controller Layer                      │
│  - REST API Endpoints                            │
│  - WebSocket Handlers                            │
│  - Request Validation                            │
└─────────────────┬────────────────────────────────┘
                  │
┌─────────────────▼────────────────────────────────┐
│            Service Layer                         │
│  - Business Logic                                │
│  - Transaction Management                        │
│  - External API Integration (Tmap, etc.)         │
└─────────────────┬────────────────────────────────┘
                  │
┌─────────────────▼────────────────────────────────┐
│        Repository & Entity Layer                 │
│  - JPA Repositories                              │
│  - Domain Entities                               │
│  - Database Access                               │
└──────────────────────────────────────────────────┘
```

---

## 📁 프로젝트 구조

### Android 프로젝트 구조

```
android/ongil/
├── app/                                    # 메인 애플리케이션 모듈
│   └── src/main/java/kr/co/ongil/
│       ├── OngilApplication.kt             # 애플리케이션 진입점
│       │
│       ├── core/                           # 핵심 유틸리티
│       │   ├── constants/                  # 상수 정의
│       │   ├── extensions/                 # Kotlin 확장 함수
│       │   ├── utils/                      # 유틸리티 클래스
│       │   └── webrtc/                     # WebRTC 클라이언트
│       │
│       ├── data/                           # Data Layer
│       │   ├── datasource/
│       │   │   ├── local/                  # 로컬 데이터 소스
│       │   │   │   ├── database/           # Room Database
│       │   │   │   └── preferences/        # DataStore
│       │   │   ├── remote/                 # 원격 데이터 소스
│       │   │   │   ├── api/                # Retrofit API 인터페이스
│       │   │   │   └── interceptor/        # 인증 인터셉터
│       │   │   ├── wear/                   # Wear OS 통신
│       │   │   └── websocket/              # WebSocket 통신
│       │   ├── mapper/                     # DTO ↔ Domain 변환
│       │   ├── model/                      # DTO 모델
│       │   │   ├── auth/
│       │   │   ├── call/
│       │   │   ├── dashboard/
│       │   │   ├── favorite/
│       │   │   ├── health/
│       │   │   ├── insight/
│       │   │   ├── location/
│       │   │   ├── map/
│       │   │   └── notification/
│       │   └── repository/                 # Repository 구현체
│       │
│       ├── domain/                         # Domain Layer
│       │   ├── model/                      # Domain 모델
│       │   ├── repository/                 # Repository 인터페이스
│       │   └── usecase/                    # Use Case
│       │
│       └── presentation/                   # Presentation Layer
│           ├── ui/
│           │   ├── auth/                   # 인증 화면
│           │   ├── call/                   # 통화 화면
│           │   ├── dashboard/              # 대시보드
│           │   ├── favorite/               # 즐겨찾기
│           │   ├── location/               # 위치 화면
│           │   ├── map/                    # 지도 화면
│           │   └── settings/               # 설정 화면
│           ├── viewmodel/                  # ViewModel
│           └── navigation/                 # 네비게이션 그래프
│
├── common/                                 # 공통 모듈 (앱-워치 간 공유)
└── wear/                                   # Wear OS 앱 모듈
```

### Backend 프로젝트 구조

```
backend/ongil/
└── src/main/java/kr/co/ongil/
    ├── domain/                             # 비즈니스 도메인
    │   ├── auth/                           # 인증/인가
    │   │   ├── controller/
    │   │   ├── service/
    │   │   └── dto/
    │   ├── call/                           # 통화
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── entity/
    │   │   └── dto/
    │   ├── map/                            # 지도/위치
    │   │   ├── controller/
    │   │   ├── service/
    │   │   │   ├── MapService.java
    │   │   │   └── TmapService.java
    │   │   └── dto/
    │   ├── notification/                   # 알림
    │   ├── patient/                        # 환자 관리
    │   │   ├── dashboard/                  # 대시보드
    │   │   ├── health/                     # 건강 데이터
    │   │   ├── insight/                    # AI 인사이트
    │   │   ├── abnormal/                   # 이상행동 탐지
    │   │   ├── favorite/                   # 즐겨찾기 장소
    │   │   ├── location/                   # 위치 추적
    │   │   └── safezone/                   # 안전구역
    │   ├── relationship/                   # 보호자-환자 관계
    │   └── user/                           # 사용자 관리
    │
    └── global/                             # 공통 레이어
        ├── common/
        │   ├── entity/BaseEntity.java      # 공통 엔티티
        │   └── response/ApiResponse.java   # 표준 응답 래퍼
        ├── config/                         # 설정
        │   ├── SecurityConfig.java
        │   ├── SwaggerConfig.java
        │   └── WebMvcConfig.java
        ├── exception/                      # 예외 처리
        │   ├── ErrorCode.java
        │   ├── BusinessException.java
        │   └── GlobalExceptionHandler.java
        ├── util/                           # 유틸리티
        └── sse/                            # SSE 구현
```

---

## 🚀 설치 및 실행

### 사전 요구사항

#### Android 개발 환경
- Android Studio Ladybug | 2024.2.1 이상
- JDK 17 이상
- Android SDK 30-36
- Gradle 8.14.3

#### Backend 개발 환경
- JDK 21
- Gradle 8.x
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

### Android 앱 설치

1. **프로젝트 클론**
   ```bash
   git clone https://lab.ssafy.com/s13-final/S13P31A305.git
   cd S13P31A305/android/ongil
   ```

2. **환경 변수 설정**

   `local.properties` 파일을 생성하고 다음 내용을 추가합니다:
   ```properties
   BASE_URL=https://staging.on-gil.co.kr/
   SSE_URL=https://staging.on-gil.co.kr/api/v1/location/stream
   TMAP_API_KEY=your-tmap-api-key
   ```

3. **빌드 및 실행**
   ```bash
   ./gradlew assembleDebug
   ```

   또는 Android Studio에서 직접 실행:
   - `Run > Run 'app'` (Shift + F10)

### Backend 서버 실행

1. **프로젝트 클론**
   ```bash
   git clone https://lab.ssafy.com/s13-final/S13P31A305.git
   cd S13P31A305/backend/ongil
   ```

2. **환경 변수 설정**

   `.env.dev` 또는 `.env.prod` 파일을 생성합니다:
   ```env
   # Database
   DB_HOST=your-db-host
   DB_PORT=5432
   DB_NAME=ongil
   DB_USERNAME=postgres
   DB_PASSWORD=your-password

   # Redis
   REDIS_HOST=your-redis-host
   REDIS_PORT=6379

   # AWS
   AWS_REGION=ap-northeast-2
   AWS_ACCESS_KEY=your-access-key
   AWS_SECRET_KEY=your-secret-key
   S3_BUCKET=your-bucket-name

   # JWT
   JWT_SECRET=your-jwt-secret-key
   JWT_ACCESS_TOKEN_EXPIRATION=86400000
   JWT_REFRESH_TOKEN_EXPIRATION=259200000

   # Tmap
   TMAP_APP_KEY=your-tmap-key

   # TURN/STUN
   TURN_SERVER_HOST=turn.on-gil.co.kr
   TURN_SERVER_PORT=3478
   TURN_SHARED_SECRET=your-turn-secret

   # RabbitMQ
   RABBITMQ_HOST=localhost
   RABBITMQ_PORT=5672
   RABBITMQ_USERNAME=admin
   RABBITMQ_PASSWORD=password
   ```

3. **Docker Compose로 실행 (개발 환경)**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

4. **직접 실행 (로컬 개발)**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

5. **API 문서 확인**
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/v3/api-docs`

### 운영 환경 배포

운영 환경 배포에 대한 상세 가이드는 [`exec/A305_온길_포팅메뉴얼.md`](exec/A305_온길_포팅메뉴얼.md)를 참조하세요.

주요 배포 구성:
- **Docker Swarm 클러스터** (3-node)
- **Traefik** 리버스 프록시 (자동 SSL/TLS)
- **Jenkins** CI/CD 파이프라인
- **Prometheus + Grafana** 모니터링

---

## 🔧 주요 기술적 해결 과제

### 1. GPS 위치 정확도 문제

**문제**: GPS 센서의 부정확한 좌표로 인한 위치 튐 현상(스파이크) 발생

**해결 방안**:
- **3단계 정확도 필터링**: GPS accuracy 값, 속도, 좌표 변화를 종합 분석
- **Kalman Filter 적용**: 노이즈 제거 및 위치 평활화
- **속도 기반 검증**: 예상 이동거리와 실제 좌표 변화를 비교하여 비정상 데이터 필터링
- **정지 상태 처리**: 속도 0인 경우 흔들림 방지를 위한 임계값 적용

```kotlin
// 위치 검증 로직 예시
fun isValidLocation(
    prevLocation: Location,
    newLocation: Location,
    speed: Float
): Boolean {
    val accuracy = newLocation.accuracy
    val distance = prevLocation.distanceTo(newLocation)
    val expectedDistance = speed * timeDelta

    return accuracy < ACCURACY_THRESHOLD &&
           distance < expectedDistance * SPEED_TOLERANCE_FACTOR
}
```

### 2. VoIP 수신 지연 및 세션 연결 문제

**문제**: FCM 푸시 → 앱 → WebSocket 연결까지의 지연 시간 및 세션 불안정

**해결 방안**:
- **FCM 데이터 페이로드 최적화**: 통화 정보를 FCM 데이터 메시지에 포함
- **WebSocket 사전 연결**: 앱 시작 시 WebSocket 연결 유지
- **STOMP over RabbitMQ**: 안정적인 메시지 브로커 활용
- **TURN/STUN 서버 구축**: NAT 환경에서의 P2P 연결 보장

```kotlin
// VoIP 호출 흐름
FCM Push → OnFcmReceived() → WebSocket.send(CALL_SIGNAL)
→ WebRTC Offer/Answer → Media Stream Connection
```

### 3. Android Emulator 네트워크 차단 문제

**문제**: Apple Silicon Mac에서 Android Emulator 네트워크 연결 불안정

**해결 방안**:
- **AVD 재생성**: 최신 시스템 이미지로 에뮬레이터 재설치
- **네트워크 설정 변경**: 브리지 모드 또는 NAT 모드 전환
- **10.0.2.2 사용**: localhost 대신 에뮬레이터 전용 호스트 주소 사용
- **실제 디바이스 테스트 병행**: 최종 검증은 실기기에서 수행

### 4. 장소 Alias 반영 및 UI 동기화 문제

**문제**: 사용자가 설정한 장소 별칭(alias)이 목록과 상세 화면에서 불일치

**해결 방안**:
- **StateFlow 기반 상태 관리**: 단일 진실 공급원(Single Source of Truth) 패턴 적용
- **Repository 레벨 동기화**: 로컬 DB와 네트워크 응답 동기화
- **자동 fallback 로직**: alias가 null인 경우 원본 장소명으로 자동 표시

```kotlin
// Alias fallback 로직
val displayName = place.alias?.takeIf { it.isNotBlank() }
    ?: place.placeName
```

### 5. Git 충돌 대규모 해결

**문제**: 다중 브랜치 환경에서 대규모 Merge Conflict 발생

**해결 방안**:
- **Rebase 전략 적용**: `git rebase -i` 를 통한 커밋 정리
- **충돌 영역 분할**: 파일별/기능별로 충돌 해결 단위 분리
- **코드 리뷰 강화**: PR 단위 축소 및 빈번한 머지

---

## 💡 기대 효과

### 보호자 관점
- **돌봄 부담 경감**: 실시간 위치 확인으로 불안감 해소
- **긴급 대응 강화**: 이상 징후 즉시 알림 및 통화 연결
- **생활 패턴 이해**: 대시보드를 통한 환자 상태 파악

### 환자 관점
- **이동 안전성 향상**: 안전구역 이탈 시 보호자 개입 가능
- **독립성 유지**: 과도한 제약 없이 일상 생활 가능
- **긴급 상황 대응**: 즉시 보호자와 연락 가능

### 사회적 효과
- **노인 실종률 감소**: 조기 발견 및 예방 시스템 구축
- **의료비 절감**: 조기 이상 징후 감지로 중증 악화 방지
- **초고령 사회 대응**: 기술 기반 돌봄 모델 제시

---

## 👥 팀 정보

**SSAFY 13기 자율 프로젝트 - A305팀**

- **프로젝트 기간**: 2025.10 - 2025.11 (7주)
- **팀원**: 6명
- **역할 분담**:
  - Backend: Spring Boot API, 인프라 구축
  - Android: 보호자 앱, 환자 앱
---


**온길과 함께하는 안전한 돌봄의 시작** 🛤️
