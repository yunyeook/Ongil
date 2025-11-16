package kr.co.ongil.wear.data.datasource.local

import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * DataStore에 사용할 Key 상수들
 *
 * 스프링부트의 Constants 클래스와 동일
 * - Key 이름을 한 곳에서 관리
 * - 오타 방지
 * - key는 앱 모듈의 DataStoreKeys.kt와 동일하게
 */
object DataStoreKeys {
    // 로그인 토큰
    val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

    // 사용자 정보
    val LOGIN_USER_ID_KEY = stringPreferencesKey("login_user_id")
    val USER_TYPE_KEY = stringPreferencesKey("user_type")
    val SELECTED_PATIENT_ID_KEY = stringPreferencesKey("selected_patient_id")
}