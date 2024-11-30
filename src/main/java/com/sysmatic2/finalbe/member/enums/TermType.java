package com.sysmatic2.finalbe.member.enums;

public enum TermType {

    PRIVACY_POLICY("개인정보처리방침"),
    SERVICE_TERMS("서비스이용약관"),
    PROMOTION("프로모션"),
    MARKETING_AGREEMENT("마케팅,광고성정보수신");

    private final String description;

    // 생성자
    TermType(String description) {
        this.description = description;
    }

    // 설명 반환 메서드
    public String getDescription() {
        return description;
    }

    // Enum 이름으로 Enum 반환
    public static TermType from(String name) {
        for (TermType type : TermType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid TermType: " + name);
    }
}