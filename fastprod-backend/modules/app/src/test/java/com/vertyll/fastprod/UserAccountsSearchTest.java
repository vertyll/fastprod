package com.vertyll.fastprod;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.vertyll.fastprod.sharedinfrastructure.enums.RoleType;
import com.vertyll.fastprod.user.api.UserAccounts;
import com.vertyll.fastprod.user.api.UserAccounts.Account;
import com.vertyll.fastprod.user.api.UserAccounts.AccountSearch;
import com.vertyll.fastprod.user.identity.IdentityProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** The account search against PostgreSQL, with Keycloak's Admin API stood in for. */
@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestContainer.class)
@Transactional
class UserAccountsSearchTest {

    private static final AtomicInteger KEYCLOAK_IDS = new AtomicInteger();

    @Autowired
    UserAccounts accounts;

    @MockitoBean
    IdentityProvider identityProvider;

    @BeforeEach
    void someAccounts() {
        when(identityProvider.createUser(any(), anyString(), any()))
            .thenAnswer(_ -> "kc-" + KEYCLOAK_IDS.incrementAndGet());
        accounts.create(new UserAccounts.NewAccount("Anna", "Nowak", "anna@search.test", "x", Set.of(RoleType.ADMIN)));
        accounts
            .create(new UserAccounts.NewAccount("Jan", "Kowalski", "jan@search.test", "x", Set.of(RoleType.EMPLOYEE)));
        Account gone = accounts
            .create(new UserAccounts.NewAccount("Ewa", "Nowak", "ewa@search.test", "x", Set.of(RoleType.EMPLOYEE)));
        accounts.deactivate(gone.id());
    }

    @Test
    void everyCriterionLeftOutMatchesEveryActiveAccount() {
        assertThat(emails(new AccountSearch(null, null, null, null, Set.of(), null)))
            .containsExactly("anna@search.test", "jan@search.test");
    }

    @Test
    void aNameMatchesAnyPartIgnoringCase() {
        assertThat(emails(new AccountSearch(null, "now", null, null, Set.of(), null)))
            .containsExactly("anna@search.test");
        assertThat(emails(new AccountSearch("JA", null, null, null, Set.of(), null)))
            .containsExactly("jan@search.test");
        assertThat(emails(new AccountSearch(null, null, "ANNA@", null, Set.of(), null)))
            .containsExactly("anna@search.test");
    }

    @Test
    void theTextMatchesTheNameOrTheEmail() {
        assertThat(emails(new AccountSearch(null, null, null, null, Set.of(), "kowal")))
            .containsExactly("jan@search.test");
        assertThat(emails(new AccountSearch(null, null, null, null, Set.of(), "search.test")))
            .containsExactly("anna@search.test", "jan@search.test");
    }

    @Test
    void rolesAndVerificationNarrowTheSearch() {
        assertThat(emails(new AccountSearch(null, null, null, null, Set.of("admin"), null)))
            .containsExactly("anna@search.test");
        assertThat(emails(new AccountSearch(null, null, null, true, Set.of(), null))).isEmpty();
        assertThat(emails(new AccountSearch(null, null, null, false, Set.of("employee"), null)))
            .containsExactly("jan@search.test");
    }

    private List<String> emails(AccountSearch search) {
        return accounts.findActive(search, PageRequest.of(0, 10, Sort.by("email")))
            .map(Account::email)
            .filter(email -> email.endsWith("@search.test"))
            .toList();
    }
}
