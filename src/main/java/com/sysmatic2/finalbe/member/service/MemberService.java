package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.member.dto.SignupDTO;
import com.sysmatic2.finalbe.member.entity.MemberEntity;

public interface MemberService {
    public void signUp(SignupDTO signupDTO);
}
