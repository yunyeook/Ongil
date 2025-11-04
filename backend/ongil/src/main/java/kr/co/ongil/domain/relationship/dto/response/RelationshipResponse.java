package kr.co.ongil.domain.relationship.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.relationship.entity.Relationship;
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

        @Schema(description = "관계 유형", example = "딸")
        String relationshipType,

        @Schema(description = "생성 시각", example = "2025-10-18T13:00:00")
        LocalDateTime createdAt
) {

    public static RelationshipResponse from(Relationship relationship, User requestUser) {
        User counterpartUser = relationship.getCounterpartUser(requestUser);

        String relationshipName;
        String relationshipType;

        // 요청자가 보호자인지 환자인지에 따라 다른 필드 사용
        if (requestUser.getUserType() == UserType.GUARDIAN) {
            relationshipName = relationship.getNameSetByGuardian();
            relationshipType = relationship.getTypeSetByGuardian();
        } else {
            relationshipName = relationship.getNameSetByPatient();
            relationshipType = relationship.getTypeSetByPatient();
        }

        return new RelationshipResponse(
                relationship.getId(),
                requestUser.getId(),
                counterpartUser != null ? counterpartUser.getId() : null,
                relationshipName,
                relationshipType,
                relationship.getCreatedAt()
        );
    }
}
