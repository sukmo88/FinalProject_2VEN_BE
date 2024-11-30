package com.sysmatic2.finalbe.member.dto;

import com.sysmatic2.finalbe.member.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class CustomUserDetails implements UserDetails {

    //private final MemberEntity member;
    private final Optional<MemberEntity> member;

    public CustomUserDetails(Optional<MemberEntity> member) {
        this.member = member;
    }

    public String getMemberId() {
        //return member.getMemberId();
        return member.map(MemberEntity::getMemberId).orElseThrow(()->new IllegalArgumentException("Member not found"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                String memberGradeCode = member.map(MemberEntity::getMemberGradeCode).orElse("MEMBER_USER");
                return memberGradeCode.replaceFirst("^MEMBER_", "");
            }
        });

        return authorities;
    }

    @Override
    public String getPassword() {
        return member.map(MemberEntity::getPassword).orElseThrow(()->new IllegalArgumentException("Password not found"));
    }

    @Override
    public String getUsername() {
        return member.map(MemberEntity::getEmail).orElseThrow(()->new IllegalArgumentException("email not found"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public boolean isLoginLocked(){
        return member.get().getIsLoginLocked() == 'Y';
    }

    public MemberEntity getMemberEntity() {
        return member.orElseThrow(() -> new IllegalArgumentException("MemberEntity not found in CustomUserDetails"));
    }
}
