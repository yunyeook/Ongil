package kr.co.ongil.domain.user.entity;

import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;

public enum UserType {
    PATIENT, GUARDIAN;

    public static UserType fromString(String type) {
        for (UserType userType : UserType.values()) {
            if (userType.name().equalsIgnoreCase(type)) {
                return userType;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_USER_TYPE);
    }
}