package kr.co.ongil.wear.data.model

/**
 * 워치에 전달되는 로그인 정보 데이터 클래스
 *
 * 스프링부트의 DTO와 동일한 역할
 * - 폰에서 워치로 로그인 정보 전송
 * - DataStore에 저장
 * - JSON 직렬화/역직렬화
 * - 생성자, getter,setter(var인경우만),equals,hashCode,toString,copy 메서드 자동 생성함.
 */
data class WearLoginData(
    val accessToken: String,      // JWT 액세스 토큰
    val refreshToken: String,     // JWT 리프레시 토큰
    val userId: String,           // 사용자 ID
    val userType: String,         // "PATIENT" 또는 "GUARDIAN"
    val selectedPatientId: String? = null  // 보호자인 경우 선택한 환자 ID (nullable)
)