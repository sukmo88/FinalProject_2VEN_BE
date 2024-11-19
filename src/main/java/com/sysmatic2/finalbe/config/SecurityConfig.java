package com.sysmatic2.finalbe.config;

import com.sysmatic2.finalbe.jwt.JWTFilter;
import com.sysmatic2.finalbe.jwt.JWTUtil;
import com.sysmatic2.finalbe.jwt.LoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {

        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    /**
     * 패스워드 암호화를 위한 BCryptPasswordEncoder Bean 등록
     * @return PasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 운영 환경 (prod)에서 HTTPS를 강제하는 SecurityFilterChain 설정
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception Spring Security 설정 중 예외 발생 시
     */
    @Bean
    @Profile("prod") // 프로파일이 'prod'일 때만 활성화
    public SecurityFilterChain securityFilterChainProd(HttpSecurity http) throws Exception {
        http
                // 모든 요청을 허용하지만 HTTPS를 강제
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll() // 모든 경로를 허용
                )
                .requiresChannel(channel -> channel
                        .anyRequest().requiresSecure() // HTTPS 강제 설정
                )
                .csrf(csrf -> csrf.disable()); // CSRF 보호 비활성화

        return http.build();
    }

    /**
     * 로컬 환경 (local)에서 HTTPS 강제 설정 없이 동작하는 SecurityFilterChain 설정
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception Spring Security 설정 중 예외 발생 시
     */
    @Bean
    @Profile("local") // 프로파일이 'local'일 때만 활성화
    public SecurityFilterChain securityFilterChainLocal(HttpSecurity http) throws Exception {
        System.out.println("local 확인");
        http
                // 모든 요청을 허용하고 HTTPS 강제 설정 없음
                .authorizeHttpRequests(auth -> auth
                        //.requestMatchers("/**").permitAll() // 모든 경로를 허용
                        .requestMatchers("/api/members/login").permitAll()
                        .requestMatchers("/api/auth/**").hasAuthority("ROLE_ADMIN")
                        //.requestMatchers("/api/auth/").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable()); // CSRF 보호 비활성화

        http
                .formLogin((auth) -> auth.disable()); //From 로그인 방식 disable

        http
                .httpBasic((auth) -> auth.disable()); //http basic 인증 방식 disable

        //로그인 필터 전에 등록
        http
                .addFilterBefore(new JWTFilter(jwtUtil),LoginFilter.class);
        
        //필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration),jwtUtil), UsernamePasswordAuthenticationFilter.class);

        //JWT 사용으로 세션 비활성화 설정
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }





}