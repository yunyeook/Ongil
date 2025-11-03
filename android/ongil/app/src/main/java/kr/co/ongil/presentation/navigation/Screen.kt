package kr.co.ongil.presentation.navigation

// 앱 내 라우트(경로)를 한 곳에서 관리
// React의 router/routes/어쩌구.tsx 같은거인듯..?
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    // 즐겨찾기 메인 화면
    data object Favorite : Screen("favorite")

    data object MyInfo : Screen("my_info")
    data object MyInfoEdit : Screen(route = "my_info_edit")
    data object RecentCalls : Screen(route = "recent_calls")
    data object ChangePassword : Screen(route = "change_password")


    // 사용자등록
    // 사용자 찾기 / 등록
    data object SearchUser : Screen("search_user")
    data object RegisterUser : Screen("register_user")

    // 환자 상세 화면
    data object PatientDetail : Screen("patient_detail/{patientId}/{name}/{phoneNumber}/{gender}") {
        fun createRoute(
            patientId: Long,
            name: String,
            phoneNumber: String,
            gender: String,
        ): String {
            return "patient_detail/$patientId/$name/$phoneNumber/$gender"
        }
    }

        // 장소 상세 화면
        // 인코딩해서 갈겨야됨...
        data object PlaceDetail : Screen("place_detail/{favoriteId}/{placeName}/{address}") {
            fun createRoute(
                favoriteId: Long,
                placeName: String,
                address: String,
            ): String {
                return "place_detail/$favoriteId/$placeName/$address"
            }
        }



    }
