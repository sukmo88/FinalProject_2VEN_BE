package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.attachment.service.ProfileService;
import com.sysmatic2.finalbe.exception.ConfirmPasswordMismatchException;
import com.sysmatic2.finalbe.exception.InvalidPasswordException;
import com.sysmatic2.finalbe.exception.MemberAlreadyExistsException;
import com.sysmatic2.finalbe.exception.MemberNotFoundException;
import com.sysmatic2.finalbe.member.dto.*;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
import com.sysmatic2.finalbe.common.DtoEntityConversion;
import com.sysmatic2.finalbe.util.RandomKeyGenerator;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sysmatic2.finalbe.util.CreatePageResponse.createPageResponse;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;
    private final StrategyRepository strategyRepository;

    @Transactional
    public void signup(SignupDTO signupDTO) {

        // nickname 중복 여부 & 비밀번호 동열 여부 확인
        duplicateNicknameCheck(signupDTO.getNickname());
        comparePassword(signupDTO.getPassword(), signupDTO.getConfirmPassword());

        MemberEntity member = DtoEntityConversion.convertToMemberEntity(signupDTO, passwordEncoder);

        String uuid = RandomKeyGenerator.createUUID();
        member.setMemberId(uuid);

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
    public ResponseEntity<Map<String,Object>> login(String email, String password, HttpSession session) {
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
        String role = member.getMemberGradeCode().replace("MEMBER_", "");
        data.put("memberId",member.getMemberId());
        data.put("email", member.getEmail());
        data.put("nickname", member.getNickname());
        data.put("role",role);
        data.put("profilePath", member.getProfilePath());
        if(role.equals("ROLE_ADMIN")){
            AdminSessionDTO adminSessionDTO = new AdminSessionDTO();
            adminSessionDTO.setAuthorized(false);
            adminSessionDTO.setAuthorizationStatus("PENDING");
            adminSessionDTO.setAuthorizedAt("");
            adminSessionDTO.setExpiresAt("");
            data.put("adminInfo",adminSessionDTO);
            session.setAttribute("adminInfo",adminSessionDTO);
        }
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

    public void checkExistEmail(String email) {
        if (memberRepository.findByEmail(email).isEmpty()) {
            throw new MemberNotFoundException("해당 이메일로 등록된 계정을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public void resetPassword(String email, PasswordResetDTO passwordResetDTO) {
        // 1. member 조회한 후 없으면 예외 발생
        MemberEntity member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);

        // 2. 입력한 두 비밀번호 일치 여부 확인
        comparePassword(passwordResetDTO.getNewPassword(), passwordResetDTO.getConfirmPassword());

        // 3. 비밀번호 암호화 후 member 수정해서 저장
        member.setPassword(passwordEncoder.encode(passwordResetDTO.getNewPassword()));
        memberRepository.save(member);
    }

    //keyword로 트레이더명 검색, 트레이더명, 트레이더 소개, 소유 전략 갯수 반환
    @Transactional(readOnly = true)
    public Map<String, Object> getTraderListByKeyword(String keyword, Integer page, Integer pageSize){
        //페이지 객체 생성
        Pageable pageable = PageRequest.of(page, pageSize);

        //트레이더 닉네임에 키워드를 포함한 해당 전략 엔티티 객체들 가져오기
        Page<MemberEntity> findTraderPage = memberRepository.searchByKeyword(keyword, pageable);

        //트레이더 엔티티에서 트레이더 id 가져와서 리스트로 만들기
        List<String> traderIds = findTraderPage.stream()
                .map(MemberEntity::getMemberId)
                .collect(Collectors.toList());

        // 트레이더 id : 전략 갯수 map
        Map<String, Integer> strategyCountMap = new HashMap<>();

        //트레이더 id로 전략 갯수 가져오기 - 승인 받은 전략 갯수만 출력함
        for(String traderId : traderIds){
            Integer strategyCnt = strategyRepository.countByWriterIdAndIsApproved(traderId, "Y");
            strategyCountMap.put(traderId, strategyCnt);
        }

        //DTO에 정보 넣기
        List<TraderSearchResultDto> dtoList = findTraderPage.stream()
                .map(memberEntity -> {
                    TraderSearchResultDto dto = new TraderSearchResultDto(
                            memberEntity.getMemberId(),      //트레이더 id
                            memberEntity.getNickname(),      //트레이더 닉네임
                            memberEntity.getIntroduction(),  //트레이더 소개글
                            memberEntity.getFileId(),        //트레이더 프로필 이미지 id
                            memberEntity.getProfilePath(),   //트레이더 프로필 이미지 링크
                            0                                //전략 수 0 설정
                    );
                    Integer strategyCnt = strategyCountMap.get(memberEntity.getMemberId());
                    dto.setStrategyCnt(strategyCnt);

                    return dto;

                }).collect(Collectors.toList());

        Page<TraderSearchResultDto> dtoPage = new PageImpl<>(dtoList, pageable, findTraderPage.getTotalElements());

        return createPageResponse(dtoPage);
    }




}
