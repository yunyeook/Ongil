package kr.co.ongil.presentation.ui.userdetail

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class UserDetailRoutes(val route: String) {
    // PatientDetail 화면 경로 (patientId, name, phoneNumber, relationshipType을 인자로 받음)
    data object Detail : UserDetailRoutes("patient_detail/{patientId}/{name}/{phoneNumber}/{relationshipType}") {
        fun createRoute(
            patientId: Long,
            name: String,
            phoneNumber: String,
            relationshipType: String
        ): String {
            val encodedName = Uri.encode(name)
            val encodedPhone = Uri.encode(phoneNumber)
            val encodedRelationshipType = Uri.encode(relationshipType)
            return "patient_detail/$patientId/$encodedName/$encodedPhone/$encodedRelationshipType"
        }

        val arguments = listOf(
            navArgument("patientId") { type = NavType.LongType },
            navArgument("name") { type = NavType.StringType },
            navArgument("phoneNumber") { type = NavType.StringType },
            navArgument("relationshipType") { type = NavType.StringType }
        )
    }
}
