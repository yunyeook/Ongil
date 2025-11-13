package kr.co.ongil.domain.user.dto.response;

import kr.co.ongil.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SearchUserResponse {

    private UserInfo user;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        private Integer id;
        private String name;
        private String phoneNumber;
        private String profileImage;

        public static UserInfo from(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .phoneNumber(user.getPhoneNumber())
                    .profileImage(user.getProfileImage())
                    .build();
        }
    }

    public static SearchUserResponse from(User user) {
        return SearchUserResponse.builder()
                .user(UserInfo.from(user))
                .build();
    }
}
