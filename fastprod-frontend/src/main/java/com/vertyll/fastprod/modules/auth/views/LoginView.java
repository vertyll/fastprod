package com.vertyll.fastprod.modules.auth.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vertyll.fastprod.modules.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.modules.auth.dto.LoginRequestDto;
import com.vertyll.fastprod.modules.auth.dto.LoginRequestDto.FormBuilder;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.security.SecurityService;
import com.vertyll.fastprod.shared.security.TokenRefreshService;
import lombok.extern.slf4j.Slf4j;

@Route("login")
@PageTitle("Login | FastProd")
@Slf4j
public class LoginView extends VerticalLayout {

    private final AuthService authService;
    private final SecurityService securityService;
    private final TokenRefreshService tokenRefreshService;
    private final Binder<FormBuilder> binder;

    private EmailField emailField;
    private PasswordField passwordField;
    private Button loginButton;

    public LoginView(AuthService authService, SecurityService securityService, TokenRefreshService tokenRefreshService) {
        this.authService = authService;
        this.securityService = securityService;
        this.tokenRefreshService = tokenRefreshService;
        this.binder = new Binder<>(FormBuilder.class);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-contrast-5pct), var(--lumo-contrast-10pct))")
                .set("padding", "var(--lumo-space-l)");

        createForm();
    }

    private void createForm() {
        Div card = new Div();
        card.addClassName("login-card");
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xl)")
                .set("padding", "var(--lumo-space-xl)")
                .set("max-width", "400px")
                .set("width", "100%");

        H1 title = new H1("Sign In");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxxl)")
                .set("font-weight", "600")
                .set("color", "var(--lumo-primary-text-color)");

        Paragraph subtitle = new Paragraph("Sign in to your account");
        subtitle.getStyle()
                .set("margin", "var(--lumo-space-xs) 0 var(--lumo-space-xl) 0")
                .set("color", "var(--lumo-secondary-text-color)");

        emailField = new EmailField("Email");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setErrorMessage("Please enter a valid email address");
        emailField.setClearButtonVisible(true);
        emailField.setWidthFull();
        emailField.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        passwordField = new PasswordField("Password");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setClearButtonVisible(true);
        passwordField.setWidthFull();
        passwordField.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        loginButton = new Button("Sign In");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.setWidthFull();
        loginButton.addClickListener(e -> handleLogin());
        loginButton.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        RouterLink forgotPasswordLink = new RouterLink("Forgot password?", ForgotPasswordView.class);
        forgotPasswordLink.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("text-decoration", "none")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("display", "block")
                .set("text-align", "center")
                .set("margin-bottom", "var(--lumo-space-m)");

        RouterLink registerLink = new RouterLink("Create an account", RegisterView.class);
        registerLink.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("text-decoration", "none")
                .set("font-weight", "500");

        Div registerContainer = new Div();
        registerContainer.getStyle()
                .set("text-align", "center")
                .set("margin-top", "var(--lumo-space-m)");
        registerContainer.add(new Span("Don't have an account? "), registerLink);

        configureBinder();

        card.add(title, subtitle, emailField, passwordField, loginButton, forgotPasswordLink, registerContainer);

        add(card);
    }

    private void configureBinder() {
        binder.forField(emailField)
                .withValidator(new EmailValidator("Please enter a valid email address"))
                .bind(FormBuilder::getEmail, FormBuilder::setEmail);

        binder.forField(passwordField)
                .asRequired("Password is required")
                .bind(FormBuilder::getPassword, FormBuilder::setPassword);
    }

    private void handleLogin() {
        try {
            FormBuilder form = new FormBuilder();
            binder.writeBean(form);

            LoginRequestDto loginRequest = form.toDto();

            loginButton.setEnabled(false);
            loginButton.setText("Signing in...");

            ApiResponse<AuthResponseDto> response = authService.login(loginRequest);

            if (response.data() != null) {
                securityService.login(response.data());
                tokenRefreshService.setTokenExpiration();
            }

            String message = response.message() != null
                    ? response.message()
                    : "Login successful!";
            showNotification(message, NotificationVariant.LUMO_SUCCESS);

            UI.getCurrent().getPage().setLocation("/");

        } catch (ValidationException e) {
            log.error("Validation error during login", e);
        } catch (ApiException e) {
            if (e.getStatusCode() == 403 && e.getMessage().contains("not verified")) {
                showNotification(
                        "Your account is not verified. Please check your email for the verification code.",
                        NotificationVariant.LUMO_WARNING
                );
                String email = emailField.getValue();
                UI.getCurrent().navigate(VerifyAccountView.class, email);
            } else {
                showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
                log.error("API error during login: {} (status: {})", e.getMessage(), e.getStatusCode());
            }
        } catch (Exception e) {
            showNotification("An unexpected error occurred. Please try again.", NotificationVariant.LUMO_ERROR);
            log.error("Unexpected error during login", e);
        } finally {
            loginButton.setEnabled(true);
            loginButton.setText("Sign In");
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification();
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(5000);

        Div text = new Div();
        text.setText(message);
        text.getStyle()
                .set("white-space", "normal")
                .set("max-width", "400px")
                .set("text-align", "center");

        notification.add(text);
        notification.open();
    }
}
