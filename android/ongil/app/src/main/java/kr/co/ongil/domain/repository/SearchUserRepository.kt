package kr.co.ongil.domain.repository

import kr.co.ongil.domain.model.UserSummary


interface SearchUserRepository {

    // POST /api/v1/users/searches
    suspend fun searchUserByPhone(
        phoneNumber: String
    ): UserSummary?

   // POST /api/v1/phone-verifications
    suspend fun requestVerificationCode(
        phoneNumber: String
    ): Boolean

//    /api/v1/phone-verifications/verify
//     *
//     * @return Pair(
//     *    first  = verified 여부,
//     *    second = verificationToken (verified == true일 때만 유효)

    suspend fun verifyCode(
        phoneNumber: String,
        verificationCode: String,
        grants: String = "RELATIONSHIP_BIND"
    ): Pair<Boolean, String?>


//     * 백엔드: POST /api/v1/relationships/me
//     *
//     * @param targetUserPhone  -- 등록 대상 유저 번호
//     * @param relationshipName -- relationshipName
//     * @param relationshipType -- relationshipType
//     * @param memo             --  추가 메모
//     * @param verificationToken  --  verifyCode() 성공 시 받은 토큰

    suspend fun registerContact(
        targetUserId: String,
        relationshipName: String,
        relationshipType: String,
        memo: String,
        verificationToken: String
    ): Boolean
}