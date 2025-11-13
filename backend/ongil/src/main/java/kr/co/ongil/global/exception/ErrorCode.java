package kr.co.ongil.global.exception;

import kr.co.ongil.global.common.response.ApiResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public enum ErrorCode {

    // AUTH
    USER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 회원에 대한 접근 권한이 없습니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 리소스에 접근할 권한이 없습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다."),
    OLD_PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다."),
    PASSWORD_CONFIRMATION_MISMATCH(HttpStatus.BAD_REQUEST, "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    SOCIAL_AUTH_FAILED(HttpStatus.BAD_REQUEST, "소셜 로그인 인증에 실패했습니다."),
    TOKEN_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 발급 중 오류가 발생했습니다."),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 제공자입니다."),
    INVALID_USER_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 유형입니다."),
    LOGIN_REQUEST_PARSE_FAILED(HttpStatus.BAD_REQUEST, "로그인 요청 파싱에 실패했습니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    INVALID_USER_STATE(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 상태입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "이미 등록된 전화번호입니다."),

    // PHONE VERIFICATION
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.GONE, "인증번호가 만료되었습니다."),
    VERIFICATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "인증번호를 찾을 수 없습니다. 먼저 인증번호를 요청해주세요."),
    VERIFICATION_ATTEMPTS_EXCEEDED(HttpStatus.UNAUTHORIZED, "인증번호 입력 횟수를 초과했습니다. 다시 요청해주세요."),
    PHONE_VERIFICATION_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "인증번호 요청 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),
    PHONE_VERIFICATION_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "인증번호 재요청은 1분 후에 가능합니다."),
    IP_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "요청 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),
    VERIFICATION_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "전화번호 변경 시 인증 토큰이 필요합니다."),
    PHONE_NUMBER_MISMATCH(HttpStatus.BAD_REQUEST, "인증된 전화번호와 요청한 전화번호가 일치하지 않습니다."),

    // SMS
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 전송 중 오류가 발생했습니다."),

    // PATIENT / DEVICE / CARE LINK
    PATIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 환자입니다."),
    DEVICE_NOT_REGISTERED(HttpStatus.NOT_FOUND, "등록되지 않은 기기입니다."),
    DEVICE_CONNECTION_FAILED(HttpStatus.BAD_REQUEST, "기기 연결에 실패했습니다."),
    DUPLICATE_DEVICE(HttpStatus.CONFLICT, "이미 등록된 기기입니다."),
    NO_GUARDIAN_FOUND(HttpStatus.NOT_FOUND, "관계 등록된 보호자가 없습니다."),
    PATIENT_ALREADY_LINKED(HttpStatus.CONFLICT, "이미 보호자와 연결된 환자입니다."),

    // HEALTH DATA
    HEALTH_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "생체 데이터를 찾을 수 없습니다."),
    INVALID_HEALTH_DATA_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 생체 데이터 유형입니다."),
    HEALTH_DATA_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 환자의 생체 데이터에 접근할 권한이 없습니다."),
    DUPLICATE_HEALTH_DATA(HttpStatus.CONFLICT, "중복된 생체 데이터가 존재합니다."),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 형식입니다. (yyyyMMdd 형식을 사용해주세요)"),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "잘못된 날짜 범위입니다. 시작 날짜는 종료 날짜보다 이전이어야 합니다."),

    // RELATIONSHIP
    RELATIONSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 관계를 찾을 수 없습니다."),
    RELATIONSHIP_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 관계입니다."),
    RELATIONSHIP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 관계에 대한 권한이 없습니다."),
    SAME_USER_TYPE_RELATIONSHIP(HttpStatus.FORBIDDEN, "동일한 역할 간에는 등록할 수 없습니다."),
    SELF_RELATIONSHIP_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신과는 관계를 등록할 수 없습니다."),
    INVALID_VERIFICATION_TOKEN(HttpStatus.UNPROCESSABLE_ENTITY, "토큰이 유효하지 않습니다."),
    VERIFICATION_TOKEN_EXPIRED(HttpStatus.GONE, "토큰이 만료되었거나 이미 사용되었습니다."),
    COUNTERPART_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    INVALID_RELATIONSHIP_GRANT(HttpStatus.UNPROCESSABLE_ENTITY, "관계 등록용 토큰이 아닙니다."),
    INVALID_RELATIONSHIP_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 관계 유형입니다. (부모/배우자/자녀/형제/기타)"),

    // FAVORITE
    FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "즐겨찾기를 찾을 수 없습니다."),
    FAVORITE_ALREADY_EXISTS(HttpStatus.CONFLICT, "동일한 즐겨찾기가 이미 존재합니다."),
    FAVORITE_DEFAULT_NOT_CANCELED(HttpStatus.FORBIDDEN, "기본 길찾기를 해제할 수 없습니다."),

    // LOCATION / SAFEZONE / ABNORMAL DETECTION
    SAFEZONE_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 안전범위이 없습니다."),
    SAFEZONE_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "안전범위 설정이 존재하지 않습니다."),
    OUT_OF_SAFEZONE(HttpStatus.BAD_REQUEST, "안전범위을 벗어났습니다."),
    INVALID_SAFEZONE_BOUNDARY(HttpStatus.BAD_REQUEST, "잘못된 안전범위 값입니다."),
    SAFEZONE_BOUNDARY_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "안전범위가 허용 범위를 벗어났습니다."),
    SAFEZONE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 환자에 대한 수정 권한이 없습니다."),
    ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "경로를 찾을 수 없습니다."),
    ROUTE_GUIDE_FAILED(HttpStatus.BAD_GATEWAY, "길찾기 안내 중 오류가 발생했습니다."),
    ABNORMAL_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이상탐지 이벤트를 찾을 수 없습니다."),
    LOCATION_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "위치 서비스가 일시적으로 사용 불가능합니다."),
    ABNORMAL_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 이상탐지 로그를 찾을 수 없습니다."),


    // CALL / VOICE / SOS
    CALL_NOT_FOUND(HttpStatus.NOT_FOUND, "통화 세션을 찾을 수 없습니다."),
    CALL_ALREADY_CONNECTED(HttpStatus.CONFLICT, "이미 연결된 통화입니다."),
    CALL_ALREADY_ENDED(HttpStatus.CONFLICT, "이미 종료된 통화입니다."),
    RECEIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "수신자를 찾을 수 없습니다."),
    USER_ALREADY_IN_CALL(HttpStatus.CONFLICT, "이미 통화 중인 사용자입니다."),
    CALL_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "통화 권한이 없습니다."),
    CANNOT_CALL_SELF(HttpStatus.BAD_REQUEST, "자기 자신에게 통화할 수 없습니다."),
    INVALID_CALL_STATUS(HttpStatus.BAD_REQUEST, "올바르지 않은 통화 상태입니다."),
    CALL_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "통화 연결 중 오류가 발생했습니다."),
    EMERGENCY_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "긴급 통화 요청에 실패했습니다."),
    INVALID_CALL_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 통화 유형입니다."),
    SOS_NOT_REGISTERED(HttpStatus.NOT_FOUND, "등록된 긴급 연락처가 없습니다."),

    // CALL LOG
    CALL_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "통화 기록을 찾을 수 없습니다."),
    CALL_LOG_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "통화 기록 접근 권한이 없습니다."),

    // CALL RECORDING
    CALL_RECORDING_NOT_FOUND(HttpStatus.NOT_FOUND, "통화 녹음을 찾을 수 없습니다."),
    CALL_RECORDING_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 통화에 녹음 정보가 등록되어 있습니다."),

    // NOTIFICATION / SSE / ALERT
    // SOS
    SOS_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 도움 요청 내역을 찾을 수 없습니다."),
    SOS_CALLBACK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 환자 워치의 콜백 권한이 없습니다."),
    SOS_ALREADY_ACKNOWLEDGED(HttpStatus.CONFLICT, "이미 재생 완료로 처리된 요청입니다."),
    // SOS
    SOS_STOP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "요청에 대한 접근 권한이 없습니다."),
    ACTIVE_SOS_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 환자 또는 도움 요청을 찾을 수 없습니다."),
    SOS_ALREADY_STOPPED(HttpStatus.CONFLICT, "이미 종료된 도움 요청입니다."),

    // NOTIFICATION
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림 정보를 찾을 수 없습니다."),
    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송 중 오류가 발생했습니다."),
    SSE_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "실시간 연결이 불안정합니다."),
    ALERT_TYPE_INVALID(HttpStatus.BAD_REQUEST, "올바르지 않은 알림 유형입니다."),
    INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 알림 유형입니다."),

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
    NAVIGATION_ALREADY_ACTIVE(HttpStatus.CONFLICT, "이미 진행 중인 길안내가 있습니다."),

    // REDIS
    REDIS_DESERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"Redis 데이터 역직렬화에 실패했습니다." ),
    REDIS_SESSION_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 세션 저장에 실패했습니다."),

    //SYSTEM / REQUEST / COMMON
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
    INVALID_INPUT(HttpStatus.UNPROCESSABLE_ENTITY, "입력값이 유효하지 않습니다. 형식을 다시 확인해주세요."),
    JSON_PARSING_ERROR(HttpStatus.BAD_REQUEST, "요청 본문 파싱 중 오류가 발생했습니다."),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 연동 중 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 서비스를 이용할 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,"지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다."),

    //DASHBOARD
    DASHBOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "대쉬보드가 존재하지 않습니다.");
    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
