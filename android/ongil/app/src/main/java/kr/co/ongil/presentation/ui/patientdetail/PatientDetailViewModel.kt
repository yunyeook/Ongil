package kr.co.ongil.presentation.ui.patientdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialPatientId: Long = savedStateHandle["patientId"] ?: 0L
    private val initialName: String = savedStateHandle["name"] ?: ""
    private val initialPhoneNumber: String = savedStateHandle["phoneNumber"] ?: ""
    private val initialGender: String = savedStateHandle["gender"] ?: ""

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
    }

    // 삭제하기
    fun deletePatient(onSuccess: () -> Unit = {}) {
        _uiState.update { it.copy() }
        onSuccess()
    }
}