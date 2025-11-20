package kr.co.ongil.wear.domain.model

/**
 * 환자 정보 Domain Model
 * (보호자용 - 여러 환자 중 선택)
 */
data class PatientInfo(
    val patientId: Long,
    val name: String,
    val age: Int? = null,
    val relationship: String? = null,
    val phoneNumber: String? = null,
    val isSelected: Boolean = false
)
