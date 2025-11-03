package kr.co.ongil.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshResponse {

    private String accessToken;
    private String refreshToken;
}
