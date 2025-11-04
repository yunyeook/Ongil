package kr.co.ongil.presentation.ui.placedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kr.co.ongil.data.repository.fake.FakeFavoriteRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow


class PlaceDetailViewModel(
    private val repository: FakeFavoriteRepository,
    initialFavoriteId: Long,
    initialPlaceName: String,
    initialAddress: String,
    initialIsDefault: Boolean,
    initialPatientId: Long
) : ViewModel() {

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

    // 기본목적지 설정
    fun setAsDefault() {
        _uiState.update { current ->
            current.copy(
                isDefault = !current.isDefault
            )
        }
        // 나중에 백 되면 레포지토리에 직접 응답, 현재는 더미 사용
        viewModelScope.launch {
            repository.setDefaultPlace(
                patientId = _uiState.value.patientId,
                favoriteId = _uiState.value.favoriteId
            )
        }
    }

    // 저장하기
    fun updatePlaceInfo(newName: String, newAddress: String) {
        _uiState.update { current ->
            current.copy(
                placeName = newName,
                address = newAddress
            )
        }

        viewModelScope.launch {
            repository.updatePlaceInfo(
                patientId = _uiState.value.patientId,
                favoriteId = _uiState.value.favoriteId,
                newName = newName,
                newAddress = newAddress
            )
        }
    }

    // 삭제하기
    fun deletePlace(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.deletePlace(
                patientId = _uiState.value.patientId,
                favoriteId = _uiState.value.favoriteId
            )
            if (success) {
                onSuccess()
            }
        }
    }
}