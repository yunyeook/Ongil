package kr.co.ongil.data.datasource.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
            preferences[DataStoreKeys.SAFE_ZONE_LEVEL1_DISTANCE] = level1Distance
            preferences[DataStoreKeys.SAFE_ZONE_LEVEL1_DWELL] = level1Dwell
            preferences[DataStoreKeys.SAFE_ZONE_LEVEL2_DISTANCE] = level2Distance
            preferences[DataStoreKeys.SAFE_ZONE_LEVEL2_DWELL] = level2Dwell
            preferences[DataStoreKeys.SAFE_ZONE_LEVEL3_DISTANCE] = level3Distance
            preferences[DataStoreKeys.SAFE_ZONE_LEVEL3_DWELL] = level3Dwell
            preferences[DataStoreKeys.SAFE_ZONE_PUSH_ENABLED] = pushEnabled
            preferences[DataStoreKeys.SAFE_ZONE_AUTO_CALL_ENABLED] = autoCallEnabled
        }
    }

    override suspend fun getSafeZoneSettings(): kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings {
        return dataStore.data.map { preferences ->
            kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings(
                level1Distance = preferences[DataStoreKeys.SAFE_ZONE_LEVEL1_DISTANCE] ?: 100,
                level1Dwell = preferences[DataStoreKeys.SAFE_ZONE_LEVEL1_DWELL] ?: 60,
                level2Distance = preferences[DataStoreKeys.SAFE_ZONE_LEVEL2_DISTANCE] ?: 350,
                level2Dwell = preferences[DataStoreKeys.SAFE_ZONE_LEVEL2_DWELL] ?: 30,
                level3Distance = preferences[DataStoreKeys.SAFE_ZONE_LEVEL3_DISTANCE] ?: 700,
                level3Dwell = preferences[DataStoreKeys.SAFE_ZONE_LEVEL3_DWELL] ?: 15,
                pushEnabled = preferences[DataStoreKeys.SAFE_ZONE_PUSH_ENABLED] ?: true,
                autoCallEnabled = preferences[DataStoreKeys.SAFE_ZONE_AUTO_CALL_ENABLED] ?: false
            )
        }.first() // Flow를 값으로 변환
    }

    override fun observeSafeZoneSettings(): Flow<kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings> {
        return dataStore.data.map { preferences ->
            kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings(
                level1Distance = preferences[DataStoreKeys.SAFE_ZONE_LEVEL1_DISTANCE] ?: 100,
                level1Dwell = preferences[DataStoreKeys.SAFE_ZONE_LEVEL1_DWELL] ?: 60,
                level2Distance = preferences[DataStoreKeys.SAFE_ZONE_LEVEL2_DISTANCE] ?: 350,
                level2Dwell = preferences[DataStoreKeys.SAFE_ZONE_LEVEL2_DWELL] ?: 30,
                level3Distance = preferences[DataStoreKeys.SAFE_ZONE_LEVEL3_DISTANCE] ?: 700,
                level3Dwell = preferences[DataStoreKeys.SAFE_ZONE_LEVEL3_DWELL] ?: 15,
                pushEnabled = preferences[DataStoreKeys.SAFE_ZONE_PUSH_ENABLED] ?: true,
                autoCallEnabled = preferences[DataStoreKeys.SAFE_ZONE_AUTO_CALL_ENABLED] ?: false
            )
        }
    }
}
