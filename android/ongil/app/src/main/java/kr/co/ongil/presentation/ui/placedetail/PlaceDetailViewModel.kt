package kr.co.ongil.presentation.ui.placedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kr.co.ongil.domain.repository.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kr.co.ongil.domain.model.placedetail.PlaceDetailUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.SavedStateHandle
import javax.inject.Inject

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val repository: FavoriteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialFavoriteId: Long = savedStateHandle["favoriteId"] ?: 0L
    private val initialPlaceName: String = savedStateHandle["placeName"] ?: ""
    private val initialAddress: String = savedStateHandle["address"] ?: ""
    private val initialIsDefault: Boolean = savedStateHandle["isDefault"] ?: false
    private val initialPatientId: Long = savedStateHandle["patientId"] ?: 0L

    private val _uiState = MutableStateFlow(
        PlaceDetailUiState(
            favoriteId = initialFavoriteId,
            placeName = initialPlaceName,
            address = initialAddress,
            isDefault = initialIsDefault,
            patientId = initialPatientId
        )
    )
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val current = _uiState.value
            val result = repository.getPlaceDetail(
                patientId = current.patientId,
                favoriteId = current.favoriteId
            )
            result.fold(
                onSuccess = { detail ->
                    _uiState.update {
                        it.copy(
                            placeName = detail.placeName,
                            address = detail.address,
                            isDefault = detail.isDefault,
                            favoriteId = detail.favoriteId,
                            patientId = detail.patientId,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "상세 조회 실패") }
                }
            )
        }
    }

    // 기본목적지 설정
    fun setAsDefault() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isDefault = !current.isDefault,
                    successMessage = if (!current.isDefault) "기본 목적지로 설정되었습니다." else null
                )
            }
        }
    }

    // 저장하기
    fun updatePlaceInfo(newName: String, newAddress: String) {
        if (_uiState.value.isLoading) return
        val prev = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
        _uiState.update { current ->
            current.copy(
                placeName = newName,
                address = newAddress
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updatePlaceDetail(
                patientId = prev.patientId,
                favoriteId = prev.favoriteId,
                update = PlaceDetailUpdate(
                    placeName = newName,
                    address = newAddress
                )
            )
            result.fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            placeName = updated.placeName,
                            address = updated.address,
                            isDefault = updated.isDefault,
                            // 서버가 다른 필드를 갱신했을 수 있으므로 동기화
                            favoriteId = updated.favoriteId,
                            patientId = updated.patientId,
                            isLoading = false,
                            successMessage = "장소 정보가 수정되었습니다."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        prev.copy(isLoading = false, error = error.message ?: "수정 실패")
                    }
                }
            )
        }
    }

    // 삭제하기
    fun deletePlace(onSuccess: () -> Unit = {}) {
        if (_uiState.value.isLoading) return
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.deleteFavoritePlace(
                patientId = state.patientId,
                favoriteId = state.favoriteId
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "삭제되었습니다.") }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "삭제 실패") }
                }
            )
        }
    }
}