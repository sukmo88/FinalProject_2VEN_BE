package com.sysmatic2.finalbe.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

public class JwtTokenUtil {
    private static final String SECRET_KEY = "sysmaticsuimaticsuperwalkGrndWalkallrounder";

    public static String generateToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1시간 만료
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}