package kr.co.ongil.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // AUTH
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 리소스에 접근할 권한이 없습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    SOCIAL_AUTH_FAILED(HttpStatus.BAD_REQUEST, "소셜 로그인 인증에 실패했습니다."),
    TOKEN_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 발급 중 오류가 발생했습니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    INVALID_USER_STATE(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 상태입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "이미 등록된 전화번호입니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다."),
    PHONE_VERIFICATION_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "인증 요청 횟수를 초과했습니다."),

    // PATIENT / DEVICE / CARE LINK
    PATIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 환자입니다."),
    DEVICE_NOT_REGISTERED(HttpStatus.NOT_FOUND, "등록되지 않은 기기입니다."),
    DEVICE_CONNECTION_FAILED(HttpStatus.BAD_REQUEST, "기기 연결에 실패했습니다."),
    DUPLICATE_DEVICE(HttpStatus.CONFLICT, "이미 등록된 기기입니다."),
    CARE_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "연결된 보호자 또는 환자를 찾을 수 없습니다."),
    CARE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "보호자 권한이 없습니다."),
    PATIENT_ALREADY_LINKED(HttpStatus.CONFLICT, "이미 보호자와 연결된 환자입니다."),

    // LOCATION / SAFEZONE / ABNORMAL DETECTION
    SAFEZONE_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 안전구역이 없습니다."),
    SAFEZONE_DUPLICATED(HttpStatus.CONFLICT, "이미 등록된 안전구역입니다."),
    OUT_OF_SAFEZONE(HttpStatus.BAD_REQUEST, "안전구역을 벗어났습니다."),
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "경로를 찾을 수 없습니다."),
    ROUTE_GUIDE_FAILED(HttpStatus.BAD_GATEWAY, "길찾기 안내 중 오류가 발생했습니다."),
    ABNORMAL_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이상탐지 이벤트를 찾을 수 없습니다."),
    LOCATION_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "위치 서비스가 일시적으로 사용 불가능합니다."),

    // CALL / VOICE / SOS
    CALL_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "통화 기록을 찾을 수 없습니다."),
    CALL_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "통화 연결 중 오류가 발생했습니다."),
    EMERGENCY_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "긴급 통화 요청에 실패했습니다."),
    INVALID_CALL_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 통화 유형입니다."),
    SOS_NOT_REGISTERED(HttpStatus.NOT_FOUND, "등록된 긴급 연락처가 없습니다."),

    // NOTIFICATION / SSE / ALERT
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림 정보를 찾을 수 없습니다."),
    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송 중 오류가 발생했습니다."),
    SSE_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "실시간 연결이 불안정합니다."),
    ALERT_TYPE_INVALID(HttpStatus.BAD_REQUEST, "올바르지 않은 알림 유형입니다."),

    // CONTENT / RESOURCE / DATA
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 콘텐츠입니다."),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 콘텐츠 형식입니다."),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 데이터를 찾을 수 없습니다."),
    DATA_CONFLICT(HttpStatus.CONFLICT, "데이터 충돌이 발생했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 파일이 존재하지 않습니다."),
    UNSUPPORTED_FILE_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),

    //SYSTEM / REQUEST / COMMON
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
    INVALID_INPUT(HttpStatus.UNPROCESSABLE_ENTITY, "입력값이 유효하지 않습니다. 형식을 다시 확인해주세요."),
    JSON_PARSING_ERROR(HttpStatus.BAD_REQUEST, "요청 본문 파싱 중 오류가 발생했습니다."),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 연동 중 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 서비스를 이용할 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),

    // MAP / GEOCODING / ADDRESS
    INVALID_COORDINATE(HttpStatus.BAD_REQUEST, "유효하지 않은 좌표입니다."),
    INVALID_LATITUDE(HttpStatus.BAD_REQUEST, "유효하지 않은 위도입니다. (대한민국 범위: 33~43)"),
    INVALID_LONGITUDE(HttpStatus.BAD_REQUEST, "유효하지 않은 경도입니다. (대한민국 범위: 124~132)"),
    COORDINATE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 주소의 좌표를 찾을 수 없습니다."),

    INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "유효하지 않은 주소입니다."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 좌표의 주소를 찾을 수 없습니다."),
    ROUTE_SEARCH_FAILED(HttpStatus.BAD_GATEWAY,"경로 탐색에 실패했습니다"),
    MAP_API_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "지도 API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
    MAP_API_ERROR(HttpStatus.BAD_GATEWAY, "지도 서비스와의 통신에 실패했습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."),
    PLACE_SEARCH_FAILED(HttpStatus.BAD_GATEWAY, "장소 검색 중 오류가 발생했습니다."),
    PLACE_DETAIL_FAILED(HttpStatus.BAD_GATEWAY, "장소 상세 조회 중 오류가 발생했습니다."),

    // NAVIGATION
    NAVIGATION_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "길안내 세션을 찾을 수 없습니다."),
    NAVIGATION_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "길안내 기록을 찾을 수 없습니다."),
    NAVIGATION_ALREADY_ACTIVE(HttpStatus.CONFLICT, "이미 진행 중인 길안내가 있습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
