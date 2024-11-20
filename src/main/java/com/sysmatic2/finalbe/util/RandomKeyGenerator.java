package com.sysmatic2.finalbe.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class RandomKeyGenerator {

    private static SecureRandom random = new SecureRandom();

    // 이메일 인증 코드 생성
    public static String generateVerificationCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(10);
            code.append(num);
        }
        return code.toString();
    }

    // UUID 생성 - 22자리의 고유한 문자열
    public static String createUUID() {
        UUID uuid = UUID.randomUUID();

        // UUID를 바이트 배열로 변환
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (msb & 0xFF);
            msb >>= 8;
            bytes[8+i] = (byte) (lsb & 0xFF);
            lsb >>= 8;
        }

        // Base64로 인코딩하고 패딩 제거
        String base64UUID = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return base64UUID;
    }
}
