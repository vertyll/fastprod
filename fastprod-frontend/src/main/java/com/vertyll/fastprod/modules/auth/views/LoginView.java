package com.vertyll.fastprod.modules.auth.views;

import java.io.Serial;

import com.vertyll.fastprod.modules.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.modules.auth.dto.LoginRequestDto;
import com.vertyll.fastprod.modules.auth.dto.LoginRequestDto.FormBuilder;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.components.Toast;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.i18n.I18n;
import com.vertyll.fastprod.shared.security.SecurityService;
import com.vertyll.fastprod.shared.security.TokenRefreshService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import lombok.extern.slf4j.Slf4j;

@Route("login")
@Slf4j
public final class LoginView extends VerticalLayout implements HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SIGN_IN_KEY = "auth.login.title";
    private static final String COLOR = "color";
    private static final String MARGIN_BOTTOM = "margin-bottom";
    private static final String LUMO_SPACE_M = "var(--lumo-space-m)";
    private static final String TEXT_ALIGN = "text-align";
    private static final String CENTER = "center";

    private final transient AuthService authService;
    private final transient SecurityService securityService;
    private final transient TokenRefreshService tokenRefreshService;
    private final Binder<FormBuilder> binder;

    private final EmailField emailField = new EmailField(I18n.t("common.email"));
    private final PasswordField passwordField = new PasswordField(I18n.t("common.password"));
    private final Button loginButton = new Button(I18n.t(SIGN_IN_KEY));

    public LoginView(
        AuthService authService,
        SecurityService securityService,
        TokenRefreshService tokenRefreshService
    ) {
        super();
        this.authService = authService;
        this.securityService = securityService;
        this.tokenRefreshService = tokenRefreshService;
        this.binder = new Binder<>(FormBuilder.class);

        setWidthFull();
        setMinHeight("100vh");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "var(--lumo-base-color)")
            .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
            .set("box-sizing", "border-box");

        createForm();
    }

    private void createForm() {
        Div card = new Div();
        card.addClassName("login-card");
        card.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("border", "1px solid var(--lumo-contrast-10pct)")
            .set("padding", "var(--lumo-space-xl)")
            .set("max-width", "400px")
            .set("width", "100%")
            .set("box-sizing", "border-box");

        H1 title = new H1(I18n.t(SIGN_IN_KEY));
        title.getStyle()
            .set("margin", "0")
            .set("font-size", "var(--lumo-font-size-xxxl)")
            .set("font-weight", "600")
            .set(COLOR, "var(--lumo-primary-text-color)");

        Paragraph subtitle = new Paragraph(I18n.t("auth.login.subtitle"));
        subtitle.getStyle()
            .set("margin", "var(--lumo-space-xs) 0 var(--lumo-space-xl) 0")
            .set(COLOR, "var(--lumo-secondary-text-color)");

        emailField.setRequiredIndicatorVisible(true);
        emailField.setErrorMessage(I18n.t("validation.email.invalid"));
        emailField.setClearButtonVisible(true);
        emailField.setWidthFull();
        emailField.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setClearButtonVisible(true);
        passwordField.setWidthFull();
        passwordField.getStyle().set(MARGIN_BOTTOM, "var(--lumo-space-l)");

        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.setWidthFull();
        loginButton.addClickListener(_ -> handleLogin());
        loginButton.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        RouterLink forgotPasswordLink = new RouterLink(I18n.t("auth.login.forgot"), ForgotPasswordView.class);
        forgotPasswordLink.getStyle()
            .set(COLOR, "var(--lumo-primary-color)")
            .set("text-decoration", "none")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("display", "block")
            .set(TEXT_ALIGN, CENTER)
            .set(MARGIN_BOTTOM, LUMO_SPACE_M);

        RouterLink registerLink = new RouterLink(I18n.t("auth.login.createAccount"), RegisterView.class);
        registerLink.getStyle()
            .set(COLOR, "var(--lumo-primary-color)")
            .set("text-decoration", "none")
            .set("font-weight", "500");

        Div registerContainer = new Div();
        registerContainer.getStyle().set(TEXT_ALIGN, CENTER).set("margin-top", LUMO_SPACE_M);
        registerContainer.add(new Span(I18n.t("auth.login.noAccount")), registerLink);

        configureBinder();

        card.add(title, subtitle, emailField, passwordField, loginButton, forgotPasswordLink, registerContainer);

        add(card);
    }

    private void configureBinder() {
        binder.forField(emailField)
            .withValidator(new EmailValidator(I18n.t("validation.email.invalid")))
            .bind(FormBuilder::getEmail, FormBuilder::setEmail);

        binder.forField(passwordField)
            .asRequired(I18n.t("validation.password.required"))
            .bind(FormBuilder::getPassword, FormBuilder::setPassword);
    }

    private void handleLogin() {
        try {
            FormBuilder form = new FormBuilder();
            binder.writeBean(form);

            LoginRequestDto loginRequest = form.toDto();

            loginButton.setEnabled(false);
            loginButton.setText(I18n.t("auth.login.submitting"));

            AuthResponseDto response = authService.login(loginRequest);

            securityService.login(response);
            tokenRefreshService.setTokenExpiration();

            String message = I18n.t("auth.login.success");
            Toast.show(message, NotificationVariant.LUMO_SUCCESS);

            UI.getCurrent().getPage().setLocation("/");

        } catch (ValidationException e) {
            log.error("Validation error during login", e);
        } catch (ApiException e) {
            if (e.getStatusCode() == 403 && "errors.auth.accountNotVerified".equals(e.getMessage())) {
                Toast.show(I18n.t("auth.login.notVerified"), NotificationVariant.LUMO_WARNING);
                String email = emailField.getValue();
                UI.getCurrent().navigate(VerifyAccountView.class, email);
            } else {
                Toast.show(e.getMessage(), NotificationVariant.LUMO_ERROR);
                log.error("API error during login: {} (status: {})", e.getMessage(), e.getStatusCode());
            }
        } finally {
            loginButton.setEnabled(true);
            loginButton.setText(I18n.t(SIGN_IN_KEY));
        }
    }

    @Override
    public String getPageTitle() {
        return I18n.t("pages.login");
    }
}
