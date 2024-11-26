package com.sysmatic2.finalbe.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.member.dto.CustomUserDetails;
import com.sysmatic2.finalbe.member.dto.LoginDTO;
import com.sysmatic2.finalbe.member.service.MemberService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱 도구
    private String bodyEmail;
    private String bodyPassword;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/members/login");// 인증 요청 URL 지정
    }

    //body로 받아오기
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        BufferedReader reader = null;
        LoginDTO loginDto = null;
        // 요청이 POST가 아니거나 Content-Type이 application/json이 아닌 경우 예외 처리
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new RuntimeException("Only POST method is allowed for login");
        }

        if (request.getContentType() == null || !request.getContentType().contains("application/json")) {
            throw new RuntimeException("Content-Type must be application/json");
        }

        try {
            // 요청 바디에서 JSON 읽기
            reader = request.getReader();
            loginDto = objectMapper.readValue(reader, LoginDTO.class);
            bodyEmail = loginDto.getEmail();
            bodyPassword = loginDto.getPassword();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse request body", e);
        }

        // JSON Body에서 ID와 Password 추출
        if (loginDto == null || loginDto.getEmail() == null || loginDto.getPassword() == null) {
            throw new RuntimeException("Missing email or password in request body");
        }

        // 스프링 시큐리티에서 Email과 password 검증하기 위해서는 Token에 담아야함
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                );

        // token을 검증하기 위해 authenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }


    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {
        //UserDetails
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String id = customUserDetails.getMemberId();
        String email = customUserDetails.getUsername();
        String password = customUserDetails.getPassword();

        //로그인 성공시 JWT 발급
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority authority = iterator.next();

        String role = authority.getAuthority();
        System.out.println("role:"+role);
        String token = jwtUtil.createJTwt(id,email,role,60*60*1000L);// 1시간 설정
        System.out.println("JWT 발급 success");
        // 응답 구성
        loginApiCall2(request,response,authentication);

        response.addHeader("Authorization","Bearer "+token);
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        String error = "UNKNOWN_ERROR";
        String message = "인증에 실패했습니다.";
        HttpStatus status = HttpStatus.UNAUTHORIZED; // 기본값 설정
        Instant timestamp = Instant.now();
        // 실패 원인에 따라 처리
        if (failed instanceof BadCredentialsException) {
            message = "아이디 또는 비밀번호가 일치하지 않습니다.";
            error = "INVALID_PASSWORD";
        } else if (failed instanceof LockedException || failed instanceof InternalAuthenticationServiceException) {
            message = "5회 이상 로그인 실패로 계정이 잠겼습니다. 이메일 인증을 통해 비밀번호를 재설정하세요.";
            error = "LOGIN_ATTEMPT_LIMIT_EXCEEDED";
        } else if (failed instanceof DisabledException) {
            message = "해당 계정이 존재하지 않습니다.";
            error = "MEMBER_NOT_FOUND";
            status = HttpStatus.NOT_FOUND;
        }

        // JSON 응답 생성
        response.setContentType("application/json");
        response.setStatus(status.value());

        // JSON 객체 생성 후 클라이언트로 전송
        String jsonResponse = String.format(
                //"{\"timestamp\":\"%s\",\"error\":\"%s\", \"message\":\"%s\", \"status\":%d}",
                //timestamp,error, message, status.value()
                "{\"timestamp\":\"%s\",\"error\":\"%s\", \"message\":\"%s\"}",
                timestamp,error, message, status.value()
        );
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
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
        HttpSession session = request.getSession();
        //로그인 api 호출
        System.out.println("password:"+password);
        ResponseEntity<Map<String,Object>> loginResponse = memberService.login(email,password,session);
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

    protected void loginApiCall2(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        //순환 의존성 해결을 위한 추가
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
        MemberService memberService = context.getBean(MemberService.class);
        //UserDetails
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String email = customUserDetails.getUsername();

        //로그인 api 호출
        HttpSession session = request.getSession();
        ResponseEntity<Map<String,Object>> loginResponse = memberService.login(email,bodyPassword,session);

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
