package com.vertyll.fastprod.user.api;

import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;

/**
 * The accounts an administrator manages: each one is a Keycloak account and its record here, kept in step, so a
 * caller never reaches Keycloak or the account's storage itself.
 */
public interface UserAccounts {

    /**
     * Creates the account in Keycloak, with a temporary password, and here.
     *
     * @throws com.vertyll.fastprod.sharedinfrastructure.exception.ApiException {@code errors.user.emailTaken} when
     *     another account has the e-mail address
     */
    Account create(NewAccount account);

    /** The account with this id, active or not. */
    Optional<Account> find(long id);

    /** The active accounts the search matches, a page at a time. */
    Page<Account> findActive(AccountSearch search, Pageable pageable);

    /**
     * Changes an account's details, and its password and roles when the changes name them.
     *
     * @throws com.vertyll.fastprod.sharedinfrastructure.exception.ApiException {@code errors.user.notFound} when no
     *     account has the id, {@code errors.user.emailTaken} when another account has the new e-mail address
     */
    Account update(long id, AccountChanges changes);

    /**
     * Disables the account in Keycloak and marks it inactive here.
     *
     * @throws com.vertyll.fastprod.sharedinfrastructure.exception.ApiException {@code errors.user.notFound} when no
     *     account has the id
     */
    void deactivate(long id);

    record Account(
        long id,
        String firstName,
        String lastName,
        String email,
        Set<RoleType> roles,
        boolean verified,
        boolean active
    ) {
        public Account {
            roles = Set.copyOf(roles);
        }
    }

    record NewAccount(String firstName, String lastName, String email, String temporaryPassword, Set<RoleType> roles) {
        public NewAccount {
            roles = Set.copyOf(roles);
        }
    }

    /** New details; a {@code null} password or roles leaves them as they are. */
    record AccountChanges(
        String firstName,
        String lastName,
        String email,
        @Nullable String temporaryPassword,
        @Nullable Set<RoleType> roles
    ) {
        public AccountChanges {
            roles = roles == null ? null : Set.copyOf(roles);
        }
    }

    /** Each criterion that is {@code null}, or an empty set of role names, matches every account. */
    record AccountSearch(
        @Nullable String firstName,
        @Nullable String lastName,
        @Nullable String email,
        @Nullable Boolean verified,
        Set<String> roleNames,
        @Nullable String text
    ) {
        public AccountSearch {
            roleNames = Set.copyOf(roleNames);
        }
    }
}
