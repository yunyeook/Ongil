package kr.co.ongil.presentation.ui.placedetail

import android.util.Log
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
        Log.d("PlaceDetailVM", "초기화: patientId=$initialPatientId, favoriteId=$initialFavoriteId")
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val current = _uiState.value
            Log.d("PlaceDetailVM", "API 호출: patientId=${current.patientId}, favoriteId=${current.favoriteId}")
            val result = repository.getPlaceDetail(
                patientId = current.patientId,
                favoriteId = current.favoriteId
            )
            result.fold(
                onSuccess = { detail ->
                    Log.d("PlaceDetailVM", "API 성공: placeName=${detail.placeName}, placeAlias=${detail.placeAlias}, isDefault=${detail.isDefault}")
                    _uiState.update {
                        it.copy(
                            // placeAlias가 있으면 placeAlias를, 없으면 placeName을 표시
                            placeName = if (detail.placeAlias.isNullOrBlank()) detail.placeName else detail.placeAlias,
                            address = detail.address,
                            isDefault = detail.isDefault,
                            initialIsDefault = detail.isDefault, // API에서 받은 초기값 저장
                            favoriteId = detail.favoriteId,
                            patientId = detail.patientId,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    Log.e("PlaceDetailVM", "API 실패", error)
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "상세 조회 실패") }
                }
            )
        }
    }

    // 기본목적지 설정
    fun setAsDefault() {
        if (_uiState.value.isLoading) return
        val currentState = _uiState.value
        val newIsDefault = !currentState.isDefault

        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updatePlaceDetail(
                patientId = currentState.patientId,
                favoriteId = currentState.favoriteId,
                update = PlaceDetailUpdate(
                    isDefault = if (newIsDefault) true else null // true일 때만 보내고, false일 때는 null
                )
            )
            result.fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            placeName = if (updated.placeAlias.isNullOrBlank()) updated.placeName else updated.placeAlias,
                            address = updated.address,
                            isDefault = updated.isDefault,
                            isLoading = false,
                            successMessage = if (newIsDefault) "기본 목적지로 설정되었습니다." else "기본 목적지 설정이 해제되었습니다."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "기본 목적지 설정 실패"
                        )
                    }
                }
            )
        }
    }

    // 저장하기 (장소명은 placeAlias로 저장)
    fun updatePlaceInfo(newName: String, newAddress: String, newIsDefault: Boolean?, onSuccess: () -> Unit = {}) {
        if (_uiState.value.isLoading) return
        val prev = _uiState.value
        Log.d("PlaceDetailVM", "저장: patientId=${prev.patientId}, favoriteId=${prev.favoriteId}, placeAlias=$newName, isDefault=$newIsDefault")
        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.updatePlaceDetail(
                patientId = prev.patientId,
                favoriteId = prev.favoriteId,
                update = PlaceDetailUpdate(
                    placeAlias = newName, // 사용자가 입력한 이름은 placeAlias로 저장
                    address = newAddress,
                    isDefault = newIsDefault // 변경되었을 때만 true, 아니면 null
                )
            )
            result.fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            // placeAlias가 있으면 placeAlias를, 없으면 placeName을 표시
                            placeName = if (updated.placeAlias.isNullOrBlank()) updated.placeName else updated.placeAlias,
                            address = updated.address,
                            isDefault = updated.isDefault,
                            // 서버가 다른 필드를 갱신했을 수 있으므로 동기화
                            favoriteId = updated.favoriteId,
                            patientId = updated.patientId,
                            isLoading = false,
                            successMessage = "장소 정보가 수정되었습니다."
                        )
                    }
                    // 네비게이션은 메인 스레드에서 실행
                    viewModelScope.launch(Dispatchers.Main) {
                        onSuccess()
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
                    // 네비게이션은 메인 스레드에서 실행
                    viewModelScope.launch(Dispatchers.Main) {
                        onSuccess()
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "삭제 실패") }
                }
            )
        }
    }
}