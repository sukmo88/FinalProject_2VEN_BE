package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // username : 토큰에서 추출한 사용자 식별자
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 데이터베이스에서 사용자 정보 조회
        MemberEntity member = memberRepository.findByEmail(username);

        // 조회된 사용자 정보가 없을 경우 예외 발생
        if (member == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // 사용자 정보 있으면 UserDetails 반환
        return new CustomUserDetails(member);
    }
}
