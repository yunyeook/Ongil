# Ongil 백엔드 프로젝트 가이드

## 📋 프로젝트 개요

**Ongil**은 치매 환자와 보호자를 연결하는 케어 플랫폼입니다.

### 기술 스택
- **Java 21** + **Spring Boot 3.5.7**
- **PostgreSQL** (RDS) + **Redis** (ElastiCache)
- **AWS S3** (파일 저장소)
- **Spring Security** + **JWT** (인증/인가)
- **WebSocket** (실시간 통신)
- **Swagger/OpenAPI** (API 문서화)
- **Tmap API** (지도/위치 서비스)

---

## 📁 프로젝트 구조

```
src/main/java/kr/co/ongil/
├── domain/                          # 비즈니스 도메인 계층
│   ├── auth/                        # 인증/인가 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   └── dto/request/
│   │
│   ├── call/                        # 통화 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   │       ├── request/
│   │       └── response/
│   │
│   ├── map/                         # 지도/위치 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   │   ├── MapService.java     # 비즈니스 로직
│   │   │   └── TmapService.java    # Tmap API 연동
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   │       ├── request/
│   │       ├── response/            # 클라이언트 응답 DTO
│   │       └── tmap/                # Tmap API 전용 DTO
│   │
│   ├── notification/                # 알림 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   │
│   ├── patient/                     # 환자 도메인 (서브도메인 포함)
│   │   ├── abnormal/                # 이상행동 탐지
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   └── dto/
│   │   ├── favorite/                # 자주 가는 장소
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   └── dto/
│   │   ├── location/                # 위치 추적
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   └── dto/
│   │   └── safezone/                # 안전구역
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       └── dto/
│   │
│   ├── relationship/                # 보호자-환자 관계 도메인
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   │
│   └── user/                        # 사용자 도메인
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       │   ├── User.java
│       │   ├── Provider.java        # OAuth 제공자 enum
│       │   └── UserType.java        # 사용자 유형 enum
│       └── dto/
│
└── global/                          # 공통 레이어
    ├── common/                      # 공통 컴포넌트
    │   ├── entity/
    │   │   └── BaseEntity.java      # 공통 엔티티 (id, createdAt, updatedAt)
    │   └── response/
    │       ├── ApiResponse.java     # 표준 응답 래퍼
    │       ├── ResponseMessage.java # 성공 메시지 enum
    │       └── PageInfo.java        # 페이징 정보
    │
    ├── config/                      # 설정
    │   ├── SecurityConfig.java      # Spring Security + JWT
    │   ├── SwaggerConfig.java       # API 문서화
    │   ├── CorsConfig.java          # CORS 설정
    │   ├── WebMvcConfig.java        # MVC 설정 (/api/v1 prefix 자동 추가)
    │   ├── TmapConfig.java          # Tmap API 설정
    │   ├── JacksonConfig.java       # JSON 직렬화 설정
    │   └── DotenvApplicationInitializer.java
    │
    ├── exception/                   # 예외 처리
    │   ├── ErrorCode.java           # 에러 코드 enum
    │   ├── BusinessException.java   # 비즈니스 예외
    │   └── GlobalExceptionHandler.java
    │
    └── util/                        # 유틸리티
        ├── JwtUtil.java             # JWT 토큰 유틸
        └── FileService.java         # 파일 처리 유틸
```

---

## 🎯 도메인별 설명

### 1. **auth** - 인증/인가
- 회원가입, 로그인, 로그아웃
- JWT 토큰 발급 및 갱신
- OAuth 2.0 소셜 로그인 (향후 확장)

### 2. **user** - 사용자
- 사용자 정보 조회/수정
- 프로필 이미지 관리
- Provider: `LOCAL`, `KAKAO`, `GOOGLE`, `APPLE`
- UserType: `CAREGIVER` (보호자), `PATIENT` (환자)

### 3. **relationship** - 보호자-환자 관계
- 보호자와 환자의 연결 관계 관리
- 권한 검증 (특정 환자 정보 접근 권한)

### 4. **patient** - 환자 관련 기능
#### 4.1 **location** - 위치 추적
- 환자의 실시간 위치 정보 저장/조회
- 위치 이력 관리

#### 4.2 **safezone** - 안전구역
- 안전구역 설정 (집, 병원 등)
- 안전구역 이탈 감지

#### 4.3 **abnormal** - 이상행동 탐지
- 비정상적인 이동 패턴 감지
- 이상행동 알림 이벤트 생성

