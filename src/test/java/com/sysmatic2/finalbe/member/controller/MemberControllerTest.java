package com.sysmatic2.finalbe.member.controller;

import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
import com.sysmatic2.finalbe.member.service.EmailService;
import com.sysmatic2.finalbe.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService; // Spring 컨텍스트에서 MockBean으로 관리

    @MockBean
    private EmailService emailService;

    // 닉네임 중복검사 테스트
    @Test
    @DisplayName("닉네임 검사 - 중복되지 않은 닉네임으로 검증 시도")
    @WithMockUser
    void testCheckNickname_Success() throws Exception {

        // 중복되지 않은 닉네임으로 검증 시도하면 아무일도 일어나지 않음
        String validNickname = "unique";
        doNothing().when(memberService).duplicateNicknameCheck(validNickname);

        // API 호출 및 검증
        mockMvc.perform(get("/api/members/check-nickname")
                        .param("nickname", validNickname)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("사용 가능한 닉네임입니다."));

        verify(memberService, times(1)).duplicateNicknameCheck(validNickname);
    }

    @Test
    @DisplayName("닉네임 검사 - 이미 존재하는 닉네임으로 검증 시도 시 예외 발생")
    @WithMockUser
    void testCheckNickname_Fail() throws Exception {

        // 이미 존재하는 닉네임으로 검증 시도
        String invalidNickname = "invalid";
        doThrow(new MemberAlreadyExistsException("이미 사용 중인 닉네임입니다."))
                .when(memberService).duplicateNicknameCheck(invalidNickname);

        // API 호출 및 검증
        mockMvc.perform(get("/api/members/check-nickname")
                        .param("nickname", invalidNickname)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()) // 409 Conflict
                .andExpect(jsonPath("$.errorType").value("MemberAlreadyExistsException"))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));

        verify(memberService, times(1)).duplicateNicknameCheck(invalidNickname);
    }

    @Test
    @DisplayName("닉네임 검사 - 형식이 유효하지 않은 닉네임으로 검증 시도")
    @WithMockUser
    void testCheckNickname_InvalidFormat() throws Exception {

        // 유효하지 않은 닉네임
        String invalidNickname = "!@#";

        // API 호출 및 검증
        mockMvc.perform(get("/api/members/check-nickname")
                        .param("nickname", invalidNickname)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()) // 형식 검증 실패: 400 Bad Request
                .andExpect(jsonPath("$.errorType").value("ConstraintViolationException"))
                .andExpect(jsonPath("$.errors['checkNickname.nickname']").value("닉네임은 2~10자 이내의 문자(한글, 영어, 숫자)여야 합니다."))
                .andExpect(jsonPath("$.message").value("유효성 검사에 실패했습니다."));
    }


    // 이메일 중복 검사 및 인증번호 이메일 발송 테스트
    @WithMockUser(username = "testUser", roles = "USER")  // 인증된 사용자를 모의(Mock)
    @Test
    public void testCheckEmail_Success() throws Exception {
        // 유효한 이메일로 인증 시도
        String validEmail = "valid@email.com";
        String verificationCode = "123456";

        doNothing().when(memberService).duplicateEmailCheck(validEmail);
        doNothing().when(emailService).sendVerificationMail(validEmail, verificationCode);

        // API 호출 및 검증
        mockMvc.perform(get("/api/members/check-email")
                    .param("email", validEmail)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("인증 번호가 이메일로 전송되었습니다."));
    }

    @Test
    @DisplayName("이메일 검사 - 이메일 형식이 유효하지 않으면 예외 발생")
    public void testCheckEmail_Failure() throws Exception {
    }


}
