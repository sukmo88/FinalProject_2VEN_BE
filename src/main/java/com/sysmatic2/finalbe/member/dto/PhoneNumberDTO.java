package com.sysmatic2.finalbe.member.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneNumberDTO {

    // 휴대폰번호 형식 : 숫자 only, 10~11자
    @Pattern(
            regexp = "^\\d{10,11}$",
            message = "휴대폰번호는 '-' 없이 10~11개의 숫자여야 합니다."
    )
    @NotNull(message = "휴대폰번호는 필수 입력 값입니다.")
    private String phoneNumber;
}
