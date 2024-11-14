package com.sysmatic2.finalbe.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class SignupDTO {

    // 회원타입 형식 : TRADER 또는 INVESTOR 둘 중 하나
    @Pattern(regexp = "^(TRADER|INVESTOR)$", message = "Type must be either TRADER or INVESTOR")
    private String memberType;

    // email 형식 확인
    @Email
    private String email;

    // 비밀번호 형식 : 영문 숫자를 포함한 8~10자
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,10}$",
            message = "Must be 6 to 10 characters long, contain at least one letter and one number, and have no spaces."
    )
    private String password;

    private String confirmPassword;

    // nickname 형식 : 2~10자의 문자열 (특수문자 X)
    @Pattern(
            regexp = "^[A-Za-z\\d]{2,10}$",
            message = "Must be 2 to 10 characters long and contain only letters and numbers (no special characters)."
    )
    private String nickname;

    // 휴대폰번호 형식 : 숫자 only, 10~11자
    @Pattern(
            regexp = "^\\d{10,11}$",
            message = "Must be 10 to 11 digits long and contain only numbers."
    )
    private String phoneNumber;
}
