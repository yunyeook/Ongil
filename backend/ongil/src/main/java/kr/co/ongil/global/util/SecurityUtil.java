package kr.co.ongil.global.util;

import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.security.userdetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        Object principal = authentication.getPrincipal();

        // CustomUserDetails에서 userId 추출
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();
        }

        // 이전 호환성을 위해 Integer도 지원
        if (principal instanceof Integer userId) {
            return userId;
        }

        throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }
}
