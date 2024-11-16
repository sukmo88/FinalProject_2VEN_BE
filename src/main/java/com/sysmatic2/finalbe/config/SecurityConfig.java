package com.sysmatic2.finalbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
        http
                // 모든 요청을 허용하고 HTTPS 강제 설정 없음
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll() // 모든 경로를 허용
                )
                .csrf(csrf -> csrf.disable()); // CSRF 보호 비활성화

        return http.build();
    }
}