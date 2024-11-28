package com.sysmatic2.finalbe.member.entity;
import com.sysmatic2.finalbe.common.Auditable;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@ToString
public class MemberEntity extends Auditable {
    @Id
    @Column(name = "member_id")
    private String memberId;  // 회원 ID

    @Column(name = "member_grade_code", nullable = false)
    private String memberGradeCode;  // 회원등급코드

    @Column(name = "member_status_code", nullable = false)
    private String memberStatusCode;  // 회원상태코드

    @Column(name = "email", nullable = false, unique = true)
    private String email;  // 이메일

    @Column(name = "password", nullable = false)
    private String password;  // 암호화된 비밀번호

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;  // 닉네임

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;  // 휴대전화번호

    @Column(name = "introduction")
    private String introduction;  // 자기소개

    @Column(name = "login_fail_count", nullable = false)
    private Integer loginFailCount = 0;  // 로그인 실패 횟수 (기본값 0)

    @Column(name = "is_login_locked", nullable = false)
    private char isLoginLocked = 'N';  // 로그인잠금여부 (Y/N)

    @Column(name = "birth_date")
    private LocalDateTime birthDate;  // 생년월일

    @Column(name = "gender")
    private char gender;  // 성별 (F, M)

    @Column(name = "signup_at")
    private LocalDateTime signupAt;  // 가입 일자

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;  // 최근접속일자

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // 회원탈퇴일시

    @Column(name = "withdrawal_reason")
    private String withdrawalReason;  // 탈퇴사유

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;  // 비밀번호 변경일

    @Column(name = "file_id")
    private String fileId;  // 프로필 이미지 id

    @Column(name = "notes")
    private String notes;  // 비고 (탈퇴사유 등 참고사항)

    @OneToMany(mappedBy = "member")
    private List<MemberTermEntity> memberTermEntityList = new ArrayList<>();

}
