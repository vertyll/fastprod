package com.vertyll.fastprod.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ApplicationAuditAware implements AuditorAware<String> {

    private static final String SYSTEM_ACCOUNT = "SYSTEM";

    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of(SYSTEM_ACCOUNT);
        }

        return Optional.ofNullable(authentication.getName()).filter(name -> !name.isBlank()).or(() -> Optional.of(SYSTEM_ACCOUNT));
    }
}
