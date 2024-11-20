package com.sysmatic2.finalbe.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenUtilTest {

    @Test
    public void testGenerateToken() {
        String token = JwtTokenUtil.generateToken("testUser");
        assertNotNull(token); // 토큰이 null이 아니어야 함
        System.out.println("Generated Token: " + token);
    }

    @Test
    public void testTokenValidation() {
        String secretKey = "sysmaticsuimaticsuperwalkgrndwalkallrounder";
        Key signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());

        // 토큰 생성 (예: JwtTokenUtil.generateToken)
        String token = JwtTokenUtil.generateToken("testUser");

        // JwtParserBuilder를 통해 토큰 파싱
        Claims claims = Jwts.parser()
                .setSigningKey(signingKey) // 키 설정
                .build() // 파서 빌드
                .parseClaimsJws(token) // JWS 파싱
                .getBody(); // Claims 가져오기

        String subject = claims.getSubject(); // subject 가져오기
        assertEquals("testUser", subject); // subject가 올바른지 확인
    }


    private JWTUtil jwtUtil;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Test
    public void testSecretKeyInjection() {
        assertNotNull(secretKey, "Secret key should not be null"); // 값이 주입되었는지 확인
        assertEquals("yourExpectedSecretKey", secretKey, "Secret key does not match the expected value"); // 예상값과 비교
    }
}