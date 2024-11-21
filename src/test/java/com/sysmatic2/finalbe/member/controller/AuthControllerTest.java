package com.sysmatic2.finalbe.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.exception.EmailVerificationFailedException;
import com.sysmatic2.finalbe.member.dto.EmailVerificationDTO;
import com.sysmatic2.finalbe.member.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    // EmailVerificationDTO 객체 생성
    private EmailVerificationDTO createEmailVerificationDTO(String verificationCode) {
        EmailVerificationDTO emailVerificationDTO = new EmailVerificationDTO();
        emailVerificationDTO.setEmail("test@test.com");
        emailVerificationDTO.setVerificationCode(verificationCode);
        return emailVerificationDTO;
    }

    // DTO를 JSON 문자열로 변환 (ObjectMapper 사용)
    private String dtoToJson(EmailVerificationDTO emailVerificationDTO) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(emailVerificationDTO);
        return requestBody;
    }

    // 이메일 인증번호 검증 테스트
    // 인증만료시간 이내, 인증번호 일치 -> 세션에서 인증 정보 얻어왔을 때 true & 성공응답
    @WithMockUser(username = "testUser", roles = "USER")
    @Test
    @DisplayName("인증시간만료 이내이고 인증번호 일치하면 성공응답 발생")
    public void checkVerificationCodeTest_success() throws Exception {
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);
        String savedVerificationCode = "123456";
        String inputVerificationCode = "123456";

        // MockHttpSession 생성 후 값 저장
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("expiryTime", expiryTime);
        session.setAttribute("verificationCode", savedVerificationCode);

        // DTO 객체 생성
        EmailVerificationDTO emailVerificationDTO = createEmailVerificationDTO(inputVerificationCode);

        // DTO를 JSON 문자열로 변환
        String requestBody = dtoToJson(emailVerificationDTO);

        // MockMvc로 요청 수행 후 응답 확인
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/check-verification-code")
                        .with(csrf())                              // CSRF 토큰 추가
                        .session(session)                          // 세션 추가
                        .contentType("application/json")           // Content-Type 설정
                        .content(requestBody))                     // Request Body에 JSON 데이터 추가
                .andExpect(status().isOk())                    // HTTP 상태 코드 검증
                .andExpect(jsonPath("$.status").value("success"))  // 응답 JSON 검증
                .andExpect(jsonPath("$.message").value("이메일 인증에 성공하였습니다."));

        // Service 메서드 호출 검증
        verify(authService, times(1)).validateVerificationCode(inputVerificationCode, savedVerificationCode, expiryTime);
    }



    // 세션 == null, 만료시간 == null, 인증코드 == null 시 예외 발생 테스트
    @WithMockUser(username = "testUser", roles = "USER")
    @Test
    @DisplayName("세션이 유효하지 않아 인증 실패 시 예외 발생, 응답 확인")
    public void checkVerificationCodeTest_failure1() throws Exception {
        String inputVerificationCode = "123456";

        // DTO 객체 생성
        EmailVerificationDTO emailVerificationDTO = createEmailVerificationDTO(inputVerificationCode);

        // DTO를 JSON 문자열로 변환
        String requestBody = dtoToJson(emailVerificationDTO);

        // 세션을 제공하지 않고 MockMvc 호출
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/check-verification-code")
                        .with(csrf())                              // CSRF 토큰 추가
                        .contentType("application/json")           // Content-Type 설정
                        .content(requestBody))                     // Request Body에 JSON 데이터 추가
                .andExpect(status().isBadRequest())                // HTTP 상태 코드 검증
                .andExpect(jsonPath("$.error").value("EMAIL_VERIFICATION_FAILED")) // 응답 JSON 검증
                .andExpect(jsonPath("$.errorType").value("EmailVerificationFailedException"))
                .andExpect(jsonPath("$.message").value("이메일 인증에 실패하였습니다."));
    }


    // 인증번호 불일치 -> 예외 응답코드 확인
    @WithMockUser(username = "testUser", roles = "USER")
    @Test
    @DisplayName("인증번호 불일치로 인증실패 시 예외 발생, 응답 확인")
    public void checkVerificationCodeTest_failure2() throws Exception {
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);
        String savedVerificationCode = "123456";
        String inputVerificationCode = "654321";

        // DTO 객체 생성
        EmailVerificationDTO emailVerificationDTO = createEmailVerificationDTO(inputVerificationCode);

        // DTO를 JSON 문자열로 변환
        String requestBody = dtoToJson(emailVerificationDTO);

        // MockHttpSession 생성 후 값 저장
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("expiryTime", expiryTime);
        session.setAttribute("verificationCode", savedVerificationCode);

        // MockService 에서 인증번호 검증하는 메소드 호출 시 예외 발생하도록 지정
        Mockito.doThrow(new EmailVerificationFailedException("인증번호가 일치하지 않습니다."))
                        .when(authService).validateVerificationCode(inputVerificationCode, savedVerificationCode, expiryTime);

        // MockMvc로 요청 수행 후 응답 확인
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/check-verification-code")
                        .with(csrf())
                        .session(session)
                        .contentType("application/json")           // Content-Type 설정
                        .content(requestBody))                     // Request Body에 JSON 데이터 추가
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EMAIL_VERIFICATION_FAILED"))
                .andExpect(jsonPath("$.errorType").value("EmailVerificationFailedException"))
                .andExpect(jsonPath("$.message").value("인증번호가 일치하지 않습니다."));

        verify(authService, times(1)).validateVerificationCode(inputVerificationCode, savedVerificationCode, expiryTime);
    }

    // 인증시간만료 -> 예외 응답코드 확인
    @WithMockUser(username = "testUser", roles = "USER")
    @Test
    @DisplayName("인증만료로 인증실패 시 예외 발생, 응답 확인")
    public void checkVerificationCodeTest_failure3() throws Exception {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(5);
        String savedVerificationCode = "123456";
        String inputVerificationCode = "123456";

        // DTO 객체 생성
        EmailVerificationDTO emailVerificationDTO = createEmailVerificationDTO(inputVerificationCode);

        // DTO를 JSON 문자열로 변환
        String requestBody = dtoToJson(emailVerificationDTO);

        // MockHttpSession 생성 후 값 저장
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("expiryTime", expiryTime);
        session.setAttribute("verificationCode", savedVerificationCode);

        // MockService 에서 인증번호 검증하는 메소드 호출 시 예외 발생하도록 지정
        Mockito.doThrow(new EmailVerificationFailedException("인증시간이 만료되었습니다."))
                .when(authService).validateVerificationCode(inputVerificationCode, savedVerificationCode, expiryTime);

        // MockMvc로 요청 수행 후 응답 확인
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/check-verification-code")
                        .with(csrf())
                        .session(session)
                        .contentType("application/json")           // Content-Type 설정
                        .content(requestBody))                     // Request Body에 JSON 데이터 추가
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EMAIL_VERIFICATION_FAILED"))
                .andExpect(jsonPath("$.errorType").value("EmailVerificationFailedException"))
                .andExpect(jsonPath("$.message").value("인증시간이 만료되었습니다."));

        verify(authService, times(1)).validateVerificationCode(inputVerificationCode, savedVerificationCode, expiryTime);
    }
}