package com.vertyll.fastprod.modules.auth.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vertyll.fastprod.modules.auth.dto.VerifyAccountRequestDto;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.exception.ApiException;
import lombok.extern.slf4j.Slf4j;

@Route("verify-account")
@PageTitle("Verify Account | FastProd")
@Slf4j
public class VerifyAccountView extends VerticalLayout implements HasUrlParameter<String> {

    private final AuthService authService;

    private TextField codeField;
    private TextField emailField;
    private Button verifyButton;
    private Button resendButton;

    public VerifyAccountView(AuthService authService) {
        this.authService = authService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-contrast-5pct), var(--lumo-contrast-10pct))")
                .set("padding", "var(--lumo-space-l)");

        createView();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String email) {
        if (email != null && !email.isEmpty()) {
            emailField.setValue(email);
            emailField.setReadOnly(true);
        }
    }

    private void createView() {
        Div card = new Div();
        card.addClassName("verify-card");
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xl)")
                .set("padding", "var(--lumo-space-xl)")
                .set("max-width", "500px")
                .set("width", "100%")
                .set("text-align", "center");

        Icon icon = VaadinIcon.ENVELOPE_O.create();
        icon.setSize("64px");
        icon.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("margin-bottom", "var(--lumo-space-m)");

        H1 title = new H1("Verify Your Account");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxxl)")
                .set("font-weight", "600")
                .set("color", "var(--lumo-primary-text-color)");

        Paragraph description = new Paragraph(
                "We've sent a verification code to your email address. " +
                        "Please enter the code below to verify your account."
        );
        description.getStyle()
                .set("margin", "var(--lumo-space-s) 0 var(--lumo-space-xl) 0")
                .set("color", "var(--lumo-secondary-text-color)");

        emailField = new TextField("Email Address");
        emailField.setWidthFull();
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setPlaceholder("your.email@example.com");
        emailField.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        codeField = new TextField("Verification Code");
        codeField.setWidthFull();
        codeField.setPrefixComponent(VaadinIcon.KEY.create());
        codeField.setPlaceholder("Enter 6-digit code");
        codeField.setMaxLength(6);
        codeField.setPattern("[0-9]*");
        codeField.getStyle()
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("text-align", "center");

        verifyButton = new Button("Verify Account", VaadinIcon.CHECK.create());
        verifyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        verifyButton.setWidthFull();
        verifyButton.addClickListener(_ -> handleVerification());
        verifyButton.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        resendButton = new Button("Resend Code", VaadinIcon.REFRESH.create());
        resendButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resendButton.setWidthFull();
        resendButton.addClickListener(_ -> handleResendCode());
        resendButton.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        Button backButton = new Button("Back to Login", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        backButton.addClickListener(_ -> UI.getCurrent().navigate(LoginView.class));

        card.add(
                icon,
                title,
                description,
                emailField,
                codeField,
                verifyButton,
                resendButton,
                backButton
        );

        add(card);
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void handleVerification() {
        String code = codeField.getValue();

        if (code == null || code.trim().isEmpty()) {
            showNotification("Please enter the verification code", NotificationVariant.LUMO_ERROR);
            return;
        }

        if (code.length() != 6) {
            showNotification("Verification code must be 6 digits", NotificationVariant.LUMO_ERROR);
            return;
        }

        verifyButton.setEnabled(false);
        verifyButton.setText("Verifying...");

        try {
            VerifyAccountRequestDto verifyAccountRequest = new VerifyAccountRequestDto(code);
            authService.verifyAccount(verifyAccountRequest);
            showNotification("Account verified successfully! You can now log in.", NotificationVariant.LUMO_SUCCESS);

            UI ui = UI.getCurrent();
            java.util.concurrent.CompletableFuture
                    .runAsync(() -> {}, java.util.concurrent.CompletableFuture.delayedExecutor(2, java.util.concurrent.TimeUnit.SECONDS))
                    .thenRun(() -> ui.access(() -> ui.navigate(LoginView.class)))
                    .exceptionally(ex -> {
                        log.error("Delayed navigation failed", ex);
                        return null;
                    });

        } catch (ApiException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
            log.error("API error during verification: {}", e.getMessage());
        } catch (Exception e) {
            showNotification("Verification failed. Please check your code and try again.", NotificationVariant.LUMO_ERROR);
            log.error("Error during verification", e);
        } finally {
            verifyButton.setEnabled(true);
            verifyButton.setText("Verify Account");
        }
    }

    private void handleResendCode() {
        String email = emailField.getValue();

        if (email == null || email.trim().isEmpty()) {
            showNotification("Please enter your email address", NotificationVariant.LUMO_ERROR);
            return;
        }

        resendButton.setEnabled(false);
        resendButton.setText("Sending...");

        try {
            authService.resendVerificationCode(email);
            showNotification("Verification code sent! Please check your email.", NotificationVariant.LUMO_SUCCESS);
            codeField.focus();
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                showNotification(
                        "Resend feature is not yet available. Please contact support if you didn't receive the email.",
                        NotificationVariant.LUMO_WARNING
                );
            } else {
                showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
            }
            log.error("API error during resend: {}", e.getMessage());
        } catch (Exception e) {
            showNotification(
                    "Failed to resend code. Please try registering again or contact support.",
                    NotificationVariant.LUMO_ERROR
            );
            log.error("Error during resend", e);
        } finally {
            resendButton.setEnabled(true);
            resendButton.setText("Resend Code");
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
