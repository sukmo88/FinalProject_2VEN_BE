package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.member.dto.FollowingStrategyFolderDto;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FollowingStrategyFolderServiceTest {
    @Autowired
    public FollowingStrategyFolderService followingStrategyFolderService;
    @Autowired
    public MemberRepository memberRepository;
    @Autowired
    public MemberService memberService;

    @Test
    public void createDefaultFolder() {

        //기존 회원의 기본 폴더를 생성해보는 테스트
        //투자자 멤버 정보를 가져와서
        Optional<MemberEntity> memberEntityOptional = memberRepository.findByEmail("user1@example.com");
        if (memberEntityOptional.isPresent()) {
            MemberEntity memberEntity = memberEntityOptional.get();
            followingStrategyFolderService.createDefaultFolder(memberEntity);

            assertTrue(followingStrategyFolderService.getFolderList(memberEntity).get(0).getFolderName().equals("기본 폴더"));
        }
        //조회해서 있는지 확인

    }
    /*
    @Test
    public void createFolder() {
        Optional<MemberEntity> memberEntityOptional = memberRepository.findByEmail("user1@example.com");
        if (memberEntityOptional.isPresent()) {
            MemberEntity memberEntity = memberEntityOptional.get();
            FollowingStrategyFolderDto folderDto = new FollowingStrategyFolderDto();
            folderDto.setFolderName("테스트 폴더");
            followingStrategyFolderService.createFolder(folderDto);

            assertTrue(followingStrategyFolderService.getFolderList(memberEntity).get(0).getFolderName().equals("테스트 폴더"));
        }
    }
    */
    @Test
    public void deleteFolder() {
    }
}