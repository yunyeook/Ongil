package kr.co.ongil.global.common.response;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    // Auth
    LOGIN_SUCCESS("로그인에 성공했습니다."),
    LOGOUT_SUCCESS("로그아웃에 성공했습니다."),
    SIGNUP_SUCCESS("회원가입이 완료되었습니다."),

    // User
    USER_FOUND("사용자 조회 성공"),
    USER_UPDATED("사용자 정보 수정 성공"),

    // Patient
    PATIENT_FOUND("환자 조회 성공"),
    SAFEZONE_UPDATED("안전구역이 수정되었습니다."),

    // Map
    ADDRESS_FOUND("주소를 성공적으로 조회하였습니다."),
    COORDINATE_FOUND("좌표를 성공적으로 조회하였습니다."),
    PLACE_SEARCH_SUCCESS("장소 검색이 완료되었습니다."),
    FAVORITE_PLACE_SAVED("자주 가는 장소가 저장되었습니다."),
    FAVORITE_PLACE_FOUND("자주 가는 장소 조회 성공"),
    FAVORITE_PLACE_UPDATED("자주 가는 장소가 수정되었습니다."),
    FAVORITE_PLACE_DELETED("자주 가는 장소가 삭제되었습니다."),
    ROUTE_FOUND("경로를 성공적으로 조회하였습니다."),

    // Common
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
    INVALID_INPUT("입력값이 유효하지 않습니다.");

    private final String message;
}
