package kr.co.ongil.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.ongil.global.security.jwt.JwtUtil;
import kr.co.ongil.global.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        String token = extractTokenFromRequest(request);

        if (token != null && jwtUtil.validateToken(token)) {

            // 액세스 토큰인지 확인
            String tokenType = jwtUtil.getTokenType(token);
            if (!"access".equals(tokenType)) {
                log.warn("잘못된 토큰 타입: {}", tokenType);
                filterChain.doFilter(request, response);
                return;
            }

            // 블랙리스트 체크 (로그아웃된 토큰인지 확인)
            if (refreshTokenRepository.isAccessTokenBlacklisted(token)) {
                log.warn("블랙리스트에 등록된 토큰입니다. (로그아웃된 토큰)");
                filterChain.doFilter(request, response);
                return;
            }

            Integer userId = jwtUtil.getUserIdFromToken(token);

            // Spring Security Context에 인증 정보 설정
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 토큰 인증 성공: userId={}", userId);
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}