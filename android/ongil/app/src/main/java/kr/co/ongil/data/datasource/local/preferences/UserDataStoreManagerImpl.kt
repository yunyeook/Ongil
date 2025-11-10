package kr.co.ongil.data.datasource.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
}
