package kr.co.ongil.presentation.ui.sos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.domain.repository.FavoriteRepository
import kr.co.ongil.presentation.ui.favorite.PatientData
import javax.inject.Inject

data class SosAlertUiState(
    val defaultGuardian: PatientData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SosAlertViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SosAlertUiState())
    val uiState: StateFlow<SosAlertUiState> = _uiState.asStateFlow()

    /**
     * 기본 보호자 정보 가져오기
     */
    fun loadDefaultGuardian() {
        Log.d("SosAlertViewModel", "기본 보호자 정보 로드 시작")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            favoriteRepository.getMyRelationships()
                .onSuccess { relationships ->
                    Log.d("SosAlertViewModel", "관계 목록 조회 성공: ${relationships.size}개")

                    // isDefault = true인 보호자 찾기
                    val defaultGuardian = relationships.firstOrNull { it.isDefault }

                    if (defaultGuardian != null) {
                        Log.d("SosAlertViewModel", "✅ 기본 보호자 찾음: ${defaultGuardian.name}, ${defaultGuardian.phoneNumber}")
                        _uiState.update {
                            it.copy(
                                defaultGuardian = defaultGuardian,
                                isLoading = false,
                                error = null
                            )
                        }
                    } else {
                        Log.w("SosAlertViewModel", "⚠️ 기본 보호자를 찾을 수 없습니다")
                        // 기본 보호자가 없으면 첫 번째 보호자 사용
                        val firstGuardian = relationships.firstOrNull()
                        if (firstGuardian != null) {
                            Log.d("SosAlertViewModel", "첫 번째 보호자 사용: ${firstGuardian.name}")
                            _uiState.update {
                                it.copy(
                                    defaultGuardian = firstGuardian,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "등록된 보호자가 없습니다"
                                )
                            }
                        }
                    }
                }
                .onFailure { exception ->
                    Log.e("SosAlertViewModel", "❌ 관계 목록 조회 실패", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "보호자 정보를 불러올 수 없습니다: ${exception.message}"
                        )
                    }
                }
        }
    }
}
