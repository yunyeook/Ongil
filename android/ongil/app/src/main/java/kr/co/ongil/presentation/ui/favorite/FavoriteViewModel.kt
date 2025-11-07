package kr.co.ongil.presentation.ui.favorite

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.domain.repository.FavoriteRepository
import kr.co.ongil.domain.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialPatientId: Long = savedStateHandle["patientId"] ?: 0L
    private var lastLoadedPatientId: Long? = null

    private val _uiState = MutableStateFlow(
        FavoriteUiState(
            selectedTab = FavoriteTab.PATIENTS,
            patients = emptyList(),
            places = emptyList()
        )
    )
    val uiState: StateFlow<FavoriteUiState> = _uiState

    init {
        loadUserInfo()
        loadData(initialPatientId)
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            userRepository.getMyInfo()
                .onSuccess { userDto ->
                    _uiState.update {
                        it.copy(
                            userName = userDto.name,
                            userType = userDto.userType
                        )
                    }
                    Log.d("FavoriteViewModel", "사용자 정보 로드 성공: name=${userDto.name}, type=${userDto.userType}")
                }
                .onFailure { error ->
                    Log.e("FavoriteViewModel", "사용자 정보 로드 실패", error)
                }
        }
    }

    fun loadData(patientId: Long) {
        if (_uiState.value.isLoading) return
        lastLoadedPatientId = patientId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentPatientId = patientId) }
            val currentPatients = _uiState.value.patients
            val result = favoriteRepository.getFavoritePlaces(patientId)
            result.fold(
                onSuccess = { placesDomain ->
                    _uiState.update {
                        it.copy(
                            patients = currentPatients,
                            places = placesDomain.items,
                            currentPatientId = patientId,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "오류가 발생했습니다."
                        )
                    }
                }
            )
        }
    }

    fun onPatientChanged(newPatientId: Long) {
        if (lastLoadedPatientId != newPatientId) {
            loadData(newPatientId)
        }
    }

    fun refresh() {
        lastLoadedPatientId?.let { loadData(it) }
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
