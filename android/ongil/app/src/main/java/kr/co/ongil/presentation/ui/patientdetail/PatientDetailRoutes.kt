package kr.co.ongil.presentation.ui.patientdetail

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class PatientDetailRoutes(val route: String) {
    // PatientDetail 화면 경로 (patientId, name, phoneNumber를 인자로 받음)
    data object Detail : PatientDetailRoutes("patient_detail/{patientId}/{name}/{phoneNumber}") {
        fun createRoute(
            patientId: Long,
            name: String,
            phoneNumber: String
        ): String {
            val encodedName = Uri.encode(name)
            val encodedPhone = Uri.encode(phoneNumber)
            return "patient_detail/$patientId/$encodedName/$encodedPhone"
        }

        val arguments = listOf(
            navArgument("patientId") { type = NavType.LongType },
            navArgument("name") { type = NavType.StringType },
            navArgument("phoneNumber") { type = NavType.StringType }
        )
    }
}
