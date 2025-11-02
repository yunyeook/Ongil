package kr.co.ongil.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Provider는 필수입니다.")
    private String provider;

    private String providerMemberId;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 30, message = "이름은 30자 이하로 입력해주세요.")
    private String name;

    @Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자로 입력해주세요. (예: 19970306)")
    private String birth;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]\\d{8}$", message = "전화번호 형식이 올바르지 않습니다. (예: 01012345678)")
    private String phoneNumber;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 30, message = "비밀번호는 8자 이상 30자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "사용자 유형은 필수입니다.")
    @Pattern(regexp = "^(PATIENT|GUARDIAN)$", message = "사용자 유형은 PATIENT 또는 GUARDIAN이어야 합니다.")
    private String userType;

    private MultipartFile profileImage;
}