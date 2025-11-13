package kr.co.ongil.domain.relationship.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관계 정보 수정 요청")
public record UpdateRelationshipRequest(

        @Schema(description = "수정할 별칭", example = "사랑하는 둘째 딸")
        String relationshipName,

        @Schema(description = "수정할 관계 유형 (부모/배우자/자녀/기타)", example = "자녀")
        String relationshipType,

        @Schema(description = "수정할 정렬 순서 (낮을수록 앞에 표시)", example = "1")
        Integer displayOrder
) {
}
