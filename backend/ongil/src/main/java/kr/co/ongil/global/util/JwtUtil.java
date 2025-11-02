package kr.co.ongil.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kr.co.ongil.domain.verification.entity.VerificationGrant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 유틸리티
 *
 * - 인증 토큰 (Verification Token) 발급 및 검증
 * - Access Token, Refresh Token 발급 및 검증 (향후 추가)
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long verificationExpirationTime;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.verification.expiration}") long verificationExpirationTime
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.verificationExpirationTime = verificationExpirationTime;
    }

    // ========== 인증 토큰 (Verification Token) ==========

    /**
     * 인증 토큰 생성
     *
     * @param phoneNumber 전화번호
     * @param grant       토큰 사용 목적 (SELF, RELATIONSHIP)
     * @return JWT 토큰
     */
    public String generateVerificationToken(String phoneNumber, VerificationGrant grant) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + verificationExpirationTime);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject("phone-verification")
                .claim("phoneNumber", phoneNumber)
                .claim("grant", grant.name())
                .id(jti)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 토큰 검증
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 Claims 추출
     *
     * @param token JWT 토큰
     * @return Claims
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰에서 전화번호 추출
     *
     * @param token JWT 토큰
     * @return 전화번호
     */
    public String getPhoneNumberFromToken(String token) {
        return getClaims(token).get("phoneNumber", String.class);
    }

    /**
     * 토큰에서 Grant 추출
     *
     * @param token JWT 토큰
     * @return Grant (SELF, RELATIONSHIP)
     */
    public String getGrantFromToken(String token) {
        return getClaims(token).get("grant", String.class);
    }

    /**
     * 토큰에서 Subject 추출
     *
     * @param token JWT 토큰
     * @return Subject
     */
    public String getSubjectFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @param token JWT 토큰
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // ========== 향후 Access Token, Refresh Token 관련 메서드 추가 예정 ==========

    // TODO: generateAccessToken(Long userId, UserType userType)
    // TODO: generateRefreshToken(Long userId)
    // TODO: getUserIdFromToken(String token)
}

