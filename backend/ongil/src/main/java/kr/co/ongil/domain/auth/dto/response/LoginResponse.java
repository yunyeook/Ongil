package kr.co.ongil.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private UserInfo user;
    private String accessToken;
    private String refreshToken;

    @Getter
    @Builder
    public static class UserInfo {
        private Integer id;
        private String name;
        private String birth;
        private String phoneNumber;
        private String userType;
        private String profileImage;
    }
}