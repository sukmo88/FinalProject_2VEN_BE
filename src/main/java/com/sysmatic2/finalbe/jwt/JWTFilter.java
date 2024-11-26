package com.sysmatic2.finalbe.jwt;

import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    //발급된 JWT토큰의 유효성을 검사하는 필터
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("/api/members/login".equals(path)) {
            filterChain.doFilter(request, response); // 로그인 api는 필터를 건너뜀
            return;
        }
        //request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if(authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료(필수)
            return;
        }

        //토큰 소멸 시간 검증
        String token = authorization.split(" ")[1];
        System.out.println("token:"+token);
        if(jwtUtil.isExpired(token)){
            System.out.println("token expired");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료(필수)
            return;
        }
        //토큰에서 username과 role 획득
        String id = jwtUtil.getId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
        System.out.println("username:"+username);
        System.out.println("role:"+role);

        //userEntity를 생성하여 값 set
        Optional <MemberEntity> memberEntityOptional = Optional.of(new MemberEntity());
        memberEntityOptional.ifPresent(memberEntity -> {
            memberEntity.setMemberId(id);
            memberEntity.setEmail(username);
            memberEntity.setPassword("ddddsadd");
            memberEntity.setMemberGradeCode(role);
        });
        //UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(memberEntityOptional);
        //Authentication 구현체 생성(회원정보객체,인증자격증명(null),권한정보)
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        System.out.println("authToken:"+authToken);
        System.out.println("customUserDetails 권한:"+customUserDetails.getAuthorities());

        //Authentication 객체 인증이 성공하면 SecurityContextHolder에 저장됨
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
