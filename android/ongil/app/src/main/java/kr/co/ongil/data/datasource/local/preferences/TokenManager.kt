package kr.co.ongil.data.datasource.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 토큰 관리를 위한 DataStore
 */
private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "token_preferences")

/**
 * 토큰 관리 인터페이스
 */
interface TokenManager {
    /**
     * AccessToken 저장
     */
    suspend fun saveAccessToken(token: String)

    /**
     * RefreshToken 저장
     */
    suspend fun saveRefreshToken(token: String)

    /**
     * AccessToken 불러오기
     */
    fun getAccessToken(): Flow<String?>

    /**
     * RefreshToken 불러오기
     */
    fun getRefreshToken(): Flow<String?>

    /**
     * 토큰 삭제 (로그아웃 시 사용)
     */
    suspend fun clearTokens()

    /**
     * AccessToken과 RefreshToken을 동시에 저장
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    /**
     * FCM 토큰 저장
     */
    suspend fun saveFcmToken(token: String)

    /**
     * FCM 토큰 불러오기
     */
    fun getFcmToken(): Flow<String?>
}

/**
 * TokenManager 구현체
 */
@Singleton
class TokenManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenManager {

    private val dataStore = context.tokenDataStore

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
    }

    override suspend fun saveAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    override suspend fun saveRefreshToken(token: String) {
        dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = token
        }
    }

    override fun getAccessToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    override fun getRefreshToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    override suspend fun saveFcmToken(token: String) {
        dataStore.edit { preferences ->
            preferences[FCM_TOKEN_KEY] = token
        }
    }

    override fun getFcmToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[FCM_TOKEN_KEY]
        }
    }
}
