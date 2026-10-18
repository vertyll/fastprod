package com.vertyll.fastprod.shared.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.savedrequest.SavedRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnPageTest {
    private final ReturnPage requests = new ReturnPage();

    @Test
    void aButtonsSignInReturnsOnceToTheViewItWasStartedOn() {
        MockHttpSession session = new MockHttpSession();

        ReturnPage.remember(signIn(session, "/orders?status=open"));

        SavedRequest saved = requests.getRequest(request(session), new MockHttpServletResponse());
        assertThat(saved).isNotNull();
        assertThat(saved.getRedirectUrl()).isEqualTo("/orders?status=open");
        assertThat(requests.getRequest(request(session), new MockHttpServletResponse())).isNull();
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://evil.test/",
            "//evil.test/",
            "/\\evil.test",
            "orders",
            "/a\nb",
            ""
        }
    )
    void aViewElsewhereIsNotKept(String page) {
        MockHttpSession session = new MockHttpSession();

        ReturnPage.remember(signIn(session, page));

        assertThat(requests.getRequest(request(session), new MockHttpServletResponse())).isNull();
    }

    private static MockHttpServletRequest signIn(MockHttpSession session, String page) {
        MockHttpServletRequest request = request(session);
        request.setParameter(ReturnPage.PARAMETER, page);
        return request;
    }

    private static MockHttpServletRequest request(MockHttpSession session) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        return request;
    }
}
