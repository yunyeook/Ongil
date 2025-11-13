package kr.co.ongil.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateUserResponse {

    private UserResponse user;

    public static UpdateUserResponse from(UserResponse userResponse) {
        return UpdateUserResponse.builder()
                .user(userResponse)
                .build();
    }
}
