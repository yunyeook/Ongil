package kr.co.ongil.presentation.ui.favorite

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// Hilt 주입은 나중에 붙일 예정이므로 현재는 기본 ViewModel만 사용
class FavoriteViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        FavoriteUiState(
            selectedTab = FavoriteTab.PATIENTS,
            patients = dummyPatients(),
            places = dummyPlaces()
        )
    )

    val uiState: StateFlow<FavoriteUiState> = _uiState

    fun onEvent(event: FavoriteUiEvent) {
        when (event) {
            is FavoriteUiEvent.OnTabSelected -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
            }

            is FavoriteUiEvent.OnCallClick -> {
                // 추후: 전화 Intent를 트리거할 수 있도록
                // Screen 쪽에 콜백을 노출하거나, 일단 로그/추적만
                // 지금 단계에서는 ViewModel 내부 처리만 남겨둠
            }

            FavoriteUiEvent.OnAddPatientClick -> {
                // 추후: "새로운 환자 등록" 화면으로 이동 트리거
                // 네비게이션 이벤트를 상위로 올릴 수 있도록 계획
            }

            FavoriteUiEvent.OnAddPlaceClick -> {
                // 추후: "새 장소 등록" 흐름
            }
        }
    }
}

// 더미 데이터 (MVP 뷰 확인용)
private fun dummyPatients(): List<PatientItem> = listOf(
    PatientItem(
        id = 1L,
        displayName = "김철수 (남, 75세)",
        phoneNumber = "010-1234-5678"
    ),
    PatientItem(
        id = 2L,
        displayName = "이영희 (여, 68세)",
        phoneNumber = "010-2345-6789"
    ),
    PatientItem(
        id = 3L,
        displayName = "박민수 (남, 82세)",
        phoneNumber = "010-3456-7890"
    )
)

private fun dummyPlaces(): List<PlaceItem> = listOf(
    PlaceItem(
        id = 101L,
        label = "집",
        address = "서울시 서초구 ..."
    ),
    PlaceItem(
        id = 102L,
        label = "병원",
        address = "OO병원 신관 2F 신경과"
    )
)