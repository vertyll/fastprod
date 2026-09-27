package com.vertyll.fastprod.bootstrap;

import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.fastprod.role.service.RoleService;
import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@EnableConfigurationProperties(
    {
        DataSeeder.AdminProps.class,
        DataSeeder.SeedProps.class
    }
)
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
    public void run(ApplicationArguments args) {
        if (!seedProps.enabled()) {
            log.info("[DataSeeder] Seeding is disabled (app.seed.enabled=false)");
            return;
        }
        seedAdminUser();
    }

    private void seedAdminUser() {
        String email = adminProps.email().isBlank() ? DEFAULT_ADMIN_EMAIL : adminProps.email();

        if (userService.existsByEmail(email)) {
            log.info("[DataSeeder] Admin user already exists: {}", email);
            return;
        }

        String password = adminProps.password();
        if (password.isBlank()) {
            log.warn(
                "[DataSeeder] Admin password not provided. Skipping admin creation. Set ADMIN_PASSWORD or admin.password to enable."
            );
            return;
        }

        User admin = User.builder()
            .firstName(adminProps.firstName().isBlank() ? DEFAULT_ADMIN_FIRST_NAME : adminProps.firstName())
            .lastName(adminProps.lastName().isBlank() ? DEFAULT_ADMIN_LAST_NAME : adminProps.lastName())
            .email(email)
            .password(requireNonNull(passwordEncoder.encode(password)))
            .active(true)
            .verified(true)
            .build();

        admin.assignRoles(Set.of(roleService.getOrCreateDefaultRole(RoleType.ADMIN)));

        userService.saveUser(admin);
        log.info("[DataSeeder] Admin user created: {}", email);
    }

    @ConfigurationProperties(prefix = "admin")
    public record AdminProps(String email, String password, String firstName, String lastName) {
    }

    @ConfigurationProperties(prefix = "app.seed")
    public record SeedProps(@DefaultValue("true") boolean enabled) {
    }
}
