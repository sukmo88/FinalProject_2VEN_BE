package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.MemberNotFoundException;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import org.springframework.stereotype.Component;

@Component
public class MemberHelper {

    private final MemberRepository memberRepository;

    public MemberHelper(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void initMemberFileId(String uploaderId, String fileId, String filePath) {
        MemberEntity memberEntity = memberRepository.findById(uploaderId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found for uploaderId: " + uploaderId));
        memberEntity.setFileId(fileId);
        memberEntity.setProfilePath(filePath);
        memberRepository.save(memberEntity);
    }
}
