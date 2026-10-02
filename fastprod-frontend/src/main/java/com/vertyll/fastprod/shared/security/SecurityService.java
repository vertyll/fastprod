package com.vertyll.fastprod.shared.security;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.vaadin.flow.spring.security.AuthenticationContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AuthenticationContext authenticationContext;

    public void logout() {
        authenticationContext.logout();
    }

    public boolean isAuthenticated() {
        return authenticationContext.isAuthenticated();
    }

    public boolean hasRole(RoleType role) {
        return authenticationContext.hasRole(role.name());
    }

    public boolean hasAnyRole(RoleType... roles) {
        return authenticationContext.hasAnyRole(Arrays.stream(roles).map(RoleType::name).toList());
    }
}
