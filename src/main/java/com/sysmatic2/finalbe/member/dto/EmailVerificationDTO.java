package com.sysmatic2.finalbe.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationDTO {

    @Email(message = "이메일 형식에 맞게 입력되어야 합니다.")
    private String email;

    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "인증번호는 6자리 숫자여야 합니다."
    )
    @NotNull(message = "6자리의 인증번호를 입력해 주세요.")
    private String verificationCode;

}
