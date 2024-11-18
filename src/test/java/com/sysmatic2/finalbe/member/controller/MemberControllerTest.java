package com.sysmatic2.finalbe.member.controller;

import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
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
                .andExpect(status().isBadRequest()) // 400 Bad Request
                .andExpect(jsonPath("$.error").value("Member_Already_Exists"))
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
                .andExpect(jsonPath("$.error").value("Constraint Violation"))
                .andExpect(jsonPath("$.message").value("유효성 검사가 실패했습니다: checkNickname.nickname: 닉네임은 2~10자 이내의 문자여야 합니다."));
    }
}
