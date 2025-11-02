package kr.co.ongil.global.common.response;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    // Auth
    LOGIN_SUCCESS("로그인에 성공했습니다."),
    LOGOUT_SUCCESS("로그아웃에 성공했습니다."),
    SIGNUP_SUCCESS("회원가입이 성공적으로 완료되었습니다!"),
    DUPLICATE_USER("이미 가입된 회원입니다."),

    // User
    USER_FOUND("사용자 조회 성공"),
    USER_UPDATED("사용자 정보 수정 성공"),

    // Patient
    PATIENT_FOUND("환자 조회 성공"),
    SAFEZONE_UPDATED("안전구역이 수정되었습니다."),

    // Common
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_INPUT("입력값이 유효하지 않습니다. 형식을 다시 확인해주세요.");

    private final String message;
}
