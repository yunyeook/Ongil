package kr.co.ongil.domain.relationship.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "관계 목록 일괄 재정렬 요청")
public record ReorderRelationshipsRequest(

        @Schema(
                description = "재정렬할 관계 ID 목록 (원하는 순서대로)",
                example = "[3, 1, 2]"
        )
        @NotEmpty(message = "관계 ID 목록은 필수입니다.")
        List<Integer> orderedRelationshipIds
) {
}
