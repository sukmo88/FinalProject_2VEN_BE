package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.attachment.service.ProfileService;
import com.sysmatic2.finalbe.exception.ConfirmPasswordMismatchException;
import com.sysmatic2.finalbe.exception.InvalidPasswordException;
import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
import com.sysmatic2.finalbe.exception.MemberNotFoundException;
import com.sysmatic2.finalbe.member.dto.*;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.util.DtoEntityConversionUtils;
import com.sysmatic2.finalbe.util.RandomKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;

    @Transactional
    public void signup(SignupDTO signupDTO) {

        // nickname 중복 여부 & 비밀번호 동열 여부 확인
        duplicateNicknameCheck(signupDTO.getNickname());
        comparePassword(signupDTO.getPassword(), signupDTO.getConfirmPassword());

        MemberEntity member = DtoEntityConversionUtils.convertToMemberEntity(signupDTO, passwordEncoder);

        String uuid = RandomKeyGenerator.createUUID();
        member.setMemberId(uuid);
        member.setFileId(profileService.createDefaultFileMetadataForMember(uuid));  // profileService 에서 fileId 받아와서 member에 등록

        memberRepository.save(member); // 가입 실패 시 예외 발생
    }

    private void comparePassword(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new ConfirmPasswordMismatchException("확인 비밀번호가 일치하지 않습니다.");
        }
    }

    // email 중복 여부 확인
    public void duplicateEmailCheck(String email) {
        // email로 디비에서 데이터 조회 -> 존재하면 중복 예외 발생 (탈퇴 시 개인정보 바로 삭제하므로 회원 상태 비교 불필요)
        if (!memberRepository.findByEmail(email).isEmpty()) {
            throw new MemberAlreadyExistsException("이미 사용 중인 이메일입니다.");
        }
    }

    // nickname 중복 여부 확인
    public void duplicateNicknameCheck(String nickname) {
        // email로 디비에서 데이터 조회 -> 존재하면 중복 예외 발생 (탈퇴 시 개인정보 바로 삭제하므로 회원 상태 비교 불필요)
        if (!memberRepository.findByNickname(nickname).isEmpty()) {
            throw new MemberAlreadyExistsException("이미 사용 중인 닉네임입니다.");
        }
    }

    //로그인 서비스
    public ResponseEntity<Map<String,Object>> login(String email, String password) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElse(null);
        Map<String,Object> response = new HashMap<>();

        if(member == null) {
            //이메일로 사용자를 찾지 못했을 경우(404)
            response.put("status","error");
            response.put("message","해당 계정이 존재하지 않습니다.");
            response.put("errorCode","MEMBER_NOT_FOUND");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if(member.getIsLoginLocked()==('Y')){
            // 계정이 잠금 상태일 경우 (401)
            response.put("status", "error");
            response.put("message", "5회이상 로그인 실패로 잠금처리되었습니다. 이메일인증을 통해 비밀번호를 재설정하세요.");
            response.put("errorCode", "LOGIN_ATTEMPTS_EXCEEDED");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {            // 비밀번호가 일치하지 않는 경우 (401 로그인 실패)
            response.put("status", "error");
            response.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            response.put("errorCode", "INVALID_PASSWORD");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        // 로그인 성공 (200)
        response.put("status", "success");
        response.put("message", "로그인에 성공했습니다.");
        Map<String, Object> data = new HashMap<>();
        data.put("member_id",member.getMemberId());
        data.put("email", member.getEmail());
        data.put("nickname", member.getNickname());
        data.put("role", member.getMemberGradeCode());
        //jwt 값을 전달해줘야지 정상적으로 로그인 했으면
        response.put("data", data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public SimpleProfileDTO getSimpleProfile(String memberId) {
        return memberRepository.findSimpleProfileByMemberId(memberId).orElseThrow(MemberNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public DetailedProfileDTO getDetailedProfile(String memberId) {
        return memberRepository.findDetailedProfileByMemberId(memberId).orElseThrow(MemberNotFoundException::new);
    }

    @Transactional
    public void modifyDetails(String memberId, ProfileUpdateDTO profileUpdateDTO) {
        // memberId로 회원 조회 -> 없으면 예외 발생
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        duplicateNicknameCheck(profileUpdateDTO.getNickname()); // 닉네임 중복 체크

        // 조회한 회원에 수정할 값 입력 후 저장
        member.setNickname(profileUpdateDTO.getNickname());
        member.setPhoneNumber(profileUpdateDTO.getPhoneNumber());
        member.setIntroduction(profileUpdateDTO.getIntroduction());
        member.setIsAgreedMarketingAd(profileUpdateDTO.getMarketingOptional() ? 'Y' : 'N');
        memberRepository.save(member);
    }

    @Transactional
    public void changePassword(String memberId, PasswordUpdateDTO passwordUpdateDTO) {
        // member 조회한 후 없으면 예외 발생
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        // 입력한 oldPassword가 기존 비밀번호와 일치하는지 확인 -> 불일치 시 예외 발생
        if (!passwordEncoder.matches(passwordUpdateDTO.getOldPassword(), member.getPassword())) {
            throw new InvalidPasswordException();
        }

        // 바꿀 비밀번호와 확인 비밀번호 일치하는지 확인 후 불일치 시 예외 발생
        comparePassword(passwordUpdateDTO.getNewPassword(), passwordUpdateDTO.getConfirmPassword());

        // 새로운 비밀번호 암호화 후 설정 및 저장
        String encodedPwd = passwordEncoder.encode(passwordUpdateDTO.getNewPassword());
        member.setPassword(encodedPwd);
        memberRepository.save(member);
    }

}
