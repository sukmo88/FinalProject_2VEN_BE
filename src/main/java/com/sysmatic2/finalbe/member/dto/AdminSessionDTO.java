package com.sysmatic2.finalbe.member.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSessionDTO {
    private boolean isAuthorized;
    private String authorizationStatus;
    private String authorizedAt;
    private String expiresAt;

}
