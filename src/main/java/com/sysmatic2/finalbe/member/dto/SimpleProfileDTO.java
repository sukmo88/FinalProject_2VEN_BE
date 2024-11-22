package com.sysmatic2.finalbe.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SimpleProfileDTO {

    private String nickname;
    private String memberType;
    private String introduction;
    private String fileId;

    public SimpleProfileDTO(String nickname, String memberType, String introduction, String fileId) {
        this.nickname = nickname;
        this.memberType = memberType.replace("MEMBER_ROLE_", "");
        this.introduction = introduction;
        this.fileId = fileId;
    }

}
