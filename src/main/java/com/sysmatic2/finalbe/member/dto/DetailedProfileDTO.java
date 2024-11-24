package com.sysmatic2.finalbe.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DetailedProfileDTO {
    private String fileId;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String introduction;
    private boolean marketingOptional;  // boolean 타입의 필드는 isMarketingOptional() 형식으로 Getter 생성 (Lombok 규칙)

    public DetailedProfileDTO(String fileId, String email, String nickname, String phoneNumber, String introduction, char marketingOptional) {
        this.fileId = fileId;
        this.email = email;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.introduction = introduction;
        this.marketingOptional = 'Y' == marketingOptional ? true : false;
    }
}
