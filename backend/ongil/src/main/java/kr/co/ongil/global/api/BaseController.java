package kr.co.ongil.global.api;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * API V1 공통 베이스 컨트롤러
 *
 * 모든 비즈니스 API 컨트롤러는 이 클래스를 상속받아
 * /api/v1 prefix를 자동으로 적용받습니다.
 *
 * 사용법:
 * <pre>
 * {@code
 * @RestController
 * @RequestMapping("/auth")
 * public class AuthController extends BaseController {
 *     // GET /api/v1/auth/...
 * }
 * }
 * </pre>
 *
 * 장점:
 * - API 버전 관리가 한 곳에서 가능 (v1 → v2 전환 시 이 클래스만 수정)
 * - 각 컨트롤러는 도메인 경로만 명시하면 됨
 * - /actuator, /swagger-ui 등 운영 도구는 영향 받지 않음
 */
@RequestMapping("/api/v1")
public abstract class BaseController {
    // 공통 로직이 필요하면 여기에 추가 가능
    // 예: 공통 예외 처리, 공통 헤더 설정, 공통 로깅 등
}
