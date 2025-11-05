package kr.co.ongil.domain.relationship.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관계 등록 요청")
public record CreateRelationshipRequest(

        @Schema(description = "전화번호 인증 후 받은 일회용 토큰", example = "eyJhbGciOi...")
        @NotBlank(message = "인증 토큰은 필수입니다.")
        String verificationToken,

        @Schema(description = "저장할 별칭", example = "첫째딸")
        @NotBlank(message = "관계 이름은 필수입니다.")
        String relationshipName,

        @Schema(description = "관계 유형 (자녀/부모/배우자/기타)", example = "딸")
        @NotBlank(message = "관계 유형은 필수입니다.")
        String relationshipType
) {
}
