package com.sysmatic2.finalbe.member.service;

import com.sysmatic2.finalbe.exception.*;
import com.sysmatic2.finalbe.member.dto.*;
import com.sysmatic2.finalbe.member.entity.MemberEntity;
import com.sysmatic2.finalbe.member.entity.MemberTermEntity;
import com.sysmatic2.finalbe.member.enums.TermType;
import com.sysmatic2.finalbe.member.repository.MemberRepository;
import com.sysmatic2.finalbe.strategy.repository.StrategyRepository;
<<<<<<< HEAD
import com.sysmatic2.finalbe.common.DtoEntityConversion;
import com.sysmatic2.finalbe.util.RandomKeyGenerator;
=======
import com.sysmatic2.finalbe.util.DtoEntityConversionUtils;
>>>>>>> c83adeb09a578587542474c0929e319c61530863
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

import java.time.LocalDateTime;
import java.util.*;
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
    private final StrategyRepository strategyRepository;

    @Transactional
    public void signup(SignupDTO signupDTO) {

        // nickname 중복 여부 & 비밀번호 동열 여부 확인
        duplicateNicknameCheck(signupDTO.getNickname());
        comparePassword(signupDTO.getPassword(), signupDTO.getConfirmPassword());


        // SignupDTO를 MemberEntity로 변환 후 저장
        MemberEntity member = DtoEntityConversion.convertToMemberEntity(signupDTO, passwordEncoder);

        // 필수약관 동의여부 확인 후 약관 및 광고성정보수신 동의내역 저장
        if (!signupDTO.getPrivacyRequired() || !signupDTO.getServiceTermsRequired()) {
            throw new RequiredAgreementException("개인정보처리방침과 서비스이용약관은 필수 동의 항목입니다.");
        }

        // 약관동의내역 저장
        LocalDateTime decisionDate = member.getSignupAt();
        String memberId = member.getMemberId();

        Iterator<Map.Entry<TermType, String>> iterator = termToMap(signupDTO).entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<TermType, String> entry = iterator.next();
            TermType termType = entry.getKey();
            String isAgreed = entry.getValue();

            MemberTermEntity memberTerm = new MemberTermEntity();
            memberTerm.setTermType(termType.name());
            memberTerm.setMember(member);
            memberTerm.setIsTermAgreed(isAgreed);
            memberTerm.setDecisionDate(decisionDate);
            memberTerm.setCreatedBy(memberId);
            memberTerm.setModifiedBy(memberId);
            member.getMemberTermList().add(memberTerm);
        }

        // TODO) 관심전략 기본폴더 생성

        memberRepository.save(member);
    }

    // 확인 비밀번호 값이 일치하는지 확인하는 메소드
    private void comparePassword(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new InvalidPasswordException("확인 비밀번호가 일치하지 않습니다.");
        }
    }

    // 약관동의내역을 map으로 변환하는 메소드
    private HashMap<TermType, String> termToMap(SignupDTO signupDTO) {
        HashMap<TermType, String> map = new HashMap<>();
        map.put(TermType.PRIVACY_POLICY, signupDTO.getPrivacyRequired() ? "Y" : "N");
        map.put(TermType.SERVICE_TERMS, signupDTO.getServiceTermsRequired() ? "Y" : "N");
        map.put(TermType.PROMOTION, signupDTO.getPromotionOptional() ? "Y" : "N");
        map.put(TermType.MARKETING_AGREEMENT, signupDTO.getMarketingOptional() ? "Y" : "N");
        return map;
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

        // 닉네임 변경 시 중복체크 후 변경
        String updateNickname = profileUpdateDTO.getNickname();
        if (!member.getNickname().equals(updateNickname)) {  // 현재 닉네임과 새로운 닉네임이 다르면 중복 검사 진행
            duplicateNicknameCheck(updateNickname);
            member.setNickname(updateNickname);
        }

        // 조회한 회원에 수정할 값 입력 후 저장
        member.setPhoneNumber(profileUpdateDTO.getPhoneNumber());
        member.setIntroduction(profileUpdateDTO.getIntroduction());

        // 약관 동의 내역 수정 : 변경여부 확인하여 수정
        String isMarketingAgreed = profileUpdateDTO.getMarketingOptional() ? "Y" : "N";

        // memberTermList에서 termType이 MARKETING_OPTIONAL인 memberTerm 찾기
        MemberTermEntity marketingTerm = member.getMemberTermList().stream()
                .filter(memberTerm -> TermType.MARKETING_AGREEMENT.name().equals(memberTerm.getTermType()))
                .findFirst()
                .orElseThrow(() -> new MemberTermNotFoundException("약관 동의 내역이 없습니다."));

        // 동의여부 변경 여부 확인 후 업데이트
        if (!isMarketingAgreed.equals(marketingTerm.getIsTermAgreed())) {
            marketingTerm.setIsTermAgreed(isMarketingAgreed);
            marketingTerm.setDecisionDate(LocalDateTime.now());
        }

        // 멤버 엔티티 저장
        memberRepository.save(member);
    }

    public void checkExistEmail(String email) {
        if (memberRepository.findByEmail(email).isEmpty()) {
            throw new MemberNotFoundException("해당 이메일로 등록된 계정을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public void changePassword(String memberId, PasswordUpdateDTO pwdUpDTO) {
        // 1. member 조회한 후 없으면 예외 발생
        MemberEntity member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        // 2. 입력한 oldPassword가 기존 비밀번호와 일치하는지 확인 후, 불일치 시 예외 발생
        if (!passwordEncoder.matches(pwdUpDTO.getOldPassword(), member.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 틀렸습니다.");
        }

        // 3. 새로운 비밀번호가 기존 비밀번호와 다른지 확인, 같으면 변경 실패
        if (passwordEncoder.matches(pwdUpDTO.getNewPassword(), member.getPassword())) {
            throw new InvalidPasswordException("새 비밀번호는 기존 비밀번호와 다르게 설정해야 합니다.");
        }

        // 4. 비밀번호 변경
        updatePassword(pwdUpDTO.getNewPassword(), pwdUpDTO.getConfirmPassword(), member);
    }

    @Transactional
    public void resetPassword(String email, PasswordResetDTO pwdResetDTO) {
        // 1. member 조회한 후 없으면 예외 발생
        MemberEntity member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        // 2. 비밀번호 변경
        updatePassword(pwdResetDTO.getNewPassword(), pwdResetDTO.getConfirmPassword(), member);
    }

    private void updatePassword(String newPwd, String confirmPwd, MemberEntity member) {
        // 입력한 두 비밀번호 일치 여부 확인
        comparePassword(newPwd, confirmPwd);

        // 비밀번호 암호화 후 member 수정해서 저장
        member.setPassword(passwordEncoder.encode(newPwd));
        member.setPasswordChangedAt(LocalDateTime.now());
        memberRepository.save(member);
    }


    @Transactional(readOnly = true)
    public List<EmailResponseDTO> findEmail(String phoneNumber) {
        List<EmailResponseDTO> emailByPhoneNumber = memberRepository.findEmailByPhoneNumber(phoneNumber);
        if (emailByPhoneNumber.isEmpty()) {
            throw new MemberNotFoundException();
        }

        return emailByPhoneNumber;
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
