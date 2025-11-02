package kr.co.ongil.presentation.ui.patientdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.data.repository.FavoriteRepository
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailUiState


class PatientDetailViewModel(
    private val repository: FavoriteRepository,
    initialPatientId: Long,
    initialName: String,
    initialPhoneNumber: String,
    initialGender: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PatientDetailUiState(
            patientId = initialPatientId,
            name = initialName,
            phoneNumber = initialPhoneNumber,
            gender = initialGender
        )
    )
    val uiState: StateFlow<PatientDetailUiState> = _uiState.asStateFlow()

    // 저장하기
    fun updatePatientInfo(newName: String, newPhoneNumber: String, newGender: String) {
        _uiState.update { current ->
            current.copy(
                name = newName,
                phoneNumber = newPhoneNumber,
                gender = newGender
            )
        }

        viewModelScope.launch {
            repository.updatePatientDetail(
                updatedId = _uiState.value.patientId,
                newName = newName,
                newPhoneNumber = newPhoneNumber,
                newGender = newGender
            )
        }
    }

    // 삭제하기
    fun deletePatient(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.deletePatient(
                patientId = _uiState.value.patientId
            )
            if (success) {
                onSuccess()
            }
        }
    }
}