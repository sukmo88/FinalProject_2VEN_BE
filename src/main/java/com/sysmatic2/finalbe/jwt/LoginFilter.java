package com.sysmatic2.finalbe.jwt;

import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/members/login");// 인증 요청 URL 지정
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("attemptAuthentication");
        //클라이언트 요청에서 username, password 추출
        //String username = obtainUsername(request);
        String id = request.getParameter("id");
        String password = obtainPassword(request);

        System.out.println("attemptAuthentication:"+id);
        System.out.println("password:"+password);

        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        //UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(id, password, null);
        //token에 담은 검증을 위한 AuthenticationManager로 전달
        System.out.println("autoToken:"+authToken);
        return authenticationManager.authenticate(authToken);
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        //UserDetails
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String id = customUserDetails.getMemberId();
        String email = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority authority = iterator.next();

        String role = authority.getAuthority();
        System.out.println("role:"+role);
        String token = jwtUtil.createJTwt(id,email,role,60*60*1000L);// 1시간 설정
        System.out.println("JWT 발급 success");
        response.addHeader("Authorization","Bearer "+token);
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        System.out.println("failed:"+failed.getMessage());
        System.out.println("JWT 발급실패 unsuccessfulAuthentication");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

}
