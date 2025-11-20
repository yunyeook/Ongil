package kr.co.ongil.domain.relationship.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.user.entity.User;

import java.time.LocalDateTime;

@Schema(description = "상대 사용자 정보(비밀번호/Provider/ProviderMemberId 제외)")
public record CounterpartUserResponse(

    @Schema(description = "상대 사용자 ID", example = "2")
    Integer userId,

    @Schema(description = "이름", example = "홍길동")
    String name,

    @Schema(description = "생년월일(YYYYMMDD)", example = "19650101")
    String birth,

    @Schema(description = "전화번호", example = "01012345678")
    String phoneNumber,

    @Schema(description = "사용자 유형 (PATIENT/GUARDIAN)", example = "PATIENT")
    String userType,

    @Schema(description = "프로필 이미지 URL")
    String profileImage,

    @Schema(description = "생성 시각", example = "2025-10-18T13:00:00")
    LocalDateTime createdAt
) {
    public static CounterpartUserResponse from(User user) {
        if (user == null) return null;
        return new CounterpartUserResponse(
            user.getId(),
            user.getName(),
            user.getBirth(),
            user.getPhoneNumber(),
            user.getUserType() != null ? user.getUserType().name() : null,
            user.getProfileImage(),
            user.getCreatedAt()
        );
    }
}
