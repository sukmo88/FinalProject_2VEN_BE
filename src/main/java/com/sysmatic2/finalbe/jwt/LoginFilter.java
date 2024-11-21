package com.sysmatic2.finalbe.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.member.service.MemberService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.security.AuthProvider;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        //UserDetails
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String id = customUserDetails.getMemberId();
        String email = customUserDetails.getUsername();
        //로그인 성공시 JWT 발급
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority authority = iterator.next();

        String role = authority.getAuthority();
        System.out.println("role:"+role);
        String token = jwtUtil.createJTwt(id,email,role,60*60*1000L);// 1시간 설정
        System.out.println("JWT 발급 success");
        // 응답 구성
        loginApiCall(request,response,authentication);

        response.addHeader("Authorization","Bearer "+token);
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        //순환 의존성 해결을 위한 추가
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
        MemberService memberService = context.getBean(MemberService.class);
        System.out.println("failed:"+failed.getMessage());
        System.out.println("JWT 발급실패 unsuccessfulAuthentication");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    protected void loginApiCall(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        //순환 의존성 해결을 위한 추가
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
        MemberService memberService = context.getBean(MemberService.class);
        //UserDetails
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String id = customUserDetails.getMemberId();
        String email = customUserDetails.getUsername();
        String password =request.getParameter("password");
        //로그인 api 호출
        System.out.println("password:"+password);
        ResponseEntity<Map<String,Object>> loginResponse = memberService.login(email,password);
        if(loginResponse.getStatusCode() != HttpStatus.OK){
            // 로그인 실패 시 처리 (예: 잠금 상태, 잘못된 비밀번호 등)
            Map<String, Object> errorBody = loginResponse.getBody();
            response.setStatus(loginResponse.getStatusCodeValue());
            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorBody));
            return;
        }
        Map<String, Object> successBody = loginResponse.getBody(); // login API의 응답 데이터 사용
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(successBody));
    }
}
