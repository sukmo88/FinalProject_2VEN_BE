package com.sysmatic2.finalbe.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/members")
public class MemberController {
    //로그인
    @PostMapping("/login")
    public String login(HttpServletRequest request) {
        return "login";
    }
    //로그아웃
    @PostMapping("/logout")
    public String logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.invalidate();

        return "logout";
    }
    //이메일찾기
    @GetMapping("/check-phone")
    public String checkPhone(HttpServletRequest request) {
        return "check-phone";
    }
    //이메일 체크(중복체크) 확인해봐야함
    @GetMapping("/check-email")
    public String checkEmail(HttpServletRequest request) {
        return "check-email";
    }
    //비밀번호 재설정
    @PostMapping("/password")
    public String password(HttpServletRequest request) {
        return "password";
    }
    //회원가입
    @PostMapping("/signup")
    public String signup(HttpServletRequest request) {
        return "signup";
    }
    //닉네임 중복 체크
    @GetMapping("/check-nickname")
    public String checkNickname(HttpServletRequest request) {
        return "check-nickname";
    }
    //회원탈퇴
    @DeleteMapping("/withdrawal")
    public String delete(HttpServletRequest request) {
        return "withdrawal";
    }

    //개인정보상세조회
    @GetMapping("/details")
    public String details(HttpServletRequest request) {
        return "details";
    }
    //회원정보수정
    @PutMapping("/details")
    public String updateDetails(HttpServletRequest request) {
        return "updateDetails";
    }
    //비밀번호 확인
    @PostMapping("/check-password")
    public String checkPassword(HttpServletRequest request) {
        return "check-password";
    }
    //비밀번호 변경
    @PatchMapping("/change-password")
    public String changePassword(HttpServletRequest request) {
        return "change-password";
    }
}
