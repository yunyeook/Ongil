package kr.co.ongil.domain.usecase.patientinfo

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kr.co.ongil.data.model.patientinfo.PatientInfoDto
import kr.co.ongil.domain.repository.PatientInfoRepository
import javax.inject.Inject

class GetPatientInfoUseCase @Inject constructor(
    private val patientInfoRepository: PatientInfoRepository
) {
    companion object {
        private const val TAG = "GetPatientInfoUseCase"
    }

    // 환자 정보 조회
    operator fun invoke(patientId: Int): Flow<Result<PatientInfoDto>> {
        Log.d(TAG, "invoke() - 환자 정보 조회 시작: patientId=$patientId")

        return patientInfoRepository.getPatientInfo(patientId)
            .onEach { result ->
                result.onSuccess {
                    Log.d(TAG, "invoke() - 환자 정보 조회 성공: $it")
                }.onFailure {
                    Log.e(TAG, "invoke() - 환자 정보 조회 실패", it)
                }
            }
    }
}
