package kr.co.ongil.global.util;

import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Integer userId) {
            return userId;
        }

        throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }
}
