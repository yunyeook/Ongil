package kr.co.ongil.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpdateUserRequest {

    @Size(max = 30, message = "이름은 30자 이하로 입력해주세요.")
    private String name;

    @Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자로 입력해주세요. (예: 19970306)")
    private String birth;

    @Pattern(regexp = "^01[0-9]\\d{8}$", message = "전화번호 형식이 올바르지 않습니다. (예: 01012345678)")
    private String phoneNumber;

    private String verificationToken;

    private MultipartFile profileImage;
}