#### 4.4 **favorite** - 자주 가는 장소
- 자주 가는 장소 등록/조회/수정/삭제
- 장소 별칭 설정

### 5. **map** - 지도/위치 서비스
- **좌표 → 주소 변환** (Reverse Geocoding)
- **주소 → 좌표 변환** (Geocoding)
- **장소 검색** (POI 검색)
- **장소 상세 조회**
- **경로 안내** (Tmap API 연동)

### 6. **call** - 통화
- 통화 기록 관리
- 긴급 통화 (SOS) 기능
- 통화 연결 상태 관리

### 7. **notification** - 알림
- 푸시 알림 전송
- 알림 이력 조회
- SSE (Server-Sent Events) 실시간 알림

---

## 🔧 핵심 개발 규칙

### 1. **API 응답 체계**

모든 API는 `ApiResponse<T>` 래퍼를 사용합니다.

#### ✅ 성공 응답 (데이터 있음)
```java
@GetMapping("/address")
public ApiResponse<AddressResponse> getAddress(
    @RequestParam Double latitude,
    @RequestParam Double longitude
) {
    AddressResponse response = mapService.getAddress(latitude, longitude);
    return ApiResponse.success(ResponseMessage.ADDRESS_FOUND, response);
}
```

**응답 예시:**
```json
{
  "message": "주소를 성공적으로 조회하였습니다.",
  "data": {
    "roadAddress": "서울 중구 세종대로 110",
    "jibunAddress": "서울 중구 태평로1가 31"
  }
}
```

#### ✅ 성공 응답 (데이터 없음)
```java
@DeleteMapping("/{id}")
public ApiResponse<String> deletePatient(@PathVariable Long id) {
    patientService.deletePatient(id);
    return ApiResponse.success(ResponseMessage.PATIENT_DELETED);
}
```

**응답 예시:**
```json
{
  "message": "환자 정보가 삭제되었습니다.",
  "data": ""
}
```

#### ❌ 에러 응답
```json
{
  "message": "유효하지 않은 좌표입니다."
}
```

---

### 2. **예외 처리**

#### BusinessException 발생
```java
@Service
@RequiredArgsConstructor
public class MapService {

    public AddressResponse getAddress(Double latitude, Double longitude) {
        // 1. 유효성 검증
        validateCoordinate(latitude, longitude);

        // 2. 비즈니스 로직
        return tmapService.getAddressFromCoordinate(latitude, longitude);
    }

    private void validateCoordinate(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_COORDINATE);
        }

        if (latitude < 33.0 || latitude > 43.0) {
            throw new BusinessException(ErrorCode.INVALID_LATITUDE);
        }

        if (longitude < 124.0 || longitude > 132.0) {
            throw new BusinessException(ErrorCode.INVALID_LONGITUDE);
        }
    }
}
```

#### ErrorCode 추가 규칙
- **카테고리별 그룹화**: AUTH, USER, PATIENT, MAP, CALL, NOTIFICATION 등
- **HttpStatus 명시**: `BAD_REQUEST`, `NOT_FOUND`, `CONFLICT`, `UNAUTHORIZED` 등
- **사용자 친화적 메시지**: 문제를 명확히 설명

#### ResponseMessage 추가 규칙
- **간결하고 명확한 메시지**: "~가 성공했습니다.", "~를 조회하였습니다."
- **도메인별 그룹화**: Auth, User, Patient, Map 등

---

### 3. **Entity 작성 규칙**

```java
@Entity
@Table(name = "patients")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Patient extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true)
    private String deviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User caregiver;

    // 비즈니스 로직 메서드
    public void updateName(String name) {
        this.name = name;
    }
}
```

