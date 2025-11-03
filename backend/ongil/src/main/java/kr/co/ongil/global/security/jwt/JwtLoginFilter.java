package kr.co.ongil.global.security.jwt;

import kr.co.ongil.global.security.jwt.JwtUtil;
import kr.co.ongil.global.security.userdetails.CustomUserDetails;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.repository.RefreshTokenRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.auth.dto.response.LoginResponse;
import kr.co.ongil.domain.auth.dto.request.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.io.IOException;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtLoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        super.setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl("/api/v1/auth/login"); // 로그인 요청 URL 설정
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 요청에서 로그인 정보 추출
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getPhoneNumber(), loginRequest.getPassword());

            // 인증 시도
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException(ErrorCode.LOGIN_REQUEST_PARSE_FAILED.getMessage(), e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
                                            throws IOException {
        
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
        User user = userDetails.getUser();
        Integer userId = userDetails.getUserId();
        String phoneNumber = userDetails.getUsername();
        String userType = userDetails.getAuthorities().iterator().next().getAuthority();

        String accessToken = jwtUtil.generateAccessToken(userId, phoneNumber, userType);
        String refreshToken = jwtUtil.generateRefreshToken(userId, phoneNumber, userType);

        // Redis에 리프레시 토큰 저장
        refreshTokenRepository.storeRefreshToken(userId, refreshToken, jwtUtil.getRefreshTokenExpiration());

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .name(user.getName())
                .birth(user.getBirth())
                .phoneNumber(user.getPhoneNumber())
                .userType(user.getUserType().name())
                .profileImage(user.getProfileImage())
                .build();
        
        LoginResponse loginResponse = LoginResponse.builder()
                .user(userInfo)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        ApiResponse<LoginResponse> apiResponse = ApiResponse.success(ResponseMessage.LOGIN_SUCCESS, loginResponse);

        String responseBody = objectMapper.writeValueAsString(apiResponse);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(responseBody);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        ApiResponse<String> apiResponse = ApiResponse.fail(ErrorCode.LOGIN_FAILED);

        String responseBody = objectMapper.writeValueAsString(apiResponse);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(responseBody);
    }
}
