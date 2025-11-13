package kr.co.ongil.presentation.ui.searchuser

sealed interface SearchUserUiEvent {
    // 검색 단계
    data class OnPhoneInputChange(val value: String) : SearchUserUiEvent
    object OnClickSearch : SearchUserUiEvent
    object OnClickRetrySearch : SearchUserUiEvent
    object OnClickAddFriend : SearchUserUiEvent
    object OnClickBackToSearch : SearchUserUiEvent

    // 등록 단계
    data class OnRelationshipNameChange(val value: String) : SearchUserUiEvent
    data class OnRelationshipTypeSelect(val value: String) : SearchUserUiEvent
    object OnClickRequestVerification : SearchUserUiEvent
    data class OnVerificationCodeChange(val value: String) : SearchUserUiEvent
    object OnClickVerifyCode : SearchUserUiEvent
    data class OnMemoChange(val value: String) : SearchUserUiEvent

    // 제출
    object OnClickSubmitRegister : SearchUserUiEvent

    // 완료 모달
    object OnClickGoToRegisterUser : SearchUserUiEvent
    object OnClickSearchMore : SearchUserUiEvent
}