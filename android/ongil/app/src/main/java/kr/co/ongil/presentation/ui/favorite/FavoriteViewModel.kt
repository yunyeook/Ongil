package kr.co.ongil.presentation.ui.favorite

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.domain.repository.FavoriteRepository
import kr.co.ongil.domain.repository.UserRepository
import kr.co.ongil.data.util.ErrorHandler
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val userRepository: UserRepository,
    private val userDataStoreManager: kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager,
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // userType을 먼저 로드
            val initialUserType = userDataStoreManager.getUserType().first() ?: "GUARDIAN"
            _uiState.update { it.copy(userType = initialUserType) }

            // 세 가지 작업을 병렬로 실행
            val userInfoJob = launch { loadUserInfoInternal() }
            val relationshipsJob = launch { loadRelationshipsInternal() }

            val dataJob = launch {
                if (initialPatientId != 0L) {
                    Log.d("FavoriteViewModel", "initialPatientId로 로드: $initialPatientId")
                    loadDataInternal(initialPatientId)
                } else {
                    val userType = userDataStoreManager.getUserType().first()
                    Log.d("FavoriteViewModel", "사용자 타입 확인: $userType")

                    if (userType == "GUARDIAN") {
                        val selectedId = userDataStoreManager.getSelectedPatientId().first()
                        val patientId = selectedId?.toLongOrNull()

                        if (patientId != null) {
                            Log.d("FavoriteViewModel", "보호자 초기 로드 - 선택된 환자 ID: $patientId")
                            loadDataInternal(patientId)
                        } else {
                            Log.w("FavoriteViewModel", "선택된 환자 ID가 없습니다")
                        }
                    } else {
                        userRepository.getMyInfo()
                            .first()
                            .onSuccess { userDto ->
                                val userId = userDto.id.toLong()
                                Log.d("FavoriteViewModel", "환자 초기 로드 - 사용자 ID: $userId")
                                loadDataInternal(userId)
                            }
                            .onFailure { error ->
                                Log.e("FavoriteViewModel", "사용자 정보 조회 실패", error)
                            }
                    }
                }
            }

            // 모든 작업이 완료될 때까지 대기
            userInfoJob.join()
            relationshipsJob.join()
            dataJob.join()

            // 모든 로딩 완료
            _uiState.update { it.copy(isLoading = false) }
        }

        // 선택된 환자 ID 변경 감지 (보호자 전용 - 초기 로드 이후)
        viewModelScope.launch {
            val userType = userDataStoreManager.getUserType().first()
            if (userType == "GUARDIAN") {
                userDataStoreManager.getSelectedPatientId().collect { selectedId ->
                    val patientId = selectedId?.toLongOrNull()

                    if (patientId != null && patientId != lastLoadedPatientId) {
                        Log.d("FavoriteViewModel", "선택된 환자 변경 감지: $patientId")
                        loadData(patientId)
                    }
                }
            }
        }
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            loadUserInfoInternal()
        }
    }

    private fun loadRelationships() {
        viewModelScope.launch {
            loadRelationshipsInternal()
        }
    }

    fun loadData(patientId: Long, force: Boolean = false) {
        viewModelScope.launch {
            loadDataInternal(patientId)
        }
    }
    private suspend fun loadUserInfoInternal() {
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

    private suspend fun loadRelationshipsInternal() {
        val result = favoriteRepository.getMyRelationships()
        result.fold(
            onSuccess = { relationships ->
                relationships.forEach { patient ->
                    userDataStoreManager.saveProfileImage(
                        userId = patient.id.toString(),
                        profileImageUrl = patient.profileImage
                    )
                }

                val sortedRelationships = relationships.sortedByDescending { it.isDefault }

                _uiState.update {
                    it.copy(patients = sortedRelationships)
                }
                Log.d("FavoriteViewModel", "사용자 목록 로드 성공: ${relationships.size}명")
            },
            onFailure = { error ->
                Log.e("FavoriteViewModel", "사용자 목록 로드 실패", error)
                val exception = ErrorHandler.handleException(error as? Exception ?: Exception(error))
                val userMessage = exception.message
                val shouldShowError = userMessage != null && userMessage.matches(Regex(".*[가-힣]+.*"))

                _uiState.update {
                    it.copy(error = if (shouldShowError) userMessage else null)
                }
            }
        )
    }

    private suspend fun loadDataInternal(patientId: Long) {
        lastLoadedPatientId = patientId
        _uiState.update { it.copy(error = null, currentPatientId = patientId) }
        val result = favoriteRepository.getFavoritePlaces(patientId)
        result.fold(
            onSuccess = { placesDomain ->
                val sortedPlaces = placesDomain.items.sortedByDescending { it.isDefault }

                _uiState.update {
                    it.copy(
                        places = sortedPlaces,
                        currentPatientId = patientId,
                        error = null
                    )
                }
            },
            onFailure = { throwable ->
                val exception = ErrorHandler.handleException(throwable as? Exception ?: Exception(throwable))
                val userMessage = exception.message
                val shouldShowError = userMessage != null && userMessage.matches(Regex(".*[가-힣]+.*"))

                _uiState.update {
                    it.copy(error = if (shouldShowError) userMessage else null)
                }
            }
        )
    }


    fun onPatientChanged(newPatientId: Long) {
        if (lastLoadedPatientId != newPatientId) {
            loadData(newPatientId)
        }
    }

    fun refresh() {
        loadRelationships() // 사용자 목록 새로고침

        viewModelScope.launch {
            val userType = userDataStoreManager.getUserType().first()
            Log.d("FavoriteViewModel", "refresh - userType: $userType")

            val patientId = if (userType == "GUARDIAN") {
                userDataStoreManager.getSelectedPatientId().first()?.toLongOrNull()
            } else {
                userRepository.getMyInfo().first().getOrNull()?.id?.toLong()
            }

            if (patientId != null) {
                Log.d("FavoriteViewModel", "refresh - patientId로 로드: $patientId")
                loadData(patientId, force = true)
            } else {
                Log.w("FavoriteViewModel", "refresh - patientId를 가져올 수 없음")
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
            }.sortedByDescending { it.isDefault } // 기본 목적지를 맨 위로 정렬
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