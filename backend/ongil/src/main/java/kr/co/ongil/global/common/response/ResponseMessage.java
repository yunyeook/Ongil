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
    TOKEN_REISSUE_SUCCESS("토큰이 성공적으로 재발급되었습니다."),
    PASSWORD_RESET_SUCCESS("비밀번호가 성공적으로 변경되었습니다."),

    // Phone Verification
    PHONE_VERIFICATION_SENT("전화번호 인증 요청이 성공적으로 완료되었습니다."),
    PHONE_VERIFICATION_SUCCESS("인증이 성공적으로 완료되었습니다."),

    // User
    USER_FOUND("사용자 조회 성공"),
    USER_SEARCH_SUCCESS("사용자 검색 성공"),
    USER_UPDATED("사용자 정보 수정 성공"),
    USER_DELETE_SUCCESS("회원 탈퇴가 완료되었습니다."),

    // Patient
    PATIENT_FOUND("환자 조회 성공"),
    SAFEZONE_UPDATED("안전구역이 수정되었습니다."),

    // Favorite
    FAVORITE_CREATED("즐겨찾기가 성공적으로 추가되었습니다."),
    FAVORITE_LIST_FOUND("즐겨찾기 목록 조회에 성공했습니다."),
    FAVORITE_FOUND("즐겨찾기 조회에 성공했습니다."),
    FAVORITE_UPDATED("즐겨찾기가 성공적으로 수정되었습니다."),
    FAVORITE_DELETED("즐겨찾기가 성공적으로 삭제되었습니다."),
    FAVORITE_DEFAULT_SET("기본 목적지가 설정되었습니다."),

    // Map
    ADDRESS_FOUND("주소를 성공적으로 조회하였습니다."),
    COORDINATE_FOUND("좌표를 성공적으로 조회하였습니다."),
    PLACE_SEARCH_SUCCESS("장소 검색이 완료되었습니다."),
    PLACE_SEARCH_DETAIL_SUCCESS("장소 상세 검색이 완료되었습니다."),
    FAVORITE_PLACE_SAVED("자주 가는 장소가 저장되었습니다."),
    FAVORITE_PLACE_FOUND("자주 가는 장소 조회 성공"),
    FAVORITE_PLACE_UPDATED("자주 가는 장소가 수정되었습니다."),
    FAVORITE_PLACE_DELETED("자주 가는 장소가 삭제되었습니다."),
    ROUTE_FOUND("경로를 성공적으로 조회하였습니다."),
    
    //Navigation
    NAVIGATION_START_SUCCESS("길안내가 시작되었습니다."),
    NAVIGATION_END_SUCCESS("길안내가 종료되었습니다."),


    // Notification
    NOTIFICATION_LIST_FOUND("알림 목록 조회에 성공했습니다."),
    NOTIFICATION_FOUND("알림 조회에 성공했습니다."),
    NOTIFICATION_READ("알림을 읽음 처리했습니다."),
    NOTIFICATION_READ_ALL("전체 알림을 읽음 처리했습니다."),
    NOTIFICATION_DELETED("알림이 삭제되었습니다."),
    NOTIFICATION_DELETED_ALL("전체 알림이 삭제되었습니다."),
    // Common
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_INPUT("입력값이 유효하지 않습니다. 형식을 다시 확인해주세요."), ;

    private final String message;
}
