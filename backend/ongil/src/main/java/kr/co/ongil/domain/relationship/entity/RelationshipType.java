package kr.co.ongil.domain.relationship.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 관계 유형 Enum
 *
 * 현재 지원하는 관계 유형:
 * - 부모 ↔ 자녀 (양방향 자동 매핑)
 * - 배우자 ↔ 배우자 (양방향 자동 매핑)
 * - 자녀 ↔ 부모 (양방향 자동 매핑)
 * - 기타 ↔ 기타
 *
 * 향후 확장 가능:
 * - 요양사/간병인 ↔ 환자
 * - 의료진 ↔ 환자
 * - 사회복지사 ↔ 환자
 */
@Getter
@RequiredArgsConstructor
public enum RelationshipType {

    PARENT("부모", null),      // 반대편은 CHILD (순환 참조 방지를 위해 나중에 설정)
    SPOUSE("배우자", null),    // 반대편은 SPOUSE
    CHILD("자녀", null),       // 반대편은 PARENT
    OTHER("기타", null);       // 반대편은 OTHER

    // 향후 확장 예정 (주석으로만 작성)
    // SIBLING("형제")
    // CAREGIVER("요양사", PATIENT),  // 요양사 → 환자
    // PATIENT("환자", CAREGIVER),    // 환자 → 요양사
    // MEDICAL_STAFF("의료진", PATIENT),
    // SOCIAL_WORKER("사회복지사", PATIENT)

    @JsonValue  // JSON 직렬화 시 description 값을 사용
    private final String description;

    private final RelationshipType counterpart;  // 반대편 관계 유형

    // static 블록에서 반대편 관계 설정 (순환 참조 방지)
    static {
        PARENT.setCounterpart(CHILD);
        SPOUSE.setCounterpart(SPOUSE);
        CHILD.setCounterpart(PARENT);
        OTHER.setCounterpart(OTHER);
    }

    private void setCounterpart(RelationshipType counterpart) {
        try {
            java.lang.reflect.Field field = this.getClass().getDeclaredField("counterpart");
            field.setAccessible(true);
            field.set(this, counterpart);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set counterpart relationship type", e);
        }
    }

    /**
     * 반대편 관계 유형 반환
     *
     * 예:
     * - PARENT.getCounterpart() → CHILD
     * - SPOUSE.getCounterpart() → SPOUSE
     * - CHILD.getCounterpart() → PARENT
     * - OTHER.getCounterpart() → OTHER
     *
     * @return 반대편 관계 유형
     */
    public RelationshipType getCounterpart() {
        return counterpart;
    }

    /**
     * 한글 설명(description)으로 Enum 찾기
     *
     * @param description 한글 설명 (예: "부모")
     * @return RelationshipType
     * @throws BusinessException 유효하지 않은 관계 유형인 경우
     */
    public static RelationshipType fromDescription(String description) {
        return Arrays.stream(values())
                .filter(type -> type.description.equals(description))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RELATIONSHIP_TYPE));
    }

    /**
     * 유효한 관계 유형인지 확인
     *
     * @param description 한글 설명 (예: "부모")
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValid(String description) {
        return Arrays.stream(values())
                .anyMatch(type -> type.description.equals(description));
    }

    /**
     * 모든 관계 유형 설명 목록 반환
     *
     * @return ["부모", "배우자", "자녀", "기타"]
     */
    public static String[] getAllDescriptions() {
        return Arrays.stream(values())
                .map(RelationshipType::getDescription)
                .toArray(String[]::new);
    }
}

