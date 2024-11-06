package com.sysmatic2.finalbe.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    // 현재 사용자의 ID나 이름을 반환.
    // Optional을 사용하여 null이 될 수 있음을 처리
    public Optional<String> getCurrentAuditor() {
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나 인증이 되어 있지 않으면 빈 Optional을 반환
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return Optional.empty();
//        }

        // 인증된 사용자의 이름 반환 (username 또는 ID)
//        return Optional.of(authentication.getName());

        // 임시
        return Optional.of("admin");
    }
}
