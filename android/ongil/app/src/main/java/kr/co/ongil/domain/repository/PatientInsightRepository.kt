package kr.co.ongil.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.model.insight.PatientInsightDto

interface PatientInsightRepository {
    fun getLatestWeeklyInsight(patientId: Int): Flow<Result<PatientInsightDto>>
}
