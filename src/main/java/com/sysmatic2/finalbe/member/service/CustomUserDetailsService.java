package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // username : 토큰에서 추출한 사용자 식별자
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 데이터베이스에서 사용자 정보 조회
        Optional<MemberEntity> memberEntity = memberRepository.findByEmail(username);

        // 조회된 사용자 정보가 없을 경우 예외 발생
        //MemberEntity member = memberEntity.orElseThrow(() -> new UsernameNotFoundException("MEMBER_NOT_FOUND"));
        //예외처리
        if(memberEntity.isEmpty()){
            System.out.println("isEmpty");
            throw new UsernameNotFoundException("User not found");
        }if(memberEntity.get().getIsLoginLocked()=='Y'){
            throw new LockedException("5회 이상 로그인 실패로 계정이 잠겼습니다. 이메일 인증을 통해 비밀번호를 재설정하세요.");
        }

        // 사용자 정보 있으면 UserDetails 반환
        return new CustomUserDetails(memberEntity);
    }
}
