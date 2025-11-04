# 온길 (OnGil) 프로젝트 구조

```
kr.co.ongil/
│
├── OngilApplication.kt
│
├── core/                                    # 핵심 유틸리티 및 확장
│   ├── constants/
│   │   └── Constants.kt
│   ├── extensions/
│   │   └── Extensionts.kt
│   └── utils/
│       ├── NetworkUtiols.kt
│       ├── StringFormatUtils.kt
│       └── ValidationUtils.kt
│
├── data/                                    # 데이터 레이어
│   ├── datasource/
│   │   ├── local/
│   │   │   ├── database/
│   │   │   │   ├── OngilDatabase.kt
│   │   │   │   ├── dao/
│   │   │   │   │   └── UserDao.kt
│   │   │   │   └── entity/
│   │   │   │       └── UserEntity.kt
│   │   │   └── preferences/
│   │   │       └── Preferences.kt
│   │   ├── remote/
│   │   │   ├── RetrofitClient.kt
│   │   │   └── api/
│   │   │       ├── AuthApi.kt
│   │   │       ├── CallApi.kt
│   │   │       └── UserApi.kt
│   │   └── wear/
│   │       ├── WearDataClient.kt
│   │       └── WearMessageClient.kt
│   ├── mapper/
│   │   ├── CallDetailMapper.kt
│   │   ├── CallLogMapper.kt
│   │   └── UserMapper.kt
│   ├── model/
│   │   ├── auth/
│   │   │   ├── ChangePasswordRequest.kt
│   │   │   ├── ChangePasswordResponse.kt
│   │   │   ├── LogoutRequest.kt
│   │   │   ├── LogoutResponse.kt
│   │   │   ├── VerificationRequest.kt
│   │   │   └── VerificationResponse.kt
│   │   ├── call/
│   │   │   ├── CallDetailDto.kt
│   │   │   ├── CallDetailResponse.kt
│   │   │   ├── CallLocationDto.kt
│   │   │   ├── CallLogDto.kt
│   │   │   └── CallLogResponse.kt
│   │   ├── error/
│   │   │   ├── ApiError.kt
│   │   │   └── ApiException.kt
│   │   ├── favorite/
│   │   │   ├── FavoritePatientDto.kt
│   │   │   └── FavoritePlaceDto.kt
│   │   └── user/
│   │       ├── UserDto.kt
│   │       └── UserResponse.kt
│   ├── repository/
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── CallRepositoryImpl.kt
│   │   ├── FakeSearchUserRepository.kt
│   │   ├── FavoriteRepository.kt
│   │   ├── UserRepositoryImpl.kt
│   │   └── fake/
│   │       └── FakeSearchUserRepository.kt
│   └── util/
│       └── ErrorHandler.kt
│
├── di/                                      # 의존성 주입
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
│
├── domain/                                  # 도메인 레이어
│   ├── model/
│   │   ├── User.kt
│   │   └── UserSummary.kt
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── CallRepository.kt
│   │   ├── SearchUserRepository.kt
│   │   └── UserRepository.kt
│   └── usecase/
│       ├── sync/
│       │   ├── GetWearConnectionStatusUseCase.kt
│       │   └── SyncWithWearUseCase.kt
│       └── user/
│           ├── GetUserUseCase.kt
│           └── UpdateUserUseCase.kt
│
├── presentation/                            # 프레젠테이션 레이어
│   ├── MainActivity.kt
│   ├── navigation/
│   │   ├── BottomNavItem.kt
│   │   ├── MainScreen.kt
│   │   ├── NavGraph.kt
│   │   └── Routes.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── ui/
│   │   ├── Atest/                          # 테스트용 화면
│   │   │   ├── PlayGroundMJ.kt
│   │   │   └── PlayGroundSH.kt
│   │   ├── common/                         # 공통 컴포넌트
│   │   │   ├── AlertModal.kt
│   │   │   ├── GreenButton.kt
│   │   │   ├── GreyButton.kt
│   │   │   ├── InputBox.kt
│   │   │   ├── LabeledOutlinedField.kt
│   │   │   ├── TopBar.kt
│   │   │   ├── alert/
│   │   │   │   └── AlertInfo.kt
│   │   │   ├── bottomnav/
│   │   │   │   ├── OngilBottomBar.kt
│   │   │   │   └── bottomnav.kt
│   │   │   ├── calledinfo/
│   │   │   │   └── CalledInfoCard.kt
│   │   │   ├── favorite/
│   │   │   │   ├── PatientCard.kt
│   │   │   │   └── PlaceCard.kt
│   │   │   ├── map/
│   │   │   │   ├── SearchBar.kt
│   │   │   │   └── SearchList.kt
│   │   │   ├── patientinfo/
│   │   │   │   └── InfoCard.kt
│   │   │   └── selection/
│   │   │       ├── PatientSelectCard.kt
│   │   │       ├── PatientSelectModal.kt
│   │   │       └── PatientSelectViewModel.kt
│   │   ├── favorite/                       # 즐겨찾기 화면
│   │   │   ├── FavoriteScreen.kt
│   │   │   ├── FavoriteTabBar.kt
│   │   │   ├── FavoriteUiEvent.kt
│   │   │   ├── FavoriteUiState.kt
│   │   │   ├── FavoriteViewModel.kt
│   │   │   ├── FavortieDummyFactory.kt
│   │   │   ├── PatientData.kt
│   │   │   ├── PatientList.kt
│   │   │   ├── PlaceData.kt
│   │   │   └── PlaceList.kt
│   │   ├── home/                           # 홈 화면
│   │   │   ├── HomeEvent.kt
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeUiState.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── myinfo/                         # 내 정보 화면
│   │   │   ├── CallDetailScreen.kt
│   │   │   ├── CallListItem.kt
│   │   │   ├── ChangePasswordScreen.kt
│   │   │   ├── MyInfoEditScreen.kt
│   │   │   ├── MyInfoScreen.kt
│   │   │   ├── MyInfoUiState.kt
│   │   │   ├── PhoneVerificationSection.kt
│   │   │   └── RecentCallsScreen.kt
│   │   ├── patientdetail/                  # 환자 상세 화면
│   │   │   ├── PatientDetailScreen.kt
│   │   │   ├── PatientDetailUiState.kt
│   │   │   └── PatientDetailViewModel.kt
│   │   ├── placedetail/                    # 장소 상세 화면
│   │   │   ├── PlaceDetailScreen.kt
│   │   │   ├── PlaceDetailUiState.kt
│   │   │   └── PlaceDetailViewModel.kt
│   │   ├── searchuser/                     # 사용자 검색 화면
│   │   │   ├── SearchUserScreen.kt
│   │   │   ├── SearchUserSideEffect.kt
│   │   │   ├── SearchUserUiEvent.kt
│   │   │   ├── SearchUserUiState.kt
│   │   │   └── SearchUserViewModel.kt
│   │   └── signup/                         # 회원가입 화면
│   │       ├── SignupScreen.kt
│   │       ├── SignupUiState.kt
│   │       └── SignupViewModel.kt
│   ├── uistate/                            # 공통 UI 상태
│   │   ├── CallDetailUiState.kt
│   │   ├── ChangePasswordUiState.kt
│   │   ├── MyInfoEditUiState.kt
│   │   └── RecentCallsUiState.kt
│   └── viewmodel/                          # 공통 ViewModel
│       ├── CallDetailViewModel.kt
│       ├── ChangePasswordViewModel.kt
│       ├── MyInfoEditViewModel.kt
│       ├── MyInfoViewModel.kt
│       └── RecentCallsViewModel.kt
│
├── service/                                 # 백그라운드 서비스
│   ├── call/
│   │   └── CallService.kt
│   ├── location/
│   │   └── LocationTrackingService.kt
│   └── notification/
│       └── FcmService.kt
│
└── worker/                                  # WorkManager 작업
    ├── HealthDataSyncWorker.kt
    └── LocationSyncWorker.kt
```

## 패키지별 설명

### 📦 **core**
- constants: 앱 전역 상수
- extensions: Kotlin 확장 함수
- utils: 유틸리티 클래스

### 📦 **data**
- datasource: 데이터 소스 (로컬 DB, API, Wear OS)
- mapper: DTO ↔ Domain 변환
- model: API 응답/요청 모델
- repository: Repository 구현체

### 📦 **domain**
- model: 도메인 모델
- repository: Repository 인터페이스
- usecase: 비즈니스 로직

### 📦 **presentation**
- navigation: 화면 네비게이션
- theme: 테마 및 색상
- ui: UI 화면 및 컴포넌트
- uistate: UI 상태 클래스
- viewmodel: ViewModel 클래스

### 📦 **service**
- call: 통화 서비스
- location: 위치 추적 서비스
- notification: FCM 푸시 알림

### 📦 **worker**
- 주기적 백그라운드 작업 (건강 데이터, 위치 동기화)

---

**총 파일 수**: 약 135개의 Kotlin 파일
