package kr.co.ongil.global.common.response;

import kr.co.ongil.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final String message;
    private final T data;

    public static <T> ApiResponse<String> fail(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getMessage(), "");
    }

    public static <T> ApiResponse<T> success(ResponseMessage responseMessage, T data) {
        return new ApiResponse<>(responseMessage.getMessage(), data);
    }

    public static ApiResponse<String> success(ResponseMessage responseMessage) {
        return new ApiResponse<>(responseMessage.getMessage(), "");
    }
}