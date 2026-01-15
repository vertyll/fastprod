package com.vertyll.fastprod.bootstrap;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import com.vertyll.fastprod.role.enums.RoleType;
import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.service.UserService;

@Slf4j
@Component
@EnableConfigurationProperties(DataSeeder.AdminProps.class)
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleService roleService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AdminProps adminProps;
    private final SeedProps seedProps;

    private static final String DEFAULT_ADMIN_EMAIL = "admin@fastprod.local";
    private static final String DEFAULT_ADMIN_FIRST_NAME = "System";
    private static final String DEFAULT_ADMIN_LAST_NAME = "Administrator";

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (!seedProps.isEnabled()) {
            log.info("[DataSeeder] Seeding is disabled (app.seed.enabled=false)");
            return;
        }
        seedAdminUser();
    }

    private void seedAdminUser() {
        String email =
                (adminProps.email() == null || adminProps.email().isBlank())
                        ? DEFAULT_ADMIN_EMAIL
                        : adminProps.email();

        if (userService.existsByEmail(email)) {
            log.info("[DataSeeder] Admin user already exists: {}", email);
            return;
        }

        String password = adminProps.password();
        if (password == null || password.isBlank()) {
            log.warn(
                    "[DataSeeder] Admin password not provided. Skipping admin creation. Set ADMIN_PASSWORD or admin.password to enable.");
            return;
        }

        @SuppressWarnings("NullAway")
        String nonNullPassword = password;
        Set<String> adminRoleNames = Stream.of(RoleType.ADMIN.name()).collect(Collectors.toSet());

        User admin =
                User.builder()
                        .firstName(
                                (adminProps.firstName() == null || adminProps.firstName().isBlank())
                                        ? DEFAULT_ADMIN_FIRST_NAME
                                        : adminProps.firstName())
                        .lastName(
                                (adminProps.lastName() == null || adminProps.lastName().isBlank())
                                        ? DEFAULT_ADMIN_LAST_NAME
                                        : adminProps.lastName())
                        .email(email)
                        .password(Objects.requireNonNull(passwordEncoder.encode(nonNullPassword)))
                        .active(true)
                        .verified(true)
                        .build();

        admin.setRoles(
                adminRoleNames.stream()
                        .map(roleService::getOrCreateDefaultRole)
                        .collect(Collectors.toSet()));

        userService.saveUser(admin);
        log.info("[DataSeeder] Admin user created: {}", email);
    }

    @ConfigurationProperties(prefix = "admin")
    public record AdminProps(String email, String password, String firstName, String lastName) {}

    @Getter
    @Setter
    @Component
    @ConfigurationProperties(prefix = "app.seed")
    public static class SeedProps {
        private boolean enabled = true;
    }
}
