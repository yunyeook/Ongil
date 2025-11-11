package kr.co.ongil.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.domain.repository.UserRepository
import kr.co.ongil.domain.repository.FavoriteRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userDataStoreManager: UserDataStoreManager,
    private val userRepository: UserRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            guardianName = "",
            patientName = "",
            mostVisitedLabel = "가장 많이 방문한 목적지",
            mostVisitedPlace = "집",
            outOfSafeZoneCount = 0,
            routeFailCount = 0
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            // 사용자 정보, 환자 목록, 선택된 환자 ID를 결합
            combine(
                userRepository.getMyInfo(),
                userDataStoreManager.getSelectedPatientId(),
                flow {
                    val result = favoriteRepository.getMyRelationships()
                    emit(result.getOrNull() ?: emptyList())
                }
            ) { userInfoResult, selectedId, patients ->
                Triple(userInfoResult, selectedId, patients)
            }.collect { (userInfoResult, selectedId, patients) ->
                // 로그인한 사용자 이름
                val guardianName = userInfoResult?.getOrNull()?.name ?: ""

                // 선택된 환자의 relationshipName (PatientData.name에 저장됨)
                val selectedPatient = patients.find { it.id.toString() == selectedId }
                val patientName = selectedPatient?.name ?: ""

                _uiState.value = _uiState.value.copy(
                    guardianName = guardianName,
                    patientName = patientName
                )
            }
        }
    }
}
