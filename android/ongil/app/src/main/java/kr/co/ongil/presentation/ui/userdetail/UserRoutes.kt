package kr.co.ongil.presentation.ui.userdetail

import android.net.Uri

object UserRoutes {
    // 환자 상세 Graph의 Route (필요하다면 추가)
    const val GRAPH_ROUTE = "patient_graph"

    // 환자 상세 화면 Route (gender 인자 추가)
    const val DETAIL_ROUTE = "patient_detail/{patientId}/{name}/{phoneNumber}/{gender}"

    fun createRoute(
        patientId: Long,
        name: String,
        phoneNumber: String,
        gender: String // ⭐ gender 추가
    ): String {
        val encodedName = Uri.encode(name)
        val encodedPhone = Uri.encode(phoneNumber)
        val encodedGender = Uri.encode(gender)
        // gender 인자를 포함하여 경로 생성
        return "patient_detail/$patientId/$encodedName/$encodedPhone/$encodedGender"
    }
}