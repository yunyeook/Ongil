package kr.co.ongil.data.repository

import android.util.Log
import javax.inject.Inject
import kr.co.ongil.data.datasource.remote.api.AuthApi
import kr.co.ongil.data.datasource.remote.api.UserApi
import kr.co.ongil.data.model.auth.SendVerificationRequest
import kr.co.ongil.data.model.auth.VerifyCodeRequest
import kr.co.ongil.data.model.user.CreateRelationshipRequest
import kr.co.ongil.data.model.user.UserSearchRequest
import kr.co.ongil.data.model.user.toDomain
import kr.co.ongil.domain.model.UserSummary
import kr.co.ongil.domain.repository.SearchUserRepository

/**
 * SearchUserRepository 실제 구현체
 */
class SearchUserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val authApi: AuthApi
) : SearchUserRepository {

    override suspend fun searchUserByPhone(phoneNumber: String): UserSummary? {
        return try {
            val request = UserSearchRequest(phoneNumber = phoneNumber)
            val response = userApi.searchUser(request)

            when (response.code()) {
                200 -> {
                    // 사용자 찾음
                    val body = response.body()
                    if (body == null) {
                        Log.e("SearchUserRepo", "사용자 검색 응답 body가 null")
                        return null
                    }
                    Log.d("SearchUserRepo", "사용자 검색 성공 - userId=${body.data?.user?.id}, name=${body.data?.user?.name}")
                    body.toDomain()
                }
                204 -> {
                    // 검색 결과 없음
                    Log.d("SearchUserRepo", "검색 결과 없음 - phoneNumber=$phoneNumber")
                    null
                }
                403 -> {
                    // 같은 userType (검색 불가)
                    Log.e("SearchUserRepo", "검색할 수 없는 대상입니다 (같은 userType)")
                    null
                }
                else -> {
                    Log.e("SearchUserRepo", "사용자 검색 실패 - HTTP ${response.code()}: ${response.message()}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("SearchUserRepo", "사용자 검색 예외 발생", e)
            null
        }
    }

    override suspend fun requestVerificationCode(phoneNumber: String): Boolean {
        return try {
            val request = SendVerificationRequest(phoneNumber = phoneNumber)
            val response = authApi.sendVerificationCode(request)

            Log.d("SearchUserRepo", "인증번호 발송 성공 - phoneNumber=$phoneNumber")
            true
        } catch (e: Exception) {
            Log.e("SearchUserRepo", "인증번호 발송 실패", e)
            false
        }
    }

    override suspend fun verifyCode(
        phoneNumber: String,
        verificationCode: String,
        grants: String
    ): Pair<Boolean, String?> {
        return try {
            val request = VerifyCodeRequest(
                phoneNumber = phoneNumber,
                verificationCode = verificationCode,
                grants = grants
            )
            Log.d("SearchUserRepo", "인증번호 검증 요청 - phoneNumber=$phoneNumber, code=$verificationCode, grants=$grants")

            val response = authApi.verifyCode(request)

            val verified = response.data.verified
            val token = response.data.verificationToken

            Log.d("SearchUserRepo", "인증번호 검증 성공 - verified=$verified, token=${token?.take(50)}...")
            verified to token
        } catch (e: Exception) {
            Log.e("SearchUserRepo", "인증번호 검증 실패", e)
            false to null
        }
    }

    override suspend fun registerContact(
        verificationToken: String,
        relationshipName: String,
        relationshipType: String
    ): Boolean {
        return try {
            val request = CreateRelationshipRequest(
                verificationToken = verificationToken,
                relationshipName = relationshipName,
                relationshipType = relationshipType
            )
            Log.d("SearchUserRepo", "관계 생성 요청 - relationshipName=$relationshipName, relationshipType=$relationshipType")

            val response = userApi.createRelationship(request)

            if (!response.isSuccessful) {
                Log.e("SearchUserRepo", "관계 생성 실패 - HTTP ${response.code()}: ${response.message()}")
                return false
            }

            val body = response.body()
            if (body == null) {
                Log.e("SearchUserRepo", "관계 생성 응답 body가 null")
                return false
            }

            Log.d("SearchUserRepo", "관계 생성 성공 - relationshipId=${body.data.relationshipId}, name=${body.data.relationshipName}")
            true
        } catch (e: Exception) {
            Log.e("SearchUserRepo", "관계 생성 예외 발생", e)
            false
        }
    }
}
