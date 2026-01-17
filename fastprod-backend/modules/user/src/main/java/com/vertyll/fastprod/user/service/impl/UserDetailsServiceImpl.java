package com.vertyll.fastprod.user.service.impl;

import com.vertyll.fastprod.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class UserDetailsServiceImpl implements UserDetailsService {

    private static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: ";

    private final UserRepository userRepository;

    @Override
    @NullUnmarked
    @SuppressWarnings("PMD.AvoidUncheckedExceptionsInSignatures") // Required by Spring Security interface
    public UserDetails loadUserByUsername(@Nullable String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmailWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_WITH_EMAIL + username));
    }
}
