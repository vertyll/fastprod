package com.vertyll.fastprod.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class ApplicationConfigTest {

    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final ApplicationConfig config = new ApplicationConfig(userDetailsService);

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // when
        PasswordEncoder encoder = config.passwordEncoder();

        // then
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void authenticationProvider_ShouldReturnDaoAuthenticationProvider() {
        // when
        var provider = config.authenticationProvider();

        // then
        assertNotNull(provider);
        assertInstanceOf(DaoAuthenticationProvider.class, provider);
    }

    @Test
    void authenticationProvider_ShouldUseCorrectUserDetailsService() {
        // when
        var provider = (DaoAuthenticationProvider) config.authenticationProvider();

        // then
        assertNotNull(provider);
        assertNotNull(provider);
    }

    @Test
    void authenticationManager_ShouldReturnManager() throws Exception {
        // given
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(authManager);

        // when
        AuthenticationManager result = config.authenticationManager(authConfig);

        // then
        assertNotNull(result);
        assertEquals(authManager, result);
    }
}
