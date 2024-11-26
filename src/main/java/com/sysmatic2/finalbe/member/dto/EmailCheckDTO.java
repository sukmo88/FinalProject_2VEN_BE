package com.sysmatic2.finalbe.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCheckDTO {

    // email 형식 확인
    @Email(message = "이메일 형식에 맞게 입력되어야 합니다.")
    @NotNull(message = "이메일은 필수 입력 값입니다.")
    private String email;
}