#### 규칙
1. `BaseEntity` 상속 (id, createdAt, updatedAt 자동 관리)
2. `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 필수
3. `@Builder` 패턴 사용
4. 연관관계는 `FetchType.LAZY` 사용
5. 비즈니스 로직은 Entity 내부 메서드로 작성

---

### 4. **DTO 작성 규칙**

#### Request DTO (Record 사용)
```java
public record CoordinateRequest(

    @Schema(description = "시/도", example = "서울특별시")
    @NotBlank(message = "시/도는 필수입니다.")
    String cityDo,

    @Schema(description = "구/군", example = "강남구")
    @NotBlank(message = "구/군은 필수입니다.")
    String guGun,

    @Schema(description = "동/읍/면", example = "역삼동")
    @NotBlank(message = "동/읍/면은 필수입니다.")
    String dong,

    @Schema(description = "번지", example = "737")
    String bunji
) {
}
```

#### Response DTO (Record 사용)
```java
@Schema(description = "주소 정보 응답")
public record AddressResponse(

    @Schema(description = "도로명 주소", example = "서울 중구 세종대로 110")
    String roadAddress,

    @Schema(description = "지번 주소", example = "서울 중구 태평로1가 31")
    String jibunAddress
) {

    // 정적 팩토리 메서드 - 파라미터 복수일 경우 'of'
    public static AddressResponse of(String roadAddress, String jibunAddress) {
        return new AddressResponse(roadAddress, jibunAddress);
    }

    // 정적 팩토리 메서드 - 파라미터 단수일 경우 'from'
    public static AddressResponse from(AddressEntity entity) {
        return new AddressResponse(
            entity.getRoadAddress(),
            entity.getJibunAddress()
        );
    }
}
```

#### Validation 어노테이션 가이드

| 어노테이션 | 사용 타입 | null | 빈 문자열("") | 공백(" ") | 빈 컬렉션 |
|-----------|----------|------|--------------|----------|----------|
| `@NotNull` | 모든 객체 | ❌ | ✅ | ✅ | ✅ |
| `@NotEmpty` | String, Collection, Map, Array | ❌ | ❌ | ✅ | ❌ |
| `@NotBlank` | String, CharSequence | ❌ | ❌ | ❌ | - |

#### 규칙
1. **Record 클래스 사용** (불변성 보장)
2. **DTO 이름에 "Dto" 접미사 붙이지 않음**
   - ✅ `AddressResponse`
   - ❌ `AddressResponseDto`
3. **Request DTO는 `dto/request/` 디렉토리**
4. **Response DTO는 `dto/response/` 디렉토리**
5. **정적 팩토리 메서드**:
   - 파라미터 단수: `from()`
   - 파라미터 복수: `of()`
6. **Swagger 문서화 필수**: `@Schema` 어노테이션
7. **중복 필드가 있는 경우 `~Info`로 추출하여 재사용**

---

### 5. **Service 작성 규칙**

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

    private final TmapService tmapService;
    private final MapRepository mapRepository;

    /**
     * 좌표로 주소 조회 (Reverse Geocoding)
     */
    @Transactional(readOnly = true)
    public AddressResponse getAddress(Double latitude, Double longitude) {
        log.info("좌표 → 주소 변환 요청: lat={}, lng={}", latitude, longitude);

        // 1. 유효성 검증
        validateCoordinate(latitude, longitude);

        // 2. 외부 API 호출
        return tmapService.getAddressFromCoordinate(latitude, longitude);
    }

    /**
     * 좌표 유효성 검증 (대한민국 범위)
     */
    private void validateCoordinate(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_COORDINATE);
        }

        if (latitude < 33.0 || latitude > 43.0) {
            throw new BusinessException(ErrorCode.INVALID_LATITUDE);
        }

        if (longitude < 124.0 || longitude > 132.0) {
            throw new BusinessException(ErrorCode.INVALID_LONGITUDE);
        }
    }
}
```

#### 규칙
1. `@Slf4j` 어노테이션으로 로깅 활성화
2. `@RequiredArgsConstructor`로 생성자 주입
3. public 메서드는 비즈니스 로직을 명확히 표현
4. private 메서드로 검증 로직 분리
5. 예외는 `BusinessException`으로 통일
6. **트랜잭션**:
   - 읽기 전용: `@Transactional(readOnly = true)`
   - 쓰기: `@Transactional`

---

### 6. **Controller 작성 규칙**

```java
@Slf4j
@Validated
@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
@Tag(name = "Map API", description = "지도 관련 API")
public class MapController {

    private final MapService mapService;

    /**
     * 좌표 → 주소 변환 (Reverse Geocoding)
     */
    @GetMapping("/address")
    @Operation(summary = "좌표로 주소 조회", description = "GPS 좌표를 받아 주소로 변환합니다.")
    public ApiResponse<AddressResponse> getAddress(
        @Parameter(description = "위도", example = "37.5665", required = true)
        @RequestParam Double latitude,

        @Parameter(description = "경도", example = "126.9780", required = true)
        @RequestParam Double longitude
    ) {
        AddressResponse response = mapService.getAddress(latitude, longitude);
        return ApiResponse.success(ResponseMessage.ADDRESS_FOUND, response);
    }
}
```

