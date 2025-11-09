package kr.co.ongil.presentation.ui.favorite

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
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
        loadRelationships() // 사용자(환자/보호자) 목록 불러오기

        // initialPatientId가 0L이면 로그인한 사용자의 ID를 사용
        if (initialPatientId == 0L) {
            viewModelScope.launch {
                userRepository.getMyInfo()
                    .collect { result ->
                        result.onSuccess { userDto ->
                            val userId = userDto.id.toLong()
                            if (lastLoadedPatientId == null) {
                                loadData(userId)
                            }
                        }
                    }
            }
        } else {
            loadData(initialPatientId)
        }
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            userRepository.getMyInfo()
                .onEach { result ->
                    result.onSuccess { userDto ->
                                _uiState.update {
                                    it.copy(
                                        userName = userDto.name,
                                        userType = userDto.userType
                                    )
                                }
                                Log.d("FavoriteViewModel", "사용자 정보 로드 성공: name=${userDto.name}, type=${userDto.userType}")
                            }.onFailure { error ->
                                Log.e("FavoriteViewModel", "사용자 정보 로드 실패", error)
                            }
                    }
                    .collect()
        }
    }

    private fun loadRelationships() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = favoriteRepository.getMyRelationships()
            result.fold(
                onSuccess = { relationships ->
                    _uiState.update {
                        it.copy(
                            patients = relationships,
                            isLoading = false
                        )
                    }
                    Log.d("FavoriteViewModel", "사용자 목록 로드 성공: ${relationships.size}명")
                },
                onFailure = { error ->
                    Log.e("FavoriteViewModel", "사용자 목록 로드 실패", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "사용자 목록 조회 실패"
                        )
                    }
                }
            )
        }
    }

    fun loadData(patientId: Long, force: Boolean = false) {
        if (!force && _uiState.value.isLoading) return
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
        loadRelationships() // 사용자 목록 새로고침

        // 장소 목록 새로고침
        val patientIdToLoad = lastLoadedPatientId ?: _uiState.value.currentPatientId
        if (patientIdToLoad != 0L) {
            loadData(patientIdToLoad, force = true)
        } else {
            // currentPatientId도 0L이면 사용자 ID를 다시 가져와서 로드
            viewModelScope.launch {
                userRepository.getMyInfo()
                    .collect { result ->
                        result.onSuccess { userDto ->
                            val userId = userDto.id.toLong()
                            loadData(userId, force = true)
                        }
                    }
            }
        }
    }

    fun updatePlaceLocally(favoriteId: Long, placeAlias: String, isDefault: Boolean) {
        Log.d("FavoriteViewModel", "updatePlaceLocally 호출 - favoriteId=$favoriteId, placeAlias=$placeAlias, isDefault=$isDefault")
        _uiState.update { state ->
            val updatedPlaces = state.places.map { place ->
                if (place.favoriteId == favoriteId) {
                    Log.d("FavoriteViewModel", "장소 발견 - 기존 placeAlias=${place.placeAlias} -> 새로운 placeAlias=$placeAlias")
                    place.copy(
                        placeAlias = placeAlias,
                        isDefault = isDefault
                    )
                } else {
                    // isDefault가 true로 설정된 경우 다른 장소들의 isDefault를 false로 변경
                    if (isDefault) {
                        place.copy(isDefault = false)
                    } else {
                        place
                    }
                }
            }
            Log.d("FavoriteViewModel", "로컬 업데이트 완료 - 총 ${updatedPlaces.size}개 장소")
            state.copy(places = updatedPlaces)
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
