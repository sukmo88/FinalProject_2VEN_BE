package com.sysmatic2.finalbe.member.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateDTO {

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

    // 자기소개글: 글자수 150자 제한
    @Size(max = 150, message = "자기소개글은 150자 이내여야 합니다.")
    private String introduction;

    // 광고성 정보, 마케팅 수신 동의 여부
    @NotNull(message = "약관 동의 여부는 필수 입력 값입니다.")
    private Boolean marketingOptional;
}
