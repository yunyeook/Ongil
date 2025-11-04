package kr.co.ongil.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kr.co.ongil.domain.verification.entity.VerificationGrant;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 유틸리티
 *
 * - Access Token, Refresh Token (인증/인가)
 * - Verification Token (전화번호 인증)
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    @Getter
    private final long accessTokenExpiration;

    @Getter
    private final long refreshTokenExpiration;

    @Getter
    private final long verificationTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${jwt.verification.expiration}") long verificationTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.verificationTokenExpiration = verificationTokenExpiration;
    }

    // ========== Access Token, Refresh Token ==========

    public String generateAccessToken(Integer userId, String phoneNumber, String userType) {
        return generateToken(userId, phoneNumber, userType, accessTokenExpiration, "access");
    }

    public String generateRefreshToken(Integer userId, String phoneNumber, String userType) {
        return generateToken(userId, phoneNumber, userType, refreshTokenExpiration, "refresh");
    }

    private String generateToken(Integer userId, String phoneNumber, String userType, long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        JwtBuilder builder = Jwts.builder()
                .subject(phoneNumber)
                .claim("userId", userId)
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiryDate);

        // userType이 있으면 claim에 추가
        if (userType != null) {
            builder.claim("userType", userType);
        }

        return builder.signWith(secretKey).compact();
    }

    private Claims parseClaims(String token) {
//        log.info("▶▶▶ parseClaims 호출 - token length: {}, prefix: {}",
                token != null ? token.length() : "null",
                token != null ? token.substring(0, Math.min(30, token.length())) : "null");
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
//            log.info("▶▶▶ parseClaims 성공 - subject: {}, claims: {}",
                    claims.getSubject(), claims.keySet());
            return claims;
        } catch (JwtException e) {
//            log.error("▶▶▶ JWT 토큰 파싱 실패 - 에러 타입: {}, 메시지: {}",
                    e.getClass().getSimpleName(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
//            log.error("▶▶▶ JWT 토큰 파싱 실패 - IllegalArgumentException: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("userId", Integer.class);
    }

    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("type", String.class);
    }

    public String getUserTypeFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("userType", String.class);
    }

    public boolean validateToken(String token) {
//        log.info("▶▶▶ validateToken 호출됨");
        try {
            parseClaims(token);
//            log.info("▶▶▶ validateToken 성공!");
            return true;
        } catch (BusinessException e) {
//            log.error("▶▶▶ JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // ========== Verification Token (전화번호 인증) ==========

    /**
     * 전화번호 인증 토큰 생성
     *
     * @param phoneNumber 전화번호
     * @param grant       토큰 사용 목적 (SELF: 본인 인증, RELATIONSHIP: 관계 연결 인증)
     * @return JWT 토큰
     */
    public String generateVerificationToken(String phoneNumber, VerificationGrant grant) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + verificationTokenExpiration);
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
     * 토큰에서 전화번호 추출
     *
     * @param token JWT 토큰
     * @return 전화번호
     */
    public String getPhoneNumberFromToken(String token) {
//        log.info("▶▶▶ getPhoneNumberFromToken 호출됨");
        Claims claims = parseClaims(token);
        String phoneNumber = claims.get("phoneNumber", String.class);
//        log.info("▶▶▶ getPhoneNumberFromToken 성공 - phoneNumber: {}", phoneNumber);
        return phoneNumber;
    }

    /**
     * 토큰에서 Grant 추출
     *
     * @param token JWT 토큰
     * @return Grant (SELF, RELATIONSHIP)
     */
    public String getGrantFromToken(String token) {
//        log.info("▶▶▶ getGrantFromToken 호출됨");
        Claims claims = parseClaims(token);
        String grant = claims.get("grant", String.class);
//        log.info("▶▶▶ getGrantFromToken 성공 - grant: {}", grant);
        return grant;
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @param token JWT 토큰
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isTokenExpired(String token) {
//        log.info("▶▶▶ isTokenExpired 호출됨");
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            boolean expired = expiration.before(new Date());
//            log.info("▶▶▶ isTokenExpired 결과 - expired: {}, expiration: {}", expired, expiration);
            return expired;
        } catch (BusinessException e) {
//            log.error("▶▶▶ isTokenExpired 실패 - 예외 발생, true 반환");
            return true;
        }
    }

    /**
     * 토큰에서 Subject 추출
     *
     * @param token JWT 토큰
     * @return Subject
     */
    public String getSubjectFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * 토큰의 남은 만료 시간(밀리초) 계산
     *
     * @param token JWT 토큰
     * @return 남은 만료 시간 (밀리초)
     */
    public long getRemainingExpiration(String token) {
        Claims claims = parseClaims(token);
        Date expiration = claims.getExpiration();
        Date now = new Date();
        return expiration.getTime() - now.getTime();
    }
}