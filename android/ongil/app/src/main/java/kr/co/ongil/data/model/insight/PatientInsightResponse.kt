package kr.co.ongil.data.model.insight

import kotlinx.serialization.Serializable

@Serializable
data class PatientInsightResponse(
    val message: String,
    val data: PatientInsightDto
)
