package com.sysmatic2.finalbe.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
import com.sysmatic2.finalbe.exception.MemberNotFoundException;
import com.sysmatic2.finalbe.member.dto.*;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.service.EmailService;
import com.sysmatic2.finalbe.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    // 인증토큰 생성 후 SecurityContext에 인증 정보 설정
    private UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(String memberId) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setMemberId(memberId);
        memberEntity.setEmail("valid@email.com");
        memberEntity.setMemberGradeCode("MEMBER_ROLE_TRADER");
        CustomUserDetails customUserDetails = new CustomUserDetails(Optional.of(memberEntity));

        // SecurityContext에 인증 정보 설정
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        return authenticationToken;
    }

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
    @Test
    @WithMockUser
    @DisplayName("이메일 검사 - 성공")
    public void testCheckEmail_Success() throws Exception {
        // 유효한 이메일로 인증 시도
        String validEmail = "valid@email.com";
        String verificationCode = "123456";

        EmailCheckDTO emailCheckDTO = new EmailCheckDTO();
        emailCheckDTO.setEmail(validEmail);

        doNothing().when(memberService).duplicateEmailCheck(validEmail);
        doNothing().when(emailService).sendVerificationMail(validEmail, verificationCode);

        // ObjectMapper를 사용하여 DTO를 JSON 문자열로 반환
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(emailCheckDTO);

        // API 호출 및 검증
        mockMvc.perform(post("/api/members/check-email")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("인증 번호가 이메일로 전송되었습니다."));
    }

    @Test
    @DisplayName("이메일 검사 - 이메일 형식이 유효하지 않으면 예외 발생")
    public void testCheckEmail_Failure() throws Exception {
    }

    // 사이드바 프로필 조회 메소드 테스트 - 성공
    @WithMockUser // 인증된 사용자를 모의(Mock)
    @Test
    @DisplayName("사이드바 프로필 조회 성공")
    public void testGetSidebarProfile_Success() throws Exception {

        String memberId = "validMemberId";
        String nickname = "nickname";
        String memberType = "MEMBER_ROLE_TRADER";
        String introduction = "introduction";
        String fileId = "fileId";
        String profilePath = "profilePath";

        SimpleProfileDTO simpleProfileDTO = new SimpleProfileDTO(nickname, memberType, introduction, fileId, profilePath);

        // 저장된 MemberId로 sidebar profile 조회 시 SimpleProfileDTO 반환하도록 설정
        doReturn(simpleProfileDTO).when(memberService).getSimpleProfile(memberId);

        // Controller 호출하여 성공 데이터 반환하는지 확인
        mockMvc.perform(get("/api/members/{memberId}/sidebar-profile", memberId)) // URL에 memberId 전달
                .andExpect(status().isOk()) // HTTP 200 상태 코드 확인
                .andExpect(jsonPath("$.status").value("success")) // 성공 상태 확인
                .andExpect(jsonPath("$.message").value("사이드바 프로필 조회에 성공하였습니다.")) // 메시지 확인
                .andExpect(jsonPath("$.data.nickname").value(nickname)) // nickname 확인
                .andExpect(jsonPath("$.data.memberType").value(memberType.replace("MEMBER_ROLE_", ""))) // memberType 확인
                .andExpect(jsonPath("$.data.introduction").value(introduction)) // introduction 확인
                .andExpect(jsonPath("$.data.fileId").value(fileId)); // fileId 확인

        // Mock 객체의 메소드 호출 검증
        verify(memberService, times(1)).getSimpleProfile(memberId);
    }

    // 사이드바 프로필 조회 메소드 테스트 - 실패 : 존재하지 않는 memberId로 조회 시도
    @WithMockUser(username = "testUser", roles = "USER")  // 인증된 사용자를 모의(Mock)
    @Test
    @DisplayName("사이드바 프로필 조회 실패")
    public void testGetSidebarProfile_Failure() throws Exception {
        String invalidMemberId = "invalidMemberId";

        // 없는 memberId로 sidebar profile 조회 시 예외 발생
        doThrow(MemberNotFoundException.class).when(memberService).getSimpleProfile(invalidMemberId);

        mockMvc.perform(get("/api/members/{memberId}/sidebar-profile", invalidMemberId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorType").value("MemberNotFoundException"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당되는 데이터를 찾을 수 없습니다."));
    }

    // 상세 개인정보 조회 테스트 - 성공
    @Test
    @DisplayName("상세 개인정보 조회 테스트")
    public void testGetDetails_Success() throws Exception {

        // Mocked UserDetails 생성
        String memberId = "validMemberId";
        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(memberId);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 존재하는 memberId로 상세 개인정보 조회 시 DetailedProfileDTO 반환하도록 설정
        DetailedProfileDTO detailedProfileDTO = new DetailedProfileDTO();
        detailedProfileDTO.setNickname("nickname");
        detailedProfileDTO.setEmail("valid@email.com");
        detailedProfileDTO.setIntroduction("introduction");
        detailedProfileDTO.setFileId("fileId");
        detailedProfileDTO.setMarketingOptional(true);
        detailedProfileDTO.setPhoneNumber("01012345678");

        doReturn(detailedProfileDTO).when(memberService).getDetailedProfile(memberId);

        // Controller 호출하여 성공 응답 반환하는지 확인
        mockMvc.perform(get("/api/members/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("상세 개인정보 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.nickname").value(detailedProfileDTO.getNickname()))
                .andExpect(jsonPath("$.data.email").value(detailedProfileDTO.getEmail()))
                .andExpect(jsonPath("$.data.introduction").value(detailedProfileDTO.getIntroduction()))
                .andExpect(jsonPath("$.data.fileId").value(detailedProfileDTO.getFileId()))
                .andExpect(jsonPath("$.data.fileId").value(detailedProfileDTO.getFileId()))
                .andExpect(jsonPath("$.data.marketingOptional").value(detailedProfileDTO.isMarketingOptional()))
                .andExpect(jsonPath("$.data.phoneNumber").value(detailedProfileDTO.getPhoneNumber()));
    }

    // 상세 개인정보 조회 테스트 - 실패 : 존재하지 않는 memberId로 조회 시도
    @WithMockUser
    @Test
    @DisplayName("상세 개인정보 조회 실패 테스트")
    public void testGetDetails_Failure() throws Exception {

        // Mocked UserDetails 생성
        String invalidMemberId = "invalidMemberId";
        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(invalidMemberId);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 없는 memberId로 sidebar profile 조회 시 예외 발생
        doThrow(MemberNotFoundException.class).when(memberService).getDetailedProfile(invalidMemberId);

        mockMvc.perform(get("/api/members/details", invalidMemberId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorType").value("MemberNotFoundException"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당되는 데이터를 찾을 수 없습니다."));
    }

    // 상세 개인정보 수정 테스트 - 실패
    @Test
    @DisplayName("상세 개인정보 수정 실패 테스트")
    public void testModifyDetails_Failure() throws Exception {

        // Mocked UserDetails 생성
        String invalidMemberId = "invalidMemberId";
        UsernamePasswordAuthenticationToken authenticationToken = getUsernamePasswordAuthenticationToken(invalidMemberId);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 개인정보 수정을 위한 ProfileUpdateDTO 생성
        ProfileUpdateDTO profileUpdateDTO = new ProfileUpdateDTO();
        profileUpdateDTO.setNickname("nickname");
        profileUpdateDTO.setPhoneNumber("01012345678");
        profileUpdateDTO.setIntroduction("introduction");
        profileUpdateDTO.setMarketingOptional(true);

        // ObjectMapper를 사용하여 DTO를 JSON 문자열로 반환
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(profileUpdateDTO);

        // 없는 memberId로 sidebar profile 조회 시 예외 발생
        // eq() 메서드를 사용하여 동일한 내용의 객체를 매칭하도록 설정하거나, any(ProfileUpdateDTO.class)와 같이 클래스 타입으로 매칭
        doThrow(MemberNotFoundException.class).when(memberService).modifyDetails(eq(invalidMemberId), any(ProfileUpdateDTO.class));

        mockMvc.perform(put("/api/members/details")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(authentication(authenticationToken))) // 인증 정보 추가
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorType").value("MemberNotFoundException"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당되는 데이터를 찾을 수 없습니다."));

        verify(memberService, times(1)).modifyDetails(eq(invalidMemberId), any(ProfileUpdateDTO.class));
    }
}
