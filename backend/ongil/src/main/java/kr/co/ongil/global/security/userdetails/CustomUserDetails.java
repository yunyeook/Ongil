package kr.co.ongil.global.security.userdetails;

import kr.co.ongil.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Integer userId;
    private final String username; // phoneNumber
    private final String userType; // "PATIENT" 또는 "GUARDIAN"
    private final Collection<? extends GrantedAuthority> authorities;

    // DB에서 User 엔티티를 받아 CustomUserDetails 생성
    private CustomUserDetails(User user) {
        this.user = user;
        this.userId = user.getId();
        this.username = user.getPhoneNumber();
        this.userType = user.getUserType().name(); // PATIENT, GUARDIAN
        // UserType을 ROLE_로 변환 (예: PATIENT -> ROLE_PATIENT)
        this.authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getUserType().name())
        );
    }

    // 토큰 정보로 CustomUserDetails 생성
    private CustomUserDetails(Integer userId, String username, String userType) {
        this.user = null; // DB 조회 없이 토큰 정보만으로 생성할 때는 null
        this.userId = userId;
        this.username = username;
        this.userType = userType; // "PATIENT" 또는 "GUARDIAN"
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(userType));
    }

    public static CustomUserDetails from(User user) {
        return new CustomUserDetails(user);
    }

    public static CustomUserDetails fromToken(Integer userId, String username, String userType) {
        return new CustomUserDetails(userId, username, userType);
    }

    /**
     * 사용자 타입 반환
     * @return "PATIENT" 또는 "GUARDIAN"
     */
    public String getUserType() {
        return this.userType;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        if (user == null) {
            return null; // 토큰 기반 인증에서는 비밀번호가 필요 없음
        }
        return user.getPassword(); // 로그인 시에는 비밀번호 필요
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (user == null) {
            return true; // 토큰 기반 인증에서는 사용자 상태를 확인할 수 없음
        }
        return user.getDeletedAt() == null;
    }
}