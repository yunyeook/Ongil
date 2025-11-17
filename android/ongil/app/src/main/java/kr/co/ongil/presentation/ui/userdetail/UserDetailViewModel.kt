package kr.co.ongil.presentation.ui.userdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.ongil.domain.repository.FavoriteRepository
import kr.co.ongil.domain.repository.UserRepository
import javax.inject.Inject


@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val favoriteRepository: FavoriteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val relationshipId: Long = savedStateHandle["relationshipId"] ?: 0L
    private val initialUserType: String = savedStateHandle["userType"] ?: "GUARDIAN"

    private val _uiState = MutableStateFlow(UserDetailUiState(userType = initialUserType))
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
        loadRelationshipDetail()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            userRepository.getMyInfo()
                .collect { result ->
                    result.onSuccess { userDto ->
                        _uiState.update {
                            it.copy(userType = userDto.userType)
                        }
                        Log.d("UserDetailViewModel", "사용자 정보 로드 성공: type=${userDto.userType}")
                    }.onFailure { error ->
                        Log.e("UserDetailViewModel", "사용자 정보 로드 실패", error)
                    }
                }
        }
    }

    private fun loadRelationshipDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            favoriteRepository.getRelationshipDetail(relationshipId)
                .onSuccess { patientData ->
                    _uiState.update {
                        it.copy(
                            relationshipId = patientData.relationshipId,
                            patientId = patientData.id,
                            name = patientData.name,
                            phoneNumber = patientData.phoneNumber,
                            relationshipType = patientData.relationshipType,
                            isLoading = false
                        )
                    }
                    Log.d("UserDetailViewModel", "관계 상세 로드 성공: relationshipId=${patientData.relationshipId}, name=${patientData.name}")
                }
                .onFailure { error ->
                    Log.e("UserDetailViewModel", "관계 상세 로드 실패", error)
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    // 저장하기
    fun updateUserInfo(newName: String, newPhoneNumber: String, newRelationshipType: String) {
        _uiState.update { current ->
            current.copy(
                name = newName,
                phoneNumber = newPhoneNumber,
                relationshipType = newRelationshipType
            )
        }
    }

    // 삭제하기
    fun deleteUser(onSuccess: () -> Unit = {}) {
        _uiState.update { it.copy() }
        onSuccess()
    }
}