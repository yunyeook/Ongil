package kr.co.ongil.data.model.insight

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatientInsightDto(
    val id: Int,
    @SerialName("patient_id") val patientId: Int,
    @SerialName("period_type") val periodType: String,
    @SerialName("period_start_date") val periodStartDate: String,
    @SerialName("period_end_date") val periodEndDate: String,
    @SerialName("overall_risk_level") val overallRiskLevel: String,
    val summary: String,
    @SerialName("positive_signals") val positiveSignals: List<String>,
    @SerialName("warning_signals") val warningSignals: List<String>,
    @SerialName("possible_interpretations") val possibleInterpretations: List<String>,
    @SerialName("caregiver_suggestions") val caregiverSuggestions: List<String>,
    @SerialName("data_notes") val dataNotes: List<String>
)
