package kr.co.ongil.data.datasource.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


/**
 * UserDataStoreManager 구현체
 */
@Singleton
class UserDataStoreManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserDataStoreManager {

    override suspend fun saveAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.ACCESS_TOKEN_KEY] = token
        }
    }

    override suspend fun saveRefreshToken(token: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.REFRESH_TOKEN_KEY] = token
        }
    }

    override fun getAccessToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.ACCESS_TOKEN_KEY]
        }
    }

    override fun getRefreshToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.REFRESH_TOKEN_KEY]
        }
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(DataStoreKeys.ACCESS_TOKEN_KEY)
            preferences.remove(DataStoreKeys.REFRESH_TOKEN_KEY)
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.ACCESS_TOKEN_KEY] = accessToken
            preferences[DataStoreKeys.REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    override suspend fun saveFcmToken(token: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.FCM_TOKEN_KEY] = token
        }
    }

    override fun getFcmToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.FCM_TOKEN_KEY]
        }
    }

    override fun getLoginUserId(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.LOGIN_USER_ID_KEY]
        }
    }

    override suspend fun saveLoginUserId(loginUserId: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.LOGIN_USER_ID_KEY] = loginUserId
        }
    }

    override suspend fun clearLoginUserId() {
        dataStore.edit { preferences ->
            preferences.remove(DataStoreKeys.LOGIN_USER_ID_KEY)
        }
    }

    override fun getUserType(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.USER_TYPE_KEY]
        }
    }

    override suspend fun saveUserType(userType: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.USER_TYPE_KEY] = userType
        }
    }

    override fun getSelectedPatientId(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.SELECTED_PATIENT_ID_KEY]
        }
    }

    override suspend fun saveSelectedPatientId(selectedPatientId: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.SELECTED_PATIENT_ID_KEY] = selectedPatientId
        }
    }

    override suspend fun saveAbnormalDetection(isDetected: Boolean, stage: String, detectedTime: String) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.IS_ABNORMAL_DETECTED_KEY] = isDetected
            preferences[DataStoreKeys.ABNORMAL_STAGE_KEY] = stage
            preferences[DataStoreKeys.ABNORMAL_DETECTED_TIME_KEY] = detectedTime
        }
    }

    override fun getIsAbnormalDetected(): Flow<Boolean?> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.IS_ABNORMAL_DETECTED_KEY]
        }
    }

    override fun getAbnormalStage(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.ABNORMAL_STAGE_KEY]
        }
    }

    override fun getAbnormalDetectedTime(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.ABNORMAL_DETECTED_TIME_KEY]
        }
    }

    override suspend fun clearAbnormalDetection() {
        dataStore.edit { preferences ->
            preferences.remove(DataStoreKeys.IS_ABNORMAL_DETECTED_KEY)
            preferences.remove(DataStoreKeys.ABNORMAL_STAGE_KEY)
            preferences.remove(DataStoreKeys.ABNORMAL_DETECTED_TIME_KEY)
        }
    }

    override suspend fun saveSafeZoneSettings(
        patientId: Long,
        level1Distance: Int,
        level1Dwell: Int,
        level2Distance: Int,
        level2Dwell: Int,
        level3Distance: Int,
        level3Dwell: Int,
        pushEnabled: Boolean,
        autoCallEnabled: Boolean
    ) {
        dataStore.edit { preferences ->
            // 환자별로 키를 구분하여 저장
            preferences[intPreferencesKey("safe_zone_level1_distance_$patientId")] = level1Distance
            preferences[intPreferencesKey("safe_zone_level1_dwell_$patientId")] = level1Dwell
            preferences[intPreferencesKey("safe_zone_level2_distance_$patientId")] = level2Distance
            preferences[intPreferencesKey("safe_zone_level2_dwell_$patientId")] = level2Dwell
            preferences[intPreferencesKey("safe_zone_level3_distance_$patientId")] = level3Distance
            preferences[intPreferencesKey("safe_zone_level3_dwell_$patientId")] = level3Dwell
            preferences[booleanPreferencesKey("safe_zone_push_enabled_$patientId")] = pushEnabled
            preferences[booleanPreferencesKey("safe_zone_auto_call_enabled_$patientId")] = autoCallEnabled
        }
    }

    override suspend fun getSafeZoneSettings(patientId: Long): kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings {
        return dataStore.data.map { preferences ->
            kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings(
                level1Distance = preferences[intPreferencesKey("safe_zone_level1_distance_$patientId")] ?: 100,
                level1Dwell = preferences[intPreferencesKey("safe_zone_level1_dwell_$patientId")] ?: 60,
                level2Distance = preferences[intPreferencesKey("safe_zone_level2_distance_$patientId")] ?: 350,
                level2Dwell = preferences[intPreferencesKey("safe_zone_level2_dwell_$patientId")] ?: 30,
                level3Distance = preferences[intPreferencesKey("safe_zone_level3_distance_$patientId")] ?: 700,
                level3Dwell = preferences[intPreferencesKey("safe_zone_level3_dwell_$patientId")] ?: 15,
                pushEnabled = preferences[booleanPreferencesKey("safe_zone_push_enabled_$patientId")] ?: true,
                autoCallEnabled = preferences[booleanPreferencesKey("safe_zone_auto_call_enabled_$patientId")] ?: false
            )
        }.first() // Flow를 값으로 변환
    }

    override fun observeSafeZoneSettings(patientId: Long): Flow<kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings> {
        return dataStore.data.map { preferences ->
            kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings(
                level1Distance = preferences[intPreferencesKey("safe_zone_level1_distance_$patientId")] ?: 100,
                level1Dwell = preferences[intPreferencesKey("safe_zone_level1_dwell_$patientId")] ?: 60,
                level2Distance = preferences[intPreferencesKey("safe_zone_level2_distance_$patientId")] ?: 350,
                level2Dwell = preferences[intPreferencesKey("safe_zone_level2_dwell_$patientId")] ?: 30,
                level3Distance = preferences[intPreferencesKey("safe_zone_level3_distance_$patientId")] ?: 700,
                level3Dwell = preferences[intPreferencesKey("safe_zone_level3_dwell_$patientId")] ?: 15,
                pushEnabled = preferences[booleanPreferencesKey("safe_zone_push_enabled_$patientId")] ?: true,
                autoCallEnabled = preferences[booleanPreferencesKey("safe_zone_auto_call_enabled_$patientId")] ?: false
            )
        }
    }

    override suspend fun saveProfileImage(userId: String, profileImageUrl: String?) {
        dataStore.edit { preferences ->
            if (profileImageUrl != null) {
                preferences[stringPreferencesKey("profile_image_$userId")] = profileImageUrl
            } else {
                preferences.remove(stringPreferencesKey("profile_image_$userId"))
            }
        }
    }

    override fun getProfileImage(userId: String): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey("profile_image_$userId")]
        }
    }
}
