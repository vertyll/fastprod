package com.vertyll.fastprod.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vertyll.fastprod.security.config.ApplicationAuditAware;

class ApplicationAuditAwareTest {

    private final ApplicationAuditAware auditAware = new ApplicationAuditAware();

    @Test
    void getCurrentAuditor_WhenAuthenticated_ShouldReturnUsername() {
        // given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testUser");

        // when
        Optional<String> result = auditAware.getCurrentAuditor();

        // then
        assertTrue(result.isPresent());
        assertEquals("testUser", result.get());
    }

    @Test
    void getCurrentAuditor_WhenNotAuthenticated_ShouldReturnSystem() {
        // given
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // when
        Optional<String> result = auditAware.getCurrentAuditor();

        // then
        assertTrue(result.isPresent());
        assertEquals("SYSTEM", result.get());
    }

    @Test
    void getCurrentAuditor_WhenNoAuthentication_ShouldReturnSystem() {
        // given
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // when
        Optional<String> result = auditAware.getCurrentAuditor();

        // then
        assertTrue(result.isPresent());
        assertEquals("SYSTEM", result.get());
    }
}
