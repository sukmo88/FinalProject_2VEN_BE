package com.sysmatic2.finalbe.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomKeyGeneratorTest {

    // 인증코드 생성기 반환값 -> 6자의 숫자인지 확인
    @Test
    @DisplayName("인증코드는 6개의 숫자여야 한다.")
    void testGenerateRandomKey() {
        int length = 6;
        String verificationCode = RandomKeyGenerator.generateVerificationCode(length);

        assertEquals(verificationCode.length(), length);
        assertTrue(verificationCode.matches("[0-9]{6}"));
    }

    // MemberId 생성기 반환값 -> 22자리의 문자열
    @Test
    @DisplayName("생성된 UUID 값은 22자리의 문자열이어야 한다.")
    void testCreateUUID() {
        String uuid = RandomKeyGenerator.createUUID();
        System.out.println(uuid);
        assertEquals(uuid.length(), 22);
    }
}