package kr.co.ongil.wear.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kr.co.ongil.wear.data.model.WearLoginData
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore 인스턴스 생성
 *
 * Context.dataStore = 확장 프로퍼티
 * 스프링의 @Bean 정의와 비슷
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wear_settings")

/**
 * WearDataStoreManager 구현체
 *
 * 스프링부트의 @Repository 구현체와 동일
 * - 실제 DataStore 접근 로직
 * - 파일 읽고 쓰기
 */
@Singleton  // 싱글톤 (스프링의 @Scope("singleton")과 동일)
class WearDataStoreManagerImpl @Inject constructor(
    private val context: Context
) : WearDataStoreManager {

    /**
     * 로그인 정보 전체 저장
     */
    override suspend fun saveLoginData(loginData: WearLoginData) {
        context.dataStore.edit { preferences ->
            preferences[DataStoreKeys.ACCESS_TOKEN_KEY] = loginData.accessToken
            preferences[DataStoreKeys.REFRESH_TOKEN_KEY] = loginData.refreshToken
            preferences[DataStoreKeys.LOGIN_USER_ID_KEY] = loginData.userId
            preferences[DataStoreKeys.USER_TYPE_KEY] = loginData.userType

            // selectedPatientId는 nullable이므로 null 체크
            loginData.selectedPatientId?.let {
                preferences[DataStoreKeys.SELECTED_PATIENT_ID_KEY] = it
            }
        }
    }

    /**
     * 액세스 토큰만 저장 (Token Refresh 시 사용)
     */
    override suspend fun saveAccessToken(accessToken: String) {
        context.dataStore.edit { preferences ->
            preferences[DataStoreKeys.ACCESS_TOKEN_KEY] = accessToken
        }
    }

    /**
     * 리프레시 토큰만 저장 (Token Refresh 시 사용)
     */
    override suspend fun saveRefreshToken(refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[DataStoreKeys.REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    /**
     * 액세스 토큰 가져오기
     */
    override fun getAccessToken(): Flow<String?> {
        return context.dataStore.data
            .catch { exception ->
                // 파일 읽기 에러 처리
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[DataStoreKeys.ACCESS_TOKEN_KEY]
            }
    }

    /**
     * 리프레시 토큰 가져오기
     */
    override fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[DataStoreKeys.REFRESH_TOKEN_KEY]
            }
    }

    /**
     * 사용자 ID 가져오기
     */
    override fun getUserId(): Flow<String?> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[DataStoreKeys.LOGIN_USER_ID_KEY]
            }
    }

    /**
     * 사용자 타입 가져오기
     */
    override fun getUserType(): Flow<String?> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[DataStoreKeys.USER_TYPE_KEY]
            }
    }

    /**
     * 선택된 환자 ID 가져오기
     */
    override fun getSelectedPatientId(): Flow<String?> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[DataStoreKeys.SELECTED_PATIENT_ID_KEY]
            }
    }

    /**
     * 로그인 정보 전체 삭제 (로그아웃)
     */
    override suspend fun clearLoginData() {
        context.dataStore.edit { preferences ->
            preferences.remove(DataStoreKeys.ACCESS_TOKEN_KEY)
            preferences.remove(DataStoreKeys.REFRESH_TOKEN_KEY)
            preferences.remove(DataStoreKeys.LOGIN_USER_ID_KEY)
            preferences.remove(DataStoreKeys.USER_TYPE_KEY)
            preferences.remove(DataStoreKeys.SELECTED_PATIENT_ID_KEY)
        }
    }

    /**
     * 로그인 여부 확인
     */
    override fun isLoggedIn(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                // accessToken이 있으면 로그인 상태
                preferences[DataStoreKeys.ACCESS_TOKEN_KEY] != null
            }
    }
}
