package kr.co.ongil.wear.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.wear.data.datasource.local.WearDataStoreManager
import kr.co.ongil.wear.domain.model.PatientInfo
import javax.inject.Inject

/**
 * 환자 선택 ViewModel (보호자용)
 *
 * 주요 기능:
 * 1. 환자 목록 관리
 * 2. 환자 선택/변경
 * 3. 선택한 환자 ID DataStore 저장
 *
 * TODO: Phone 앱과 환자 목록 동기화 구현 필요
 */
@HiltViewModel
class PatientViewModel @Inject constructor(
    private val dataStoreManager: WearDataStoreManager
) : ViewModel() {

    companion object {
        private const val TAG = "PatientViewModel"
    }

    // === UI 상태 ===

    data class UiState(
        val patients: List<PatientInfo> = emptyList(),
        val selectedPatientId: Long? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadPatients()
        loadSelectedPatient()
    }

    /**
     * 환자 목록 로드
     *
     * TODO: 실제로는 Phone 앱에서 DataLayer로 받아와야 함
     * 현재는 임시로 빈 목록 사용
     */
    private fun loadPatients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // TODO: Phone 앱에서 환자 목록 가져오기
                // 현재는 빈 목록
                val patients = emptyList<PatientInfo>()

                _uiState.update {
                    it.copy(
                        patients = patients,
                        isLoading = false,
                        errorMessage = if (patients.isEmpty()) "등록된 환자가 없습니다" else null
                    )
                }

                Log.d(TAG, "환자 목록 로드 완료: ${patients.size}명")

            } catch (e: Exception) {
                Log.e(TAG, "환자 목록 로드 실패", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "환자 목록을 불러올 수 없습니다"
                    )
                }
            }
        }
    }

    /**
     * 선택된 환자 ID 로드
     */
    private fun loadSelectedPatient() {
        viewModelScope.launch {
            val selectedPatientId = dataStoreManager.getSelectedPatientId().first()
            _uiState.update {
                it.copy(selectedPatientId = selectedPatientId?.toLongOrNull())
            }
            Log.d(TAG, "선택된 환자 ID: $selectedPatientId")
        }
    }

    /**
     * 환자 선택
     *
     * @param patientId 선택할 환자 ID
     */
    fun selectPatient(patientId: Long) {
        viewModelScope.launch {
            try {
                // DataStore에 저장
                dataStoreManager.saveSelectedPatientId(patientId.toString())

                // UI 상태 업데이트
                _uiState.update {
                    it.copy(selectedPatientId = patientId)
                }

                Log.d(TAG, "환자 선택: $patientId")

                // TODO: Phone 앱으로 선택 정보 전송 (DataLayer)

            } catch (e: Exception) {
                Log.e(TAG, "환자 선택 실패", e)
                _uiState.update {
                    it.copy(errorMessage = "환자 선택에 실패했습니다")
                }
            }
        }
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
