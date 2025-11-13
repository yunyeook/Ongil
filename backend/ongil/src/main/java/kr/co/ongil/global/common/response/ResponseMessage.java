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
    PASSWORD_RESET_WITH_TOKEN_SUCCESS("비밀번호 재설정이 성공적으로 완료되었습니다."),

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

    // SafeZone
    SAFEZONE_CREATED("안전범위가 성공적으로 설정되었습니다."),
    SAFEZONE_UPDATED("안전범위가 성공적으로 갱신되었습니다."),
    SAFEZONE_PARTIALLY_UPDATED("안전범위가 성공적으로 수정되었습니다."),
    SAFEZONE_RESET("안전범위가 기본값으로 복원되었습니다."),
    SAFEZONE_FOUND("환자의 안전 범위와 시간이 성공적으로 조회되었습니다."),

    // Abnormal
    ABNORMAL_LIST_FOUND("이상탐지 기록이 조회되었습니다."),
    ABNORMAL_DETAIL_FOUND("이상탐지 상세 정보가 조회되었습니다."),
    ABNORMAL_CREATED("이상탐지 이벤트가 등록되었습니다."),


    // Relationship
    RELATIONSHIP_CREATED("관계가 성공적으로 생성되었습니다."),
    RELATIONSHIP_LIST_FOUND("관계 목록 조회가 성공적으로 완료되었습니다."),
    RELATIONSHIP_FOUND("관계 상세 조회가 성공적으로 완료되었습니다."),
    RELATIONSHIP_UPDATED("관계 정보가 성공적으로 수정되었습니다."),
    RELATIONSHIP_DELETED("관계가 성공적으로 삭제되었습니다."),
    RELATIONSHIP_REORDERED("관계 정렬 순서가 성공적으로 변경되었습니다."),
    RELATIONSHIP_DEFAULT_SET("대표 관계가 성공적으로 설정되었습니다."),

    // Favorite
    FAVORITE_CREATED("즐겨찾기가 성공적으로 추가되었습니다."),
    FAVORITE_LIST_FOUND("즐겨찾기 목록 조회에 성공했습니다."),
    FAVORITE_FOUND("즐겨찾기 조회에 성공했습니다."),
    FAVORITE_UPDATED("즐겨찾기가 성공적으로 수정되었습니다."),
    FAVORITE_DELETED("즐겨찾기가 성공적으로 삭제되었습니다."),
    FAVORITE_DEFAULT_SET("기본 목적지가 설정되었습니다."),
    FAVORITE_REORDERED("즐겨찾기 정렬 순서가 성공적으로 변경되었습니다."),

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
    
    // Navigation
    NAVIGATION_START_SUCCESS("길안내가 시작되었습니다."),
    NAVIGATION_END_SUCCESS("길안내가 종료되었습니다."),

    // Call
    CALL_REQUESTED("통화 요청이 전송되었습니다."),
    CALL_ACCEPTED("통화가 연결되었습니다."),
    CALL_ENDED("통화가 정상적으로 종료되었습니다."),

    // Call Log
    CALL_LOG_CREATED("통화 로그가 성공적으로 저장되었습니다."),
    CALL_LOG_FOUND("통화 로그를 성공적으로 조회하였습니다."),
    CALL_LOG_LIST_FOUND("통화 로그 목록을 성공적으로 조회하였습니다."),
    CALL_LOG_DELETED("통화 기록을 성공적으로 삭제하였습니다."),

    // Call Recording
    CALL_RECORDING_CREATED("통화 녹음 정보가 성공적으로 등록되었습니다."),
    CALL_RECORDING_FOUND("통화 녹음 정보를 성공적으로 조회하였습니다."),
    CALL_RECORDING_DELETED("통화 녹음 정보가 성공적으로 삭제되었습니다."),

    // Notification
    NOTIFICATION_LIST_FOUND("알림 목록 조회에 성공했습니다."),
    NOTIFICATION_FOUND("알림 조회에 성공했습니다."),
    NOTIFICATION_READ("알림을 읽음 처리했습니다."),
    NOTIFICATION_READ_ALL("전체 알림을 읽음 처리했습니다."),
    NOTIFICATION_DELETED("알림이 삭제되었습니다."),
    NOTIFICATION_DELETED_ALL("전체 알림이 삭제되었습니다."),
    NOTIFICATION_CREATED("알림이 생성되었습니다."),

    //FCM
    FCM_TOKEN_REGISTED("FCM 토큰이 저장되었습니다."),
    FCM_TOKEN_DELETED("FCM 토큰이 삭제되었습니다."),

    //SOS
    SOS_REQUEST_CREATED("도움 요청이 접수되었습니다."),
    SOS_ACK_PROCESSED("워치 재생 완료 콜백이 정상적으로 처리되었습니다."),
    SOS_STOPPED("도움 요청 음성 재생이 성공적으로 종료되었습니다."),

    // Location
    LOCATION_UPDATE("위치 정보가 정상적으로 업데이트되었습니다."),
    // Common
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_INPUT("입력값이 유효하지 않습니다. 형식을 다시 확인해주세요."),
    REQUEST_SUCCESS("요청이 성공적으로 처리되었습니다."),
    // Dashboard
    DASHBOARD_SUCCESS("대쉬보드가 성공적으로 조회되었습니다."),
    DASHBOARD_FAIL("대쉬보드 정보가 없습니다"),

    // Health Data
    HEALTH_DATA_UPLOADED("생체 데이터가 성공적으로 업로드되었습니다."),
    HEALTH_DATA_FOUND("생체 데이터 조회가 완료되었습니다."),
    HEALTH_DATA_SUMMARY_FOUND("생체 데이터 요약 통계 조회가 완료되었습니다."),
    HEALTH_DATA_DELETED("생체 데이터가 성공적으로 삭제되었습니다.");
    private final String message;
}
