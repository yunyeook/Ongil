package kr.co.ongil.domain.call.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * TURN/STUN 서버 자격증명 응답
 * WebRTC P2P 연결을 위한 TURN/STUN 서버 정보
 */
@Schema(description = "TURN/STUN 서버 자격증명")
public record TurnCredentialsResponse(

    @Schema(description = "사용자명 (timestamp:앱이름 형식)", example = "1705305600:ongil")
    String username,

    @Schema(description = "자격증명 (HMAC-SHA1 기반)", example = "dGVzdGNyZWRlbnRpYWw=")
    String credential,

    @Schema(description = "TTL (초)", example = "3600")
    Long ttl,

    @Schema(description = "TURN/STUN 서버 URI 목록")
    List<String> uris
) {

    public static TurnCredentialsResponse of(
        String username,
        String credential,
        Long ttl,
        List<String> uris
    ) {
        return new TurnCredentialsResponse(username, credential, ttl, uris);
    }
}
