package kr.co.ongil.domain.user.entity;

import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;

public enum Provider {
    LOCAL, KAKAO, GOOGLE;

    public static Provider fromString(String provider) {
        for (Provider p : Provider.values()) {
            if (p.name().equalsIgnoreCase(provider)) {
                return p;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_PROVIDER);
    }
}
