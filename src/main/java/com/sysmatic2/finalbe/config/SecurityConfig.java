package com.sysmatic2.finalbe.config;

import com.sysmatic2.finalbe.exception.CustomAccessDeniedHandler;
import com.sysmatic2.finalbe.exception.CustomAuthenticationEntryPoint;
import com.sysmatic2.finalbe.jwt.JWTFilter;
import com.sysmatic2.finalbe.jwt.JWTUtil;
import com.sysmatic2.finalbe.jwt.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * 로컬 환경 (local)에서 HTTPS 강제 설정 없이 동작하는 SecurityFilterChain 설정
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception Spring Security 설정 중 예외 발생 시
     */
    @Bean
    @Profile("local") // 로컬 환경에서 활성화
    public SecurityFilterChain securityFilterChainLocal(HttpSecurity http) throws Exception {
        // CORS 설정
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // HTTP 요청 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.addAllowedHeader("*");
        configuration.addExposedHeader("Authorization");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        http
                .cors(cors -> cors.configurationSource(source))
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/members/details", "/api/members/change-password", "/api/members/withdrawal").authenticated()
                        .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers("/**").permitAll()
                )
                .formLogin(formLogin -> formLogin.disable()) // 폼 로그인 비활성화
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 비활성화
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    /**
     * 운영 환경 (prod)에서 HTTPS를 강제하는 SecurityFilterChain 설정
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception Spring Security 설정 중 예외 발생 시
     */
    @Bean
    @Profile("prod") // 프로덕션 환경에서 활성화
    public SecurityFilterChain securityFilterChainProd(HttpSecurity http) throws Exception {
        // CORS 설정
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("https://*"); // HTTPS 요청 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.addAllowedHeader("*");
        configuration.addExposedHeader("Authorization");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        http
                .cors(cors -> cors.configurationSource(source))
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/members/details", "/api/members/change-password", "/api/members/withdrawal").authenticated()
                        .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers("/swagger-ui/**", "/api/**").permitAll()
                        .requestMatchers("/**").permitAll()
                )
                .requiresChannel(channel -> channel.anyRequest().requiresSecure()) // HTTPS 강제
                .formLogin(formLogin -> formLogin.disable()) // 폼 로그인 비활성화
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 비활성화
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    /**
     * AuthenticationManager Bean 등록
     * @return AuthenticationManager 객체
     * @throws Exception Spring Security 설정 중 예외 발생 시
     */
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
}