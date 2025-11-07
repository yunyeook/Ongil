package kr.co.ongil.domain.call.service;

import kr.co.ongil.domain.call.dto.response.TurnCredentialsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * TURN/STUN 서버 자격증명 발급 서비스
 * coturn REST API 방식 사용
 */
@Slf4j
@Service
public class TurnCredentialsService {

    @Value("${turn.shared-secret:your-turn-secret-key-here}")
    private String sharedSecret;

    @Value("${turn.ttl:3600}")
    private Long ttl;

    @Value("${turn.server.host:turn.ongil.app}")
    private String turnHost;

    @Value("${turn.server.port:3478}")
    private Integer turnPort;

    /**
     * TURN/STUN 서버 자격증명 발급
     * coturn REST API 방식: timestamp:username, HMAC-SHA1(secret, username)
     */
    public TurnCredentialsResponse generateCredentials() {
        try {
            // 1. 만료 시간 계산 (현재 시각 + TTL)
            long timestamp = (System.currentTimeMillis() / 1000) + ttl;

            // 2. username 생성 (timestamp:ongil 형식)
            String username = timestamp + ":ongil";

            // 3. HMAC-SHA1 기반 credential 생성
            String credential = generateHmacSha1(username, sharedSecret);

            // 4. TURN/STUN URI 목록
            List<String> uris = List.of(
                String.format("turn:%s:%d?transport=udp", turnHost, turnPort),
                String.format("turn:%s:%d?transport=tcp", turnHost, turnPort),
                String.format("stun:%s:%d", turnHost, turnPort)
            );

            log.info("TURN 자격증명 발급: username={}, ttl={}", username, ttl);

            return TurnCredentialsResponse.of(username, credential, ttl, uris);

        } catch (Exception e) {
            log.error("TURN 자격증명 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate TURN credentials", e);
        }
    }

    /**
     * HMAC-SHA1 기반 credential 생성
     */
    private String generateHmacSha1(String data, String secret) throws Exception {
        Mac hmacSha1 = Mac.getInstance("HmacSHA1");
        SecretKeySpec secretKey = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA1"
        );
        hmacSha1.init(secretKey);

        byte[] hash = hmacSha1.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