#### 규칙
1. `@RestController` + `@RequestMapping("/도메인경로")`
   - ⚠️ **중요**: `/api/v1` prefix는 **붙이지 않습니다**
   - `WebMvcConfig`에서 `kr.co.ongil.domain` 패키지의 모든 컨트롤러에 자동으로 `/api/v1` prefix 추가
   - 예시: `@RequestMapping("/map")` → 실제 경로: `/api/v1/map`
2. `@Tag`로 Swagger 그룹화
3. `@Operation`으로 API 문서화
4. `@Parameter`로 파라미터 설명
5. 모든 응답은 `ApiResponse<T>` 타입
6. `ResponseMessage` enum을 통해 성공 메시지 관리

---

### 7. **Repository 작성 규칙**

```java
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByDeviceId(String deviceId);

    List<Patient> findByCaregiver(User caregiver);

    boolean existsByDeviceId(String deviceId);

    @Query("SELECT p FROM Patient p WHERE p.name LIKE %:keyword%")
    List<Patient> searchByNameContaining(@Param("keyword") String keyword);
}
```

#### 규칙
1. `JpaRepository<Entity, ID>` 상속
2. 메서드 네이밍 컨벤션 준수 (`findBy...`, `existsBy...`, `countBy...`)
3. 복잡한 쿼리는 `@Query` 어노테이션 사용

---

## 🔐 보안 및 인증

### JWT 토큰 사용
- **Access Token**: `Authorization: Bearer {token}` 헤더로 전달
- **Refresh Token**: 별도 관리

### 인증이 필요한 엔드포인트
```java
@GetMapping("/my-profile")
@Operation(summary = "내 프로필 조회")
public ApiResponse<UserResponse> getMyProfile(
    @AuthenticationPrincipal UserDetails userDetails) {

    Long userId = Long.parseLong(userDetails.getUsername());
    UserResponse profile = userService.getUser(userId);

    return ApiResponse.success(ResponseMessage.USER_FOUND, profile);
}
```

---

## ✅ 개발 체크리스트

### 새로운 API 개발 시
1. ✅ **ErrorCode에 필요한 에러 코드 추가**
2. ✅ **ResponseMessage에 성공 메시지 추가**
3. ✅ **Entity 작성** (BaseEntity 상속, @Builder, LAZY 로딩)
4. ✅ **DTO 작성** (Record, @Schema, Validation, 정적 팩토리 메서드)
5. ✅ **Repository 작성** (JpaRepository 상속)
6. ✅ **Service 작성** (@Slf4j, @Transactional, 검증 로직 분리)
7. ✅ **Controller 작성** (@Tag, @Operation, @Parameter, ApiResponse 반환)
8. ✅ **Swagger 문서 확인** (`http://localhost:8080/swagger-ui.html`)

---

## 📝 주의사항

### 일관성 유지
1. **응답 구조**: 모든 API는 `ApiResponse<T>` 사용
2. **에러 코드**: `ErrorCode` enum에 정의된 코드 사용
3. **성공 메시지**: `ResponseMessage` enum에 정의된 메시지 사용

### 예외 처리
1. 비즈니스 로직 검증 실패 시 `BusinessException` 발생
2. `try-catch`보다는 명시적 검증 후 예외 발생 선호
3. `GlobalExceptionHandler`가 모든 예외를 일관되게 처리

### DTO 관리
1. Entity를 직접 반환하지 않고 DTO 사용
2. Record 클래스로 불변성 보장
3. 정적 팩토리 메서드로 변환 (`from()`, `of()`)

### 검증
1. Request DTO에 Bean Validation 적용 (`@NotNull`, `@NotBlank` 등)
2. 비즈니스 로직 검증은 Service 레이어에서 수행
3. 도메인 규칙 검증은 Entity 메서드로 캡슐화

### 로깅
1. `@Slf4j`를 통한 로깅 활용
2. 중요 비즈니스 로직 시작/종료 시점 로그
3. 예외 발생 시 로그 (stack trace는 `GlobalExceptionHandler`에서 처리)

### 코드 품질
1. 메서드는 한 가지 책임만 수행
2. 매직 넘버/문자열 사용 금지 (상수화)
3. 의미 있는 변수명과 메서드명 사용
4. 주석보다는 코드로 의도 표현

---

## 🚀 다음 작업 시 참고사항

이 문서를 기반으로 다음을 수행할 수 있습니다:
1. 새로운 도메인 개발
2. API 엔드포인트 추가
3. 에러 코드 및 메시지 추가
4. Entity 및 DTO 작성
5. 비즈니스 로직 구현

**모든 코드는 이 가이드의 규칙을 따라 작성합니다.**
