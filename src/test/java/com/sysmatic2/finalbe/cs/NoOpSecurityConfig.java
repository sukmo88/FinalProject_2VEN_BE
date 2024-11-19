package com.sysmatic2.finalbe.cs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class NoOpSecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable()) // 새로운 방식으로 CSRF 비활성화
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // 모든 요청 허용
    return http.build();
  }
}
