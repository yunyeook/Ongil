package kr.co.ongil.global.util;

import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import kr.co.ongil.global.security.userdetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    /**
     * 현재 사용자의 userId 추출
     */
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

        throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    /**
     * 현재 사용자의 userType 추출
     * @return "PATIENT" 또는 "QUARDIAN"
     */
    public static String getCurrentUserType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        Object principal = authentication.getPrincipal();

        // CustomUserDetails에서 userType 추출
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserType();
        }

        throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }
}
