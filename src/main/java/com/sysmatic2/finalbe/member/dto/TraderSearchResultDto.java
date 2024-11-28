package com.sysmatic2.finalbe.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TraderSearchResultDto {
    private String memberId;
    private String nickname;
    private String introduction;
    private String fileId;
    private String profilePath;
    private Integer strategyCnt;
}
