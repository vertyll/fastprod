package com.vertyll.fastprod.user.service.impl;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Join;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.sharedinfrastructure.exception.ApiException;
import com.vertyll.fastprod.user.api.UserAccounts;
import com.vertyll.fastprod.user.entity.Role;
import com.vertyll.fastprod.user.entity.User;
import com.vertyll.fastprod.user.identity.IdentityProvider;
import com.vertyll.fastprod.user.repository.UserRepository;
import com.vertyll.fastprod.user.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class UserAccountsImpl implements UserAccounts {

    private static final String USER_NOT_FOUND = "errors.user.notFound";
    private static final String EMAIL_ALREADY_EXISTS = "errors.user.emailTaken";

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final IdentityProvider identityProvider;

    @Override
    @Transactional
    public Account create(NewAccount account) {
        if (userRepository.existsByEmail(account.email())) {
            throw new ApiException(EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
        String keycloakId = identityProvider.createUser(
            new IdentityProvider.NewIdentity(account.email(), account.firstName(), account.lastName()),
            account.temporaryPassword(),
            account.roles()
        );
        User user = User.builder()
            .keycloakId(keycloakId)
            .firstName(account.firstName())
            .lastName(account.lastName())
            .email(account.email())
            .verified(false)
            .build();
        user.assignRoles(roleEntities(account.roles()));
        return toAccount(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> find(long id) {
        return userRepository.findById(id).map(UserAccountsImpl::toAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Account> findActive(AccountSearch search, Pageable pageable) {
        return userRepository.findAll(specification(search), pageable).map(UserAccountsImpl::toAccount);
    }

    @Override
    @Transactional
    public Account update(long id, AccountChanges changes) {
        User user = existing(id);
        if (!changes.email().equals(user.getEmail()) && userRepository.existsByEmail(changes.email())) {
            throw new ApiException(EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
        identityProvider.updateUser(
            user.getKeycloakId(),
            new IdentityProvider.NewIdentity(changes.email(), changes.firstName(), changes.lastName())
        );
        user.updateDetails(changes.firstName(), changes.lastName(), changes.email());

        String password = changes.temporaryPassword();
        if (password != null && !password.isBlank()) {
            identityProvider.resetPassword(user.getKeycloakId(), password);
        }
        Set<RoleType> roles = changes.roles();
        if (roles != null && !roles.isEmpty()) {
            identityProvider.replaceRoles(user.getKeycloakId(), roles);
            user.assignRoles(roleEntities(roles));
        }
        return toAccount(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deactivate(long id) {
        User user = existing(id);
        identityProvider.disable(user.getKeycloakId());
        user.deactivate();
        userRepository.save(user);
    }

    private User existing(long id) {
        return userRepository.findById(id).orElseThrow(() -> new ApiException(USER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Set<Role> roleEntities(Set<RoleType> roles) {
        return roles.stream().map(roleService::getOrCreateDefaultRole).collect(Collectors.toSet());
    }

    private static Account toAccount(User user) {
        return new Account(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
            user.isVerified(),
            user.isActive()
        );
    }

    private static Specification<User> specification(AccountSearch search) {
        Specification<User> spec = Specification.where((root, _, cb) -> cb.isTrue(root.get("active")));
        spec = containing(spec, "firstName", search.firstName());
        spec = containing(spec, "lastName", search.lastName());
        spec = containing(spec, "email", search.email());
        Boolean verified = search.verified();
        if (verified != null) {
            spec = spec.and((root, _, cb) -> cb.equal(root.get("verified"), verified));
        }
        if (!search.roleNames().isEmpty()) {
            Set<String> roleNames = search.roleNames().stream()
                .map(name -> name.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
            spec = spec.and((root, _, cb) -> {
                Join<User, Role> rolesJoin = root.join("roles");
                return cb.upper(rolesJoin.get("name")).in(roleNames);
            });
        }
        String text = search.text();
        if (text != null && !text.isBlank()) {
            String likePattern = likePattern(text);
            spec = spec.and((root, _, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), likePattern),
                cb.like(cb.lower(root.get("lastName")), likePattern),
                cb.like(cb.lower(root.get("email")), likePattern)
            ));
        }
        return spec;
    }

    private static Specification<User> containing(Specification<User> spec, String field, @Nullable String value) {
        if (value == null || value.isBlank()) {
            return spec;
        }
        String likePattern = likePattern(value);
        return spec.and((root, _, cb) -> cb.like(cb.lower(root.get(field)), likePattern));
    }

    private static String likePattern(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
