package com.sysmatic2.finalbe.member.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.bind.annotation.RequestParam;

@Getter
@Setter
public class SignupDTO {

    // 회원타입 형식 : TRADER 또는 INVESTOR 둘 중 하나
    @Pattern(regexp = "^(TRADER|INVESTOR)$", message = "회원 유형은 'INVESTOR' 또는 'TRADER' 여야 합니다.")
    @NotNull(message = "회원 유형은 필수 입력 값입니다.")
    private String memberType;

    // email 형식 확인
    @Email(message = "이메일 형식에 맞게 입력되어야 합니다.")
    @NotNull(message = "이메일은 필수 입력 값입니다.")
    private String email;

    // 비밀번호 형식 : 영문, 숫자, 특수문자를 포함한 8자 이상
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 공백 없이 영문, 숫자, 특수문자를 하나 이상 포함한 8자 이상의 문자여야 합니다."
    )
    @NotNull(message = "비밀번호는 필수 입력 값입니다.")
    private String password;

    // 비밀번호 형식 : 영문, 숫자, 특수문자를 포함한 8자 이상
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 공백 없이 영문, 숫자, 특수문자를 하나 이상 포함한 8자 이상의 문자여야 합니다."
    )
    @NotNull(message = "비밀번호 확인은 필수 입력 값입니다.")
    private String confirmPassword;

    // nickname 형식 : 2~10자의 문자열 (특수문자 X, 한글 포함)
    @Pattern(
            regexp = "^[A-Za-z\\dㄱ-ㅎㅏ-ㅣ가-힣]{2,10}$",
            message = "닉네임은 2~10자 이내의 문자(한글, 영어, 숫자)여야 합니다."
    )
    @NotNull(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    // 휴대폰번호 형식 : 숫자 only, 10~11자
    @Pattern(
            regexp = "^\\d{10,11}$",
            message = "휴대폰번호는 '-' 없이 10~11개의 숫자여야 합니다."
    )
    @NotNull(message = "휴대폰번호는 필수 입력 값입니다.")
    private String phoneNumber;

    // 개인정보 처리방침 약관 필수 입력, true only
    @NotNull(message = "약관 동의 여부는 필수 입력 값입니다.")
    private Boolean privacyRequired;

    // 서비스 이용 약관 필수 입력, true only
    @NotNull(message = "약관 동의 여부는 필수 입력 값입니다.")
    private Boolean serviceTermsRequired;

    @NotNull(message = "약관 동의 여부는 필수 입력 값입니다.")
    private Boolean promotionOptional;

    @NotNull(message = "약관 동의 여부는 필수 입력 값입니다.")
    private Boolean marketingOptional;
}
