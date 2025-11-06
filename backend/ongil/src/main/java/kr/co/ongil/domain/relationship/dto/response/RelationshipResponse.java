package kr.co.ongil.domain.relationship.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.relationship.entity.Relationship;
import kr.co.ongil.domain.relationship.entity.RelationshipType;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.entity.UserType;

import java.time.LocalDateTime;

@Schema(description = "관계 정보 응답")
public record RelationshipResponse(

        @Schema(description = "관계 고유 ID", example = "123")
        Integer relationshipId,

        @Schema(description = "나(요청자) 사용자 ID", example = "1")
        Integer userId,

        @Schema(description = "등록된 상대 사용자 ID", example = "2")
        Integer counterpartUserId,

        @Schema(description = "관계 별칭", example = "첫째 딸")
        String relationshipName,

        @Schema(description = "관계 유형 (부모/배우자/자녀/기타)", example = "자녀")
        String relationshipType,

        @Schema(description = "정렬 순서 (낮을수록 앞에 표시)", example = "1")
        Integer displayOrder,

        @Schema(description = "대표(기본) 관계 여부", example = "true")
        Boolean isDefault,

        @Schema(description = "생성 시각", example = "2025-10-18T13:00:00")
        LocalDateTime createdAt
) {

    public static RelationshipResponse from(Relationship relationship, User requestUser) {
        User counterpartUser = relationship.getCounterpartUser(requestUser);

        String relationshipName;
        RelationshipType relationshipTypeEnum;
        Integer displayOrder;

        // 요청자가 보호자인지 환자인지에 따라 다른 필드 사용
        if (requestUser.getUserType() == UserType.GUARDIAN) {
            relationshipName = relationship.getNameSetByGuardian();
            relationshipTypeEnum = relationship.getTypeSetByGuardian();
            displayOrder = relationship.getOrderSetByGuardian();
        } else {
            relationshipName = relationship.getNameSetByPatient();
            relationshipTypeEnum = relationship.getTypeSetByPatient();
            displayOrder = relationship.getOrderSetByPatient();
        }

        // RelationshipType enum → String 변환
        String relationshipType = relationshipTypeEnum != null ? relationshipTypeEnum.getDescription() : null;

        // isDefault 조회
        boolean isDefault = relationship.isDefault(requestUser);

        return new RelationshipResponse(
                relationship.getId(),
                requestUser.getId(),
                counterpartUser != null ? counterpartUser.getId() : null,
                relationshipName,
                relationshipType,
                displayOrder,
                isDefault,
                relationship.getCreatedAt()
        );
    }
}
