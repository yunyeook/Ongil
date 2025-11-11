package kr.co.ongil.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.model.patientinfo.PatientInfoDto

interface PatientInfoRepository {
    // 환자 정보 조회
    fun getPatientInfo(patientId: Int): Flow<Result<PatientInfoDto>>
}
