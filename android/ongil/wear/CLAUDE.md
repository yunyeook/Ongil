# 온길(Ongil) Wear OS 앱 개발 가이드

## 목차
1. [프로젝트 개요](#프로젝트-개요)
2. [현재 구현 상태](#현재-구현-상태)
3. [워치 앱 아키텍처](#워치-앱-아키텍처)
4. [워치 앱 개발 로드맵](#워치-앱-개발-로드맵)
5. [주요 기능별 구현 계획](#주요-기능별-구현-계획)
6. [앱-워치 연동 전략](#앱-워치-연동-전략)
7. [공통 모듈 활용 전략](#공통-모듈-활용-전략)
8. [주요 기술 스택](#주요-기술-스택)

---

## 프로젝트 개요

온길(Ongil)은 치매 환자 및 노인을 위한 안전 케어 솔루션입니다. Wear OS 앱은 환자가 착용하는 스마트워치에서 실행되며, 보호자 앱과 연동하여 다음 기능을 제공합니다:

- **실시간 위치 추적**: 환자의 현재 위치를 보호자에게 전송
- **안전 구역 모니터링**: 설정된 안전 범위 이탈 감지
- **긴급 통화**: 보호자와 핫라인 통화 기능
- **길찾기 네비게이션**: 화살표 기반 간단한 경로 안내
- **도움 요청**: 음성 재생을 통한 주변 도움 요청
- **SOS 알림**: 위급 상황 시 자동 알림 전송

---

## 현재 구현 상태

### ✅ 완료된 기능

#### 1. 인증 및 동기화 시스템
- **Phone-Watch Data Sync**: Wearable DataLayer를 통한 로그인 정보 동기화
  - Path: `/login_data`
  - 동기화 데이터: access token, refresh token, user ID, user type, selected patient ID
- **DataStore 기반 로컬 저장**: 보안 토큰 및 사용자 정보 저장
- **실시간 로그인 상태 감지**: Flow 기반 리액티브 상태 관리

**주요 파일**:
- [WearAuthViewModel.kt](presentation/viewmodel/WearAuthViewModel.kt)
- [PhoneDataSyncManager.kt](data/datasource/sync/PhoneDataSyncManager.kt)
- [WearDataStoreManagerImpl.kt](data/datasource/local/WearDataStoreManagerImpl.kt)

#### 2. 기본 UI 화면
- **LoginSyncScreen**: 로그인 정보 동기화 대기 화면
- **MainScreen**: TMap 기반 메인 지도 화면
- **WearTMapComposable**: TMap 3.0 통합 컴포저블

**주요 파일**:
- [LoginSyncScreen.kt](presentation/ui/LoginSyncScreen.kt)
- [MainScreen.kt](presentation/ui/MainScreen.kt)
- [WearTMapComposable.kt](presentation/ui/map/WearTMapComposable.kt)

#### 3. Wear OS 특화 기능
- **Tile Service**: 기본 타일 템플릿 (`StatusTileService.kt`)
- **Complication Service**: 시계 페이스 컴플리케이션 (`StatusComplicationService.kt`)

#### 4. 아키텍처 기반 구조
- **Clean Architecture**: Domain → Data → Presentation 레이어 분리
- **MVVM 패턴**: ViewModel + Compose UI
- **Hilt DI**: 의존성 주입 설정 완료
- **Repository Pattern**: 데이터 레이어 추상화

**DI 모듈**:
- [WearAppModule.kt](di/WearAppModule.kt)
- [WearDataModule.kt](di/WearDataModule.kt)

### 🚧 진행 중/필요한 기능

#### 1. 화면 및 네비게이션
- [ ] 환자 선택 화면 (보호자용)
- [ ] 메인 대시보드 (전화/지도/도움 아이콘)
- [ ] 지도 화면 (환자 위치 + 안전 범위)
- [ ] 통화 화면 (통화 중 UI)
- [ ] 도움 요청 화면
- [ ] 네비게이션 경로 안내 화면

#### 2. 위치 추적 기능
- [ ] Foreground Service 기반 위치 추적
- [ ] 주기적 위치 전송 (서버 + 보호자 앱)
- [ ] 배터리 최적화 전략
- [ ] 안전 범위 모니터링

#### 3. 통화 기능
- [ ] WebRTC 기반 VoIP 통화
- [ ] 핫라인 통화 시작
- [ ] 통화 중 UI 표시
- [ ] 통화 종료 처리

#### 4. 네비게이션 기능
- [ ] 화살표 기반 방향 표시
- [ ] 경로 이탈 감지 및 알림
- [ ] 목적지 도착 알림

#### 5. 도움 요청 기능
- [ ] 음성 재생 (TTS)
- [ ] 주변 도움 요청 신호 전송

#### 6. 네트워크 레이어
- [ ] Retrofit API 클라이언트
- [ ] WebSocket 연결 (VoIP 신호)
- [ ] SSE 연결 (실시간 위치 업데이트)
- [ ] 오프라인 대응 로직

---

## 워치 앱 아키텍처

### 패키지 구조

```
kr.co.ongil.wear/
├── presentation/
│   ├── ui/
│   │   ├── login/
│   │   │   └── LoginSyncScreen.kt                    [✅ 완료]
│   │   ├── main/
│   │   │   ├── MainScreen.kt                         [✅ 완료]
│   │   │   ├── PatientSelectionScreen.kt            [TODO]
│   │   │   └── DashboardScreen.kt                    [TODO]
│   │   ├── map/
│   │   │   ├── WearTMapComposable.kt                 [✅ 완료]
│   │   │   ├── MapScreen.kt                          [TODO]
│   │   │   ├── NavigationScreen.kt                   [TODO]
│   │   │   └── component/
│   │   │       ├── ArrowNavigationIndicator.kt       [TODO]
│   │   │       └── SafeZoneOverlay.kt                [TODO]
│   │   ├── call/
│   │   │   ├── CallScreen.kt                         [TODO]
│   │   │   ├── IncomingCallScreen.kt                 [TODO]
│   │   │   └── component/
│   │   │       └── CallControls.kt                   [TODO]
│   │   ├── help/
│   │   │   └── HelpRequestScreen.kt                  [TODO]
│   │   └── common/
│   │       ├── LoadingIndicator.kt
│   │       └── ErrorScreen.kt
│   ├── viewmodel/
│   │   ├── WearAuthViewModel.kt                      [✅ 완료]
│   │   ├── MapViewModel.kt                           [TODO]
│   │   ├── CallViewModel.kt                          [TODO]
│   │   ├── NavigationViewModel.kt                    [TODO]
│   │   └── HelpRequestViewModel.kt                   [TODO]
│   ├── navigation/
│   │   ├── WearNavGraph.kt                           [TODO]
│   │   └── WearRoute.kt                              [TODO]
│   └── theme/
│       └── Theme.kt                                   [✅ 완료]
├── domain/
│   ├── model/
│   │   ├── WearLoginData.kt                          [✅ 완료]
│   │   ├── PatientInfo.kt                            [TODO]
│   │   ├── LocationUpdate.kt                         [TODO]
│   │   ├── SafeZoneConfig.kt                         [TODO]
│   │   ├── NavigationState.kt                        [TODO]
│   │   └── CallState.kt                              [TODO]
│   ├── repository/
│   │   ├── WearAuthRepository.kt                     [✅ 완료]
│   │   ├── LocationRepository.kt                     [TODO]
│   │   ├── CallRepository.kt                         [TODO]
│   │   ├── NavigationRepository.kt                   [TODO]
│   │   └── PatientRepository.kt                      [TODO]
│   └── usecase/
│   │   ├── SyncLoginDataUseCase.kt                   [✅ 완료]
│   │   ├── TrackLocationUseCase.kt                   [TODO]
│   │   ├── StartCallUseCase.kt                       [TODO]
│   │   ├── MonitorSafeZoneUseCase.kt                 [TODO]
│   │   ├── NavigateToDestinationUseCase.kt           [TODO]
│   │   └── RequestHelpUseCase.kt                     [TODO]
├── data/
│   ├── datasource/
│   │   ├── local/
│   │   │   ├── WearDataStoreManager.kt               [✅ 완료]
│   │   │   ├── WearDataStoreManagerImpl.kt           [✅ 완료]
│   │   │   └── DataStoreKeys.kt                      [✅ 완료]
│   │   ├── remote/
│   │   │   ├── api/
│   │   │   │   ├── WearLocationApi.kt                [TODO]
│   │   │   │   ├── WearCallApi.kt                    [TODO]
│   │   │   │   └── WearNavigationApi.kt              [TODO]
│   │   │   ├── websocket/
│   │   │   │   ├── WearWebSocketManager.kt           [TODO]
│   │   │   │   └── CallSignalingService.kt           [TODO]
│   │   │   └── RetrofitClient.kt                     [TODO]
│   │   └── sync/
│   │       ├── PhoneDataSyncManager.kt               [✅ 완료]
│   │       └── LocationSyncManager.kt                [TODO]
│   ├── model/
│   │   ├── WearLoginData.kt                          [✅ 완료]
│   │   ├── LocationDto.kt                            [TODO]
│   │   ├── CallDto.kt                                [TODO]
│   │   └── NavigationDto.kt                          [TODO]
│   └── repository/
│       ├── WearAuthRepositoryImpl.kt                 [✅ 완료]
│       ├── LocationRepositoryImpl.kt                 [TODO]
│       ├── CallRepositoryImpl.kt                     [TODO]
│       └── NavigationRepositoryImpl.kt               [TODO]
├── service/
│   ├── location/
│   │   └── WearLocationTrackingService.kt            [TODO]
│   ├── call/
│   │   └── WearCallService.kt                        [TODO]
│   └── notification/
│       └── WearNotificationManager.kt                [TODO]
├── di/
│   ├── WearAppModule.kt                              [✅ 완료]
│   ├── WearDataModule.kt                             [✅ 완료]
│   ├── WearNetworkModule.kt                          [TODO]
│   └── WearServiceModule.kt                          [TODO]
├── tile/
│   └── StatusTileService.kt                          [✅ 완료]
├── complication/
│   └── StatusComplicationService.kt                  [✅ 완료]
└── WearApplication.kt                                 [✅ 완료]
```

### 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  UI Screens  │  │  ViewModels  │  │  Navigation  │      │
│  │  (Compose)   │◄─│   (State)    │◄─│   (NavHost)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Use Cases  │  │  Repositories│  │    Models    │      │
│  │   (Logic)    │──│  (Interface) │──│   (Domain)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Repositories │  │  DataSources │  │     DTOs     │      │
│  │    (Impl)    │──│ (Local/Remote│──│    (Data)    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│  Local:           Remote:             Sync:                 │
│  - DataStore      - Retrofit API      - Wearable DataLayer │
│                   - WebSocket         - Phone-Watch Sync    │
│                   - SSE                                      │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                     External Services                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Backend    │  │  Phone App   │  │    Common    │      │
│  │   API/WS     │  │  (DataLayer) │  │    Module    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

---

## 워치 앱 개발 로드맵

### Phase 1: 기본 인프라 구축 (1-2주)
**목표**: 네트워크, 데이터 동기화, 네비게이션 기반 구축

#### 1.1 네트워크 레이어
- [ ] Retrofit 클라이언트 설정 (앱 모듈 참고)
- [ ] API 인터페이스 정의 (WearLocationApi, WearCallApi)
- [ ] Auth Interceptor (토큰 자동 주입)
- [ ] WebSocket Manager (VoIP 신호용)
- [ ] 에러 핸들링 및 재시도 로직

**참고 파일 (앱 모듈)**:
- `app/data/datasource/remote/RetrofitClient.kt`
- `app/data/datasource/remote/interceptor/AuthInterceptor.kt`
- `app/data/datasource/websocket/WebSocketManager.kt`

#### 1.2 데이터 모델 및 Repository
- [ ] DTOs (LocationDto, CallDto, NavigationDto, SafeZoneDto)
- [ ] Domain Models (LocationUpdate, CallState, NavigationState)
- [ ] Repository Interfaces (domain layer)
- [ ] Repository Implementations (data layer)

#### 1.3 Navigation 구조
- [ ] WearRoute sealed class (화면 라우트 정의)
- [ ] WearNavGraph (NavHost 설정)
- [ ] Deep linking for incoming calls
- [ ] Scaffold 기반 화면 전환

**참고 파일 (앱 모듈)**:
- `app/presentation/navigation/Routes.kt`
- `app/presentation/navigation/AppNavGraph.kt`

### Phase 2: 위치 추적 및 안전 범위 (2-3주)
**목표**: 실시간 위치 추적 및 안전 구역 모니터링

#### 2.1 위치 추적 Service
- [ ] WearLocationTrackingService (Foreground Service)
- [ ] FusedLocationProviderClient 통합
- [ ] 주기적 위치 전송 (서버 API)
- [ ] 배터리 최적화 (업데이트 간격 조정)
- [ ] LocationStreamBus 통합 (Common 모듈)

**참고 파일**:
- `app/service/location/LocationTrackingService.kt`
- `common/location/LocationStreamBus.kt`

**구현 계획**:
```kotlin
// WearLocationTrackingService.kt
class WearLocationTrackingService : Service() {
    @Inject lateinit var fusedLocationClient: FusedLocationProviderClient
    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var locationStreamBus: LocationStreamBus

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L // 5초 간격
    ).build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                // 1. LocationStreamBus로 앱 내 브로드캐스트
                val point = LocationPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = location.accuracy,
                    bearing = location.bearing,
                    speedMps = location.speed,
                    timeMillis = System.currentTimeMillis()
                )
                locationStreamBus.tryEmit(point)

                // 2. 서버로 전송
                lifecycleScope.launch {
                    locationRepository.updateLocation(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }
            }
        }
    }
}
```

#### 2.2 안전 범위 모니터링
- [ ] MonitorSafeZoneUseCase
- [ ] SafetyZoneMonitor 통합 (Common 모듈)
- [ ] 안전 범위 이탈 알림 (Notification)
- [ ] 3단계 안전 구역 시각화 (지도 오버레이)

**참고 파일**:
- `common/location/SafetyZoneMonitor.kt`
- `app/core/utils/SafetyZoneMonitor.kt`

**구현 계획**:
```kotlin
// MonitorSafeZoneUseCase.kt
class MonitorSafeZoneUseCase @Inject constructor(
    private val locationStreamBus: LocationStreamBus,
    private val safetyZoneMonitor: SafetyZoneMonitor,
    private val notificationManager: WearNotificationManager
) {
    operator fun invoke(
        homeLatitude: Double,
        homeLongitude: Double
    ): Flow<SafetyZoneStatus> = flow {
        safetyZoneMonitor.setHome(homeLatitude, homeLongitude)

        locationStreamBus.updates.collect { location ->
            safetyZoneMonitor.updateLocation(
                location.latitude,
                location.longitude,
                location.timeMillis
            )

            val status = safetyZoneMonitor.getCurrentStatus()
            emit(status)

            // 알림 발송
            if (status.stage1OutsideDurationMinutes != null) {
                notificationManager.showSafeZoneAlert(stage = 1)
            }
        }
    }
}
```

#### 2.3 지도 화면 구현
- [ ] MapScreen (환자 위치 + 안전 범위)
- [ ] SafeZoneOverlay Composable (3단계 원 표시)
- [ ] 실시간 위치 업데이트 반영
- [ ] TMap 마커 및 원형 오버레이

**UI 컴포넌트**:
```kotlin
// SafeZoneOverlay.kt
@Composable
fun SafeZoneOverlay(
    homeLatitude: Double,
    homeLongitude: Double,
    stage1Radius: Int,
    stage2Radius: Int,
    stage3Radius: Int,
    currentLatitude: Double,
    currentLongitude: Double
) {
    // TMap 오버레이로 3단계 원 + 현재 위치 마커 표시
}
```

### Phase 3: VoIP 통화 기능 (2-3주)
**목표**: WebRTC 기반 핫라인 통화 기능

#### 3.1 WebRTC 통합
- [ ] WearCallService
- [ ] WebRTC PeerConnection 설정
- [ ] Audio Track 관리
- [ ] ICE 후보 교환

**참고 파일**:
- `app/core/webrtc/WebRtcCallClient.kt`
- `app/core/webrtc/WebRtcModule.kt`

**구현 계획**:
```kotlin
// WearCallService.kt
class WearCallService @Inject constructor(
    private val peerConnectionFactory: PeerConnectionFactory,
    private val webSocketManager: WearWebSocketManager,
    private val callRepository: CallRepository
) {
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null

    suspend fun startCall(targetUserId: String, targetPhone: String) {
        // 1. 서버에 통화 생성 요청
        val callId = callRepository.createCall(targetUserId, targetPhone)

        // 2. WebRTC PeerConnection 생성
        peerConnection = createPeerConnection()

        // 3. Audio Track 추가
        localAudioTrack = createAudioTrack()
        peerConnection?.addTrack(localAudioTrack)

        // 4. SDP Offer 생성 및 전송
        val offer = peerConnection?.createOffer()
        peerConnection?.setLocalDescription(offer)
        webSocketManager.sendSignal(callId, "OFFER", offer.description)

        // 5. WebSocket으로 신호 수신
        webSocketManager.observeSignals(callId).collect { signal ->
            when (signal.type) {
                "ANSWER" -> handleAnswer(signal)
                "ICE" -> handleIceCandidate(signal)
                "HANGUP" -> endCall()
            }
        }
    }
}
```

#### 3.2 통화 UI
- [ ] IncomingCallScreen (수신 화면)
- [ ] CallScreen (통화 중 화면)
- [ ] CallControls Composable (종료 버튼)
- [ ] 통화 상태 표시 (연결 중, 통화 중, 종료)

**UI 컴포넌트**:
```kotlin
// CallScreen.kt
@Composable
fun CallScreen(
    callState: CallState,
    onHangup: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 상대방 이름
        Text(
            text = callState.targetName,
            style = MaterialTheme.typography.title1
        )

        // 통화 시간
        Text(
            text = callState.duration.formatDuration(),
            style = MaterialTheme.typography.body1
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 종료 버튼
        Button(
            onClick = onHangup,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            )
        ) {
            Icon(Icons.Default.CallEnd, "통화 종료")
        }
    }
}
```

#### 3.3 WebSocket 신호 처리
- [ ] CallSignalingService
- [ ] STOMP 메시지 수신/발신
- [ ] 신호 타입별 처리 (OFFER, ANSWER, ICE, HANGUP)

**참고 파일**:
- `app/data/datasource/websocket/VoipSignalingService.kt`

### Phase 4: 네비게이션 및 길찾기 (2주)
**목표**: 화살표 기반 간단한 경로 안내

#### 4.1 네비게이션 데이터 관리
- [ ] NavigationRouteManager 통합 (Common 모듈)
- [ ] RouteDeviationMonitor 통합 (Common 모듈)
- [ ] 경로 시작/종료 API 호출
- [ ] 실시간 경로 이탈 감지

**참고 파일**:
- `common/location/NavigationRouteManager.kt`
- `common/location/RouteDeviationMonitor.kt`

#### 4.2 화살표 네비게이션 UI
- [ ] NavigationScreen
- [ ] ArrowNavigationIndicator Composable
- [ ] 방향 계산 로직 (Bearing 기반)
- [ ] 남은 거리 표시

**구현 계획**:
```kotlin
// ArrowNavigationIndicator.kt
@Composable
fun ArrowNavigationIndicator(
    currentLatitude: Double,
    currentLongitude: Double,
    currentBearing: Float,
    nextWaypointLatitude: Double,
    nextWaypointLongitude: Double,
    distanceToNextMeters: Float
) {
    // 1. 목표 방향 계산
    val targetBearing = calculateBearing(
        currentLatitude, currentLongitude,
        nextWaypointLatitude, nextWaypointLongitude
    )

    // 2. 상대 각도 계산
    val relativeBearing = (targetBearing - currentBearing + 360) % 360

    // 3. 화살표 회전
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ArrowUpward,
            contentDescription = "방향",
            modifier = Modifier
                .size(120.dp)
                .rotate(relativeBearing),
            tint = Color.Green
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${distanceToNextMeters.toInt()}m",
            style = MaterialTheme.typography.title2
        )
    }
}

private fun calculateBearing(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Float {
    val dLon = Math.toRadians(lon2 - lon1)
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)

    val y = sin(dLon) * cos(lat2Rad)
    val x = cos(lat1Rad) * sin(lat2Rad) -
            sin(lat1Rad) * cos(lat2Rad) * cos(dLon)

    val bearing = Math.toDegrees(atan2(y, x))
    return ((bearing + 360) % 360).toFloat()
}
```

#### 4.3 경로 이탈 알림
- [ ] 경로 이탈 감지 (RouteDeviationMonitor)
- [ ] Notification 알림
- [ ] 경로 재탐색 옵션

### Phase 5: 도움 요청 및 SOS (1주)
**목표**: 주변 도움 요청 및 긴급 알림

#### 5.1 도움 요청 기능
- [ ] HelpRequestScreen
- [ ] TTS 음성 재생 ("도와주세요" 등)
- [ ] 서버로 도움 요청 전송
- [ ] 주변 앱 사용자에게 알림

**구현 계획**:
```kotlin
// RequestHelpUseCase.kt
class RequestHelpUseCase @Inject constructor(
    private val textToSpeech: TextToSpeech,
    private val sosRepository: SosRepository,
    private val locationStreamBus: LocationStreamBus
) {
    suspend operator fun invoke(message: String) {
        // 1. 음성 재생
        textToSpeech.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )

        // 2. 현재 위치 가져오기
        val location = locationStreamBus.lastValue

        // 3. 서버로 도움 요청 전송
        if (location != null) {
            sosRepository.sendHelpRequest(
                latitude = location.latitude,
                longitude = location.longitude,
                message = message
            )
        }
    }
}
```

#### 5.2 SOS 알림
- [ ] 자동 SOS 전송 (특정 조건 충족 시)
- [ ] 보호자 앱으로 긴급 알림
- [ ] SOS 버튼 UI

### Phase 6: 대시보드 및 환자 선택 (1주)
**목표**: 메인 화면 및 환자 선택 기능

#### 6.1 대시보드 화면
- [ ] DashboardScreen (전화/지도/도움 아이콘)
- [ ] 3개 주요 기능 버튼
- [ ] 현재 상태 표시 (위치 추적 중, 안전 범위 내 등)

**UI 컴포넌트**:
```kotlin
// DashboardScreen.kt
@Composable
fun DashboardScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToCall: () -> Unit,
    onNavigateToHelp: () -> Unit,
    locationTrackingActive: Boolean,
    insideSafeZone: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상태 표시
        StatusIndicator(
            locationTrackingActive = locationTrackingActive,
            insideSafeZone = insideSafeZone
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3개 버튼
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            DashboardButton(
                icon = Icons.Default.Phone,
                label = "전화",
                onClick = onNavigateToCall
            )
            DashboardButton(
                icon = Icons.Default.Map,
                label = "지도",
                onClick = onNavigateToMap
            )
            DashboardButton(
                icon = Icons.Default.Warning,
                label = "도움",
                onClick = onNavigateToHelp
            )
        }
    }
}
```

#### 6.2 환자 선택 화면 (보호자용)
- [ ] PatientSelectionScreen
- [ ] 환자 목록 표시
- [ ] 선택한 환자 DataStore 저장
- [ ] Phone 앱과 동기화

### Phase 7: 최적화 및 테스트 (1-2주)

#### 7.1 배터리 최적화
- [ ] 위치 업데이트 간격 최적화 (정지 시 감소)
- [ ] 백그라운드 작업 최소화
- [ ] WorkManager 대신 Service 사용 고려

#### 7.2 오프라인 대응
- [ ] 네트워크 연결 상태 감지
- [ ] 오프라인 시 로컬 저장
- [ ] 연결 복구 시 자동 동기화

#### 7.3 Tile & Complication 개선
- [ ] Tile에 현재 위치 상태 표시
- [ ] Complication에 안전 범위 상태 표시

#### 7.4 테스트
- [ ] Unit Test (Use Cases, ViewModels)
- [ ] Integration Test (Repository)
- [ ] UI Test (Compose)
- [ ] 실제 워치 기기 테스트

---

## 주요 기능별 구현 계획

### 1. 환자 선택 UI

**목표**: 보호자가 관리하는 여러 환자 중 한 명을 선택

**화면 구성**:
- 환자 목록 (LazyColumn)
- 각 환자 카드 (이름, 나이, 관계)
- 선택 버튼

**데이터 흐름**:
```
PatientSelectionScreen
    ↓
PatientViewModel.selectPatient(patientId)
    ↓
PatientRepository.getPatients() (서버 API)
    ↓
DataStore.saveSelectedPatientId()
    ↓
Phone 앱으로 동기화 (DataLayer)
```

**주요 파일**:
- `presentation/ui/main/PatientSelectionScreen.kt`
- `presentation/viewmodel/PatientViewModel.kt`
- `domain/usecase/SelectPatientUseCase.kt`

---

### 2. 메인 화면 아이콘 (전화/지도/도움)

**목표**: 3개 주요 기능으로 빠른 접근

**화면 구성**:
- 3개 큰 버튼 (전화, 지도, 도움)
- 상태 표시 (위치 추적 중, 안전 범위 내)

**네비게이션**:
```kotlin
DashboardScreen
├─ 전화 버튼 → CallScreen
├─ 지도 버튼 → MapScreen
└─ 도움 버튼 → HelpRequestScreen
```

---

### 3. 지도 화면 UI

**목표**: 환자 위치 + 안전 범위 시각화

**화면 구성**:
- TMap 전체 화면
- 환자 현재 위치 마커
- 3단계 안전 범위 원형 오버레이
- 현재 상태 텍스트 (안전 범위 내/외)

**데이터 흐름**:
```
LocationStreamBus.updates
    ↓
MapViewModel.updateLocation()
    ↓
UI 업데이트 (마커 위치, 안전 범위 색상 변경)
```

---

### 4. 환자 위치 표시

**목표**: 실시간 위치를 지도에 표시

**기술 스택**:
- FusedLocationProviderClient (위치 수집)
- LocationStreamBus (앱 내 브로드캐스트)
- TMap POI/Marker (지도 표시)

**업데이트 주기**: 5초

---

### 5. 안전 범위 표시

**목표**: 3단계 안전 구역을 원형으로 표시

**단계**:
- Stage 1: 100m (녹색)
- Stage 2: 350m (노란색)
- Stage 3: 700m (빨간색)

**시각화**:
```kotlin
TMap.addTCircle(
    centerLatitude = homeLatitude,
    centerLongitude = homeLongitude,
    radius = 100.0,
    lineColor = Color.Green,
    fillColor = Color.Green.copy(alpha = 0.2f)
)
```

---

### 6. 통화 중 UI

**목표**: WebRTC 통화 중 간단한 UI 제공

**화면 구성**:
- 상대방 이름
- 통화 시간 (타이머)
- 종료 버튼 (빨간 원형)

---

### 7. 핫라인 통화 기능

**목표**: 보호자/긴급 연락처와 즉시 통화

**흐름**:
```
1. 전화 버튼 클릭
2. CallViewModel.startCall(hotlineNumber)
3. 서버 API: createCall(targetUserId, targetPhone)
4. WebRTC PeerConnection 생성
5. WebSocket 신호 교환 (OFFER/ANSWER/ICE)
6. 오디오 스트림 연결
7. CallScreen 표시
```

---

### 8. 위치 주기적 전송

**목표**: 5초마다 위치를 서버 + Phone 앱으로 전송

**구현**:
```kotlin
// WearLocationTrackingService.kt
override fun onLocationResult(result: LocationResult) {
    result.lastLocation?.let { location ->
        // 1. 앱 내 브로드캐스트
        locationStreamBus.tryEmit(LocationPoint(...))

        // 2. 서버 전송
        locationRepository.updateLocation(...)

        // 3. Phone 앱 동기화
        phoneDataSyncManager.syncLocation(
            latitude = location.latitude,
            longitude = location.longitude
        )
    }
}
```

---

### 9. 배터리 최적화

**전략**:
1. **동적 업데이트 간격**: 이동 중 5초, 정지 중 30초
2. **Sensor Batching**: 위치 업데이트 배칭
3. **Wake Lock 최소화**: 필요 시에만 사용
4. **백그라운드 제한**: Foreground Service로만 동작

**구현**:
```kotlin
private fun adjustLocationUpdateInterval(speedMps: Float) {
    val interval = if (speedMps > 0.5f) {
        5000L // 이동 중: 5초
    } else {
        30000L // 정지 중: 30초
    }

    fusedLocationClient.removeLocationUpdates(locationCallback)
    fusedLocationClient.requestLocationUpdates(
        LocationRequest.Builder(interval).build(),
        locationCallback,
        Looper.getMainLooper()
    )
}
```

---

### 10. 도움 요청 음성 재생

**목표**: TTS로 "도와주세요" 음성 재생

**구현**:
```kotlin
class HelpRequestViewModel @Inject constructor(
    private val textToSpeech: TextToSpeech,
    private val requestHelpUseCase: RequestHelpUseCase
) : ViewModel() {

    fun requestHelp() {
        viewModelScope.launch {
            textToSpeech.speak(
                "도와주세요! 길을 잃었습니다.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "HELP_REQUEST"
            )

            requestHelpUseCase.invoke()
        }
    }
}
```

---

### 11. 화살표 네비게이션

**목표**: 간단한 화살표로 방향 안내

**계산 로직**:
1. 현재 위치 → 다음 waypoint 방향 계산 (Bearing)
2. 현재 기기 방향 (Sensor)
3. 상대 각도 = 목표 방향 - 현재 방향
4. 화살표 회전

**UI**:
- 큰 화살표 아이콘 (120dp)
- 남은 거리 텍스트
- 목적지 이름

---

### 12. 경로 이탈 알림

**목표**: 설정된 경로에서 50m 이상 벗어나면 알림

**구현**:
```kotlin
// NavigationViewModel.kt
init {
    viewModelScope.launch {
        locationStreamBus.updates.collect { location ->
            routeDeviationMonitor.updateLocation(
                location.latitude,
                location.longitude
            )

            if (routeDeviationMonitor.isDeviated()) {
                notificationManager.showRouteDeviationAlert()
                _uiState.update {
                    it.copy(routeDeviated = true)
                }
            }
        }
    }
}
```

---

## 앱-워치 연동 전략

### 1. Wearable DataLayer 동기화

**동기화 데이터**:

#### Phone → Watch
- **로그인 정보** (`/login_data`): 이미 구현됨
- **선택된 환자 정보** (`/selected_patient`): TODO
- **안전 범위 설정** (`/safe_zone_config`): TODO
- **긴급 연락처** (`/emergency_contacts`): TODO

#### Watch → Phone
- **위치 업데이트** (`/location_update`): TODO
- **SOS 알림** (`/sos_alert`): TODO
- **배터리 상태** (`/battery_status`): TODO

**구현 예시**:
```kotlin
// Phone App: WearDataClient.kt
suspend fun syncSafeZoneConfig(config: SafeZoneConfig) {
    val request = PutDataMapRequest.create("/safe_zone_config").apply {
        dataMap.putDouble("home_latitude", config.homeLatitude)
        dataMap.putDouble("home_longitude", config.homeLongitude)
        dataMap.putInt("stage1_radius", config.stage1Radius)
        dataMap.putInt("stage2_radius", config.stage2Radius)
        dataMap.putInt("stage3_radius", config.stage3Radius)
        dataMap.putLong("timestamp", System.currentTimeMillis())
    }

    dataClient.putDataItem(request.asPutDataRequest()).await()
}

// Watch App: SafeZoneConfigSyncManager.kt
class SafeZoneConfigSyncManager @Inject constructor(
    private val dataClient: DataClient,
    private val dataStoreManager: WearDataStoreManager
) : DataClient.OnDataChangedListener {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/safe_zone_config") {

                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val config = SafeZoneConfig(
                    homeLatitude = dataMap.getDouble("home_latitude"),
                    homeLongitude = dataMap.getDouble("home_longitude"),
                    stage1Radius = dataMap.getInt("stage1_radius"),
                    stage2Radius = dataMap.getInt("stage2_radius"),
                    stage3Radius = dataMap.getInt("stage3_radius")
                )

                // DataStore 저장
                lifecycleScope.launch {
                    dataStoreManager.saveSafeZoneConfig(config)
                }
            }
        }
    }
}
```

### 2. MessageClient 양방향 통신

**실시간 메시지 전송** (DataLayer보다 빠름):

#### Phone → Watch
- **통화 시작 알림**: FCM + MessageClient
- **긴급 알림**: SOS 상황 발생 시
- **경로 업데이트**: 네비게이션 경로 변경

#### Watch → Phone
- **즉시 SOS 전송**: 도움 요청 버튼 클릭
- **통화 종료 알림**: 워치에서 통화 종료 시

**구현 예시**:
```kotlin
// Watch App: Send SOS to Phone
suspend fun sendSosToPhone() {
    val message = "SOS:${latitude},${longitude}"

    val nodes = nodeClient.connectedNodes.await()
    nodes.forEach { node ->
        messageClient.sendMessage(
            node.id,
            "/sos_alert",
            message.toByteArray()
        ).await()
    }
}

// Phone App: Receive SOS from Watch
class WearMessageListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            "/sos_alert" -> {
                val message = String(messageEvent.data)
                val (lat, lng) = message.split(":")[1].split(",")

                // 보호자에게 알림 표시
                showSosNotification(lat.toDouble(), lng.toDouble())
            }
        }
    }
}
```

### 3. 채널 별 사용 전략

| 데이터 유형 | 채널 | 이유 |
|------------|------|------|
| 로그인 정보 | DataLayer | 자동 동기화, 영구 저장 |
| 안전 범위 설정 | DataLayer | 자동 동기화, 영구 저장 |
| 위치 업데이트 (주기) | REST API | 서버 저장 필요 |
| 위치 업데이트 (실시간) | MessageClient | Phone 앱 실시간 표시용 |
| SOS 알림 | MessageClient + REST API | 즉시 전송 + 서버 저장 |
| 통화 시작 | FCM + MessageClient | Phone에서 Watch 깨우기 |
| 통화 신호 (WebRTC) | WebSocket | 실시간 신호 교환 |

---

## 공통 모듈 활용 전략

### Common 모듈 제공 기능

#### 1. LocationPoint
**용도**: 표준 위치 데이터 모델

```kotlin
// Wear App에서 사용
val locationPoint = LocationPoint(
    latitude = location.latitude,
    longitude = location.longitude,
    accuracyMeters = location.accuracy,
    bearing = location.bearing,
    speedMps = location.speed,
    timeMillis = System.currentTimeMillis()
)
locationStreamBus.emit(locationPoint)
```

#### 2. LocationStreamBus
**용도**: 앱 내 위치 브로드캐스트 (여러 화면/서비스 간 공유)

```kotlin
// WearLocationTrackingService.kt
@Inject lateinit var locationStreamBus: LocationStreamBus

override fun onLocationResult(result: LocationResult) {
    val point = LocationPoint(...)
    locationStreamBus.tryEmit(point) // 발행
}

// MapViewModel.kt
init {
    viewModelScope.launch {
        locationStreamBus.updates.collect { point ->
            _uiState.update {
                it.copy(
                    currentLatitude = point.latitude,
                    currentLongitude = point.longitude
                )
            }
        }
    }
}
```

#### 3. NavigationRouteManager
**용도**: 현재 네비게이션 경로 관리

```kotlin
// NavigationViewModel.kt
@Inject lateinit var navigationRouteManager: NavigationRouteManager

fun startNavigation(navigationId: String, path: List<LatLng>) {
    navigationRouteManager.setRoute(navigationId, path)
}

fun endNavigation() {
    navigationRouteManager.clearRoute()
}
```

#### 4. RouteDeviationMonitor
**용도**: 경로 이탈 감지 (50m 기준)

```kotlin
// NavigationViewModel.kt
@Inject lateinit var routeDeviationMonitor: RouteDeviationMonitor

init {
    // 경로 설정
    val route = navigationRouteManager.currentRoute.value
    routeDeviationMonitor.setRoute(route?.path)

    // 위치 업데이트 감지
    viewModelScope.launch {
        locationStreamBus.updates.collect { location ->
            routeDeviationMonitor.updateLocation(
                location.latitude,
                location.longitude
            )

            if (routeDeviationMonitor.isDeviated()) {
                _uiState.update { it.copy(routeDeviated = true) }
                notificationManager.showRouteDeviationAlert()
            }
        }
    }
}
```

#### 5. SafetyZoneMonitor
**용도**: 3단계 안전 범위 모니터링

```kotlin
// MapViewModel.kt
@Inject lateinit var safetyZoneMonitor: SafetyZoneMonitor

fun setHome(latitude: Double, longitude: Double) {
    safetyZoneMonitor.setHome(latitude, longitude)
}

init {
    viewModelScope.launch {
        locationStreamBus.updates.collect { location ->
            safetyZoneMonitor.updateLocation(
                location.latitude,
                location.longitude,
                location.timeMillis
            )

            val status = safetyZoneMonitor.getCurrentStatus()
            _uiState.update {
                it.copy(safeZoneStatus = status)
            }

            // 알림 발송
            if (status.stage3OutsideDurationMinutes != null) {
                notificationManager.showSafeZoneAlert(stage = 3)
            }
        }
    }
}
```

### Common 모듈 확장 제안

앱과 워치가 공통으로 사용할 수 있는 추가 기능:

1. **DistanceCalculator**: Haversine 거리 계산 유틸리티
2. **BearingCalculator**: 방향 계산 유틸리티
3. **LocationValidator**: 위치 데이터 검증 (GPS 점프 감지)
4. **SafeZoneConfig**: 안전 범위 설정 데이터 모델
5. **NavigationState**: 네비게이션 상태 enum

---

## 주요 기술 스택

### Wear Module 의존성

```kotlin
dependencies {
    // Wear OS
    implementation("com.google.android.wearable:wearable:2.9.0")
    implementation("com.google.android.gms:play-services-wearable:18.1.0")

    // Wear Compose
    implementation("androidx.wear.compose:compose-material:1.3.0")
    implementation("androidx.wear.compose:compose-foundation:1.3.0")

    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // WebSocket (VoIP 신호)
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")

    // WebRTC (통화)
    implementation("org.webrtc:google-webrtc:1.0.32006")

    // DI
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.0")

    // Common Module
    implementation(project(":common"))

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // TTS (도움 요청 음성)
    // Android TTS는 기본 제공
}
```

### 앱 모듈에서 재사용 가능한 코드

#### 1. Network Layer
- `RetrofitClient.kt`: Retrofit 설정
- `AuthInterceptor.kt`: JWT 토큰 자동 주입
- `WebSocketManager.kt`: STOMP WebSocket 관리
- `VoipSignalingService.kt`: WebRTC 신호 처리

#### 2. WebRTC
- `WebRtcCallClient.kt`: PeerConnection 관리
- `WebRtcModule.kt`: WebRTC 초기화

#### 3. Location
- `LocationTrackingService.kt`: Foreground Service
- `SafetyZoneMonitor.kt`: 안전 범위 모니터링 (Common 모듈에도 있음)

#### 4. Data Models
- DTOs (LocationDto, CallDto 등)
- Domain Models

---

## 개발 우선순위

### 1순위: 핵심 기능 (필수)
- [x] 로그인 동기화
- [ ] 위치 추적 Service
- [ ] 지도 화면 (환자 위치 표시)
- [ ] 안전 범위 모니터링
- [ ] 메인 대시보드 (3개 버튼)

### 2순위: 주요 기능
- [ ] VoIP 통화 (핫라인)
- [ ] 화살표 네비게이션
- [ ] 경로 이탈 알림
- [ ] 도움 요청 (TTS)

### 3순위: 부가 기능
- [ ] 환자 선택 (보호자용)
- [ ] SOS 알림 자동화
- [ ] Tile/Complication 개선
- [ ] 배터리 최적화

---

## 참고 문서

### App Module 주요 파일
- [LocationTrackingService.kt](../app/src/main/java/kr/co/ongil/service/location/LocationTrackingService.kt)
- [WebRtcCallClient.kt](../app/src/main/java/kr/co/ongil/core/webrtc/WebRtcCallClient.kt)
- [VoipSignalingService.kt](../app/src/main/java/kr/co/ongil/data/datasource/websocket/VoipSignalingService.kt)
- [MapViewModel.kt](../app/src/main/java/kr/co/ongil/presentation/ui/location/MapViewModel.kt)
- [WearDataClient.kt](../app/src/main/java/kr/co/ongil/data/datasource/wear/WearDataClient.kt)

### Common Module 주요 파일
- [LocationPoint.kt](../common/src/main/java/kr/co/ongil/common/location/LocationPoint.kt)
- [LocationStreamBus.kt](../common/src/main/java/kr/co/ongil/common/location/LocationStreamBus.kt)
- [NavigationRouteManager.kt](../common/src/main/java/kr/co/ongil/common/location/NavigationRouteManager.kt)
- [RouteDeviationMonitor.kt](../common/src/main/java/kr/co/ongil/common/location/RouteDeviationMonitor.kt)
- [SafetyZoneMonitor.kt](../common/src/main/java/kr/co/ongil/common/location/SafetyZoneMonitor.kt)

### 외부 문서
- [Wear OS Developer Guide](https://developer.android.com/training/wearables)
- [WebRTC Android Guide](https://webrtc.github.io/webrtc-org/native-code/android/)
- [TMap API Documentation](https://tmapapi.sktelecom.com/)
- [Google Play Services Location](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary)

---

## 개발 시작하기

### 1. 환경 설정
```bash
# local.properties에 API 키 추가 (이미 있음)
TMAP_API_KEY=your_tmap_api_key
API_BASE_URL=https://staging.on-gil.co.kr/api

# Wear OS Emulator 또는 실제 기기 연결
adb devices

# Gradle Sync
./gradlew :wear:build
```

### 2. 첫 번째 작업: 네트워크 레이어 구축
```bash
# TODO 파일 생성
wear/src/main/java/kr/co/ongil/wear/data/datasource/remote/
├── RetrofitClient.kt
├── api/
│   ├── WearLocationApi.kt
│   └── WearCallApi.kt
└── websocket/
    └── WearWebSocketManager.kt
```

### 3. 단계별 개발
Phase 1부터 순서대로 진행하면서 각 단계마다:
1. 필요한 파일 생성
2. 앱 모듈 코드 참고하여 워치에 맞게 수정
3. Common 모듈 통합
4. 테스트
5. 다음 단계로 이동

---

**문서 작성일**: 2025-11-16
**작성자**: Claude (Anthropic)
**프로젝트**: 온길(Ongil) Wear OS App Development
