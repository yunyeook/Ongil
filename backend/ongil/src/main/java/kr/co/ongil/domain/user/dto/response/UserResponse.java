package kr.co.ongil.domain.user.dto.response;

import kr.co.ongil.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Integer id;
    private String name;
    private String birth;
    private String phoneNumber;
    private String userType;
    private String profileImage;
    private String provider;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .birth(user.getBirth())
                .phoneNumber(user.getPhoneNumber())
                .userType(user.getUserType().name())
                .profileImage(user.getProfileImage())
                .provider(user.getProvider().name())
                .build();
    }
}
