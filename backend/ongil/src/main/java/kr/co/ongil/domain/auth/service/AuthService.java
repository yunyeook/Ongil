package kr.co.ongil.domain.auth.service;

import kr.co.ongil.domain.auth.dto.request.RegisterRequest;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.entity.Provider;
import kr.co.ongil.domain.user.entity.UserType;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.util.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final FileService fileService;
    private final UserRepository userRepository;

    public void register(RegisterRequest request) {
        log.info("회원가입 처리 중... Provider: {}, Name: {}", request.getProvider(), request.getName());

        // 중복 확인
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("이미 가입된 전화번호입니다.");
        }

        // 프로필 이미지 처리
        String profileImagePath = fileService.saveProfileImage(request.getProfileImage());

        // User 엔티티 생성
        User user = User.builder()
                .provider(Provider.valueOf(request.getProvider()))
                .providerMemberId(request.getProviderMemberId())
                .name(request.getName())
                .birth(request.getBirth())
                .phoneNumber(request.getPhoneNumber())
                .password(request.getPassword()) // 암호화는 일단 생략
                .userType(UserType.valueOf(request.getUserType()))
                .profileImage(profileImagePath)
                .build();

        // 사용자 저장
        User savedUser = userRepository.save(user);

        log.info("회원가입 완료: ID={}, Name={}", savedUser.getId(), savedUser.getName());
    }
}
