package kr.co.ongil.data.datasource.local.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKeys {
    val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")

    val LOGIN_USER_ID_KEY = stringPreferencesKey("login_user_id")
    val USER_TYPE_KEY = stringPreferencesKey("user_type")
    val SELECTED_PATIENT_ID_KEY = stringPreferencesKey("selected_patient_id")

    // 이상 상황 관련
    val IS_ABNORMAL_DETECTED_KEY = booleanPreferencesKey("is_abnormal_detected")
    val ABNORMAL_STAGE_KEY = stringPreferencesKey("abnormal_stage") // "1", "2", "3"
    val ABNORMAL_DETECTED_TIME_KEY = stringPreferencesKey("abnormal_detected_time")
}