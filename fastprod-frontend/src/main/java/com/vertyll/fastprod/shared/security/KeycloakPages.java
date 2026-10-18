package com.vertyll.fastprod.shared.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.vaadin.flow.component.UI;

public final class KeycloakPages {

    private KeycloakPages() {
    }

    /** Signs the person in, and brings them back to the view they are on. */
    public static void signIn() {
        UI ui = UI.getCurrent();
        ui.getPage().setLocation(SecurityConfig.LOGIN_PATH + "?" + returnTo(ui));
    }

    public static void signUp() {
        UI ui = UI.getCurrent();
        ui.getPage()
            .setLocation(
                SecurityConfig.LOGIN_PATH + "?" + LocalizedAuthorizationRequestResolver.REGISTER_PARAMETER + "&"
                        + returnTo(ui)
            );
    }

    /** The {@code return_to} parameter naming the view the UI shows. */
    static String returnTo(UI ui) {
        String view = "/" + ui.getActiveViewLocation().getPathWithQueryParameters();
        return ReturnPage.PARAMETER + "=" + URLEncoder.encode(view, StandardCharsets.UTF_8);
    }
}
