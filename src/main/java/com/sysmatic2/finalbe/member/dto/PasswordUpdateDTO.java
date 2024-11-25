package com.sysmatic2.finalbe.member.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateDTO {

    private String oldPassword;

    // 비밀번호 형식 : 영문, 숫자, 특수문자를 포함한 8자 이상
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 공백 없이 영문, 숫자, 특수문자를 하나 이상 포함한 8자 이상의 문자여야 합니다."
    )
    @NotNull(message = "비밀번호 확인은 필수 입력 값입니다.")
    private String newPassword;

    // 비밀번호 형식 : 영문, 숫자, 특수문자를 포함한 8자 이상
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 공백 없이 영문, 숫자, 특수문자를 하나 이상 포함한 8자 이상의 문자여야 합니다."
    )
    @NotNull(message = "비밀번호 확인은 필수 입력 값입니다.")
    private String confirmPassword;

}
