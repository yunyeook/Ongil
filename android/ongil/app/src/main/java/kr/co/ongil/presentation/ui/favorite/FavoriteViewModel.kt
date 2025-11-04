package kr.co.ongil.presentation.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.data.repository.FakeFavoriteRepository
class FavoriteViewModel(
    private val fakeFavoriteRepository: FakeFavoriteRepository,
    private val initialPatientId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FavoriteUiState(
            selectedTab = FavoriteTab.PATIENTS,
            patients = emptyList(),
            places = emptyList()
        )
    )
    val uiState: StateFlow<FavoriteUiState> = _uiState
    // TODO 실제 즐겨찾기 목록 가져오는 코드 작성해야됨

    init {
        loadData((initialPatientId))
    }

    fun loadData(patientId: Long) {
        viewModelScope.launch {
            // 로컬 저장소(Repository)에서 즐겨찾기 환자 / 장소 목록 불러오기
            val patients = fakeFavoriteRepository.getFavoritePatients()
            val places = fakeFavoriteRepository.getFavoritePlaces(patientId)

            _uiState.update {
                it.copy(
                    patients = patients,
                    places = places
                )
            }
        }
    }

    fun onEvent(event: FavoriteUiEvent) {
        when (event) {
            is FavoriteUiEvent.OnTabSelected -> {
                _uiState.update { it.copy(selectedTab = event.tab) }
            }

            is FavoriteUiEvent.OnCallClick -> {
                // TODO 전화걸기 로직 넣기
            }

            is FavoriteUiEvent.OnPatientCardClick -> {
                // TODO 환자 상세 화면으로 네비게이션 로직 넣기
            }

            FavoriteUiEvent.onGoSearchUserClick -> {
                // TODO 새로운 환자 등록 로직 넣기
            }

            FavoriteUiEvent.OnAddPlaceClick -> {
                // TODO 새 장소 등록 로직 넣기
            }
        }
    }
}
