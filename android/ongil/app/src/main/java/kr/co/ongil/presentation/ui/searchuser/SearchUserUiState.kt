package kr.co.ongil.presentation.ui.searchuser

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kr.co.ongil.domain.model.UserSummary
@Immutable
enum class SearchUserMode {
    SEARCH_INPUT,        // 전화번호 입력
    SEARCH_SUCCESS,      // 검색 성공
    SEARCH_NOT_FOUND,    // 검색 실패
    REGISTER,            // 연락처 등록
    REGISTER_DONE        // 등록 완료 모달
}

@Stable
data class SearchUserUiState(
    val mode: SearchUserMode = SearchUserMode.SEARCH_INPUT,
    val phoneInput: String = "",
    val isSearching: Boolean = false,
    val searchErrorMessage: String? = null,
    val foundUser: UserSummary? = null,
    val relationshipName: String = "",
    val relationshipType: String = "",
    val relationshipTypeOptions: List<String> = emptyList(),
    val verificationCodeInput: String = "",
    val verificationToken: String = "",
    val verificationCountdownSec: Int = 0,
    val isVerificationValid: Boolean = false,
    val memo: String = "",
    val memoMaxLen: Int = 500,
    val isSubmitting: Boolean = false,
    val submitErrorMessage: String? = null
) {
    val isPhoneValid: Boolean
        get() = phoneInput.isNotBlank() && phoneInput.length >= 10

    val canShowSearchSuccess: Boolean
        get() = mode == SearchUserMode.SEARCH_SUCCESS && foundUser != null

    val canShowSearchNotFound: Boolean
        get() = mode == SearchUserMode.SEARCH_NOT_FOUND && !isSearching

    val canSubmitRegister: Boolean
        get() = mode == SearchUserMode.REGISTER &&
                relationshipName.isNotBlank() &&
                relationshipType.isNotBlank() &&
                isVerificationValid &&
                !isSubmitting
}

