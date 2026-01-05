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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.components.VerificationCodeDialog;
import com.vertyll.fastprod.shared.exception.ApiException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Route("forgot-password")
@PageTitle("Forgot Password | FastProd")
@AnonymousAllowed
@Slf4j
public class ForgotPasswordView extends VerticalLayout {

    private static final String COLOR = "color";
    private static final String MARGIN_BOTTOM = "margin-bottom";
    private static final String LUMO_SPACE_M = "var(--lumo-space-m)";
    private static final String TEXT_ALIGN = "text-align";
    private static final String CENTER = "center";

    private final transient AuthService authService;
    private final Binder<FormData> binder;

    private EmailField emailField;
    private Button submitButton;

    public ForgotPasswordView(AuthService authService) {
        this.authService = authService;
        this.binder = new Binder<>(FormData.class);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle()
                .set(
                        "background",
                        "linear-gradient(135deg, var(--lumo-contrast-5pct), var(--lumo-contrast-10pct))")
                .set("padding", "var(--lumo-space-l)");

        createView();
    }

    private void createView() {
        Div card = new Div();
        card.addClassName("forgot-password-card");
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xl)")
                .set("padding", "var(--lumo-space-xl)")
                .set("max-width", "500px")
                .set("width", "100%")
                .set(TEXT_ALIGN, CENTER);

        Icon icon = VaadinIcon.KEY_O.create();
        icon.setSize("64px");
        icon.getStyle().set(COLOR, "var(--lumo-primary-color)").set(MARGIN_BOTTOM, LUMO_SPACE_M);

        H1 title = new H1("Forgot Password?");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxxl)")
                .set("font-weight", "600")
                .set(COLOR, "var(--lumo-primary-text-color)");

        Paragraph description =
                new Paragraph(
                        "Enter your email address and we'll send you instructions to reset your password.");
        description
                .getStyle()
                .set("margin", "var(--lumo-space-s) 0 var(--lumo-space-xl) 0")
                .set(COLOR, "var(--lumo-secondary-text-color)");

        emailField = new EmailField("Email Address");
        emailField.setWidthFull();
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setPlaceholder("your.email@example.com");
        emailField.setRequiredIndicatorVisible(true);
        emailField.getStyle().set(MARGIN_BOTTOM, "var(--lumo-space-l)");

        binder.forField(emailField)
                .asRequired("Email is required")
                .withValidator(new EmailValidator("Please enter a valid email address"))
                .bind(FormData::email, FormData::setEmail);

        submitButton = new Button("Send Reset Instructions", VaadinIcon.ENVELOPE_OPEN.create());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        submitButton.setWidthFull();
        submitButton.addClickListener(_ -> handleSubmit());
        submitButton.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        Button backButton = new Button("Back to Login", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        backButton.addClickListener(_ -> UI.getCurrent().navigate(LoginView.class));

        card.add(icon, title, description, emailField, submitButton, backButton);

        add(card);
    }

    private void handleSubmit() {
        if (!binder.validate().isOk()) {
            return;
        }

        String email = emailField.getValue();
        submitButton.setEnabled(false);
        submitButton.setText("Sending...");

        try {
            authService.requestPasswordReset(email);
            showNotification(
                    "Password reset code sent! Please check your email.",
                    NotificationVariant.LUMO_SUCCESS);
            emailField.setEnabled(false);
            showVerificationDialog();

        } catch (ApiException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
            log.error("API error during password reset request: {}", e.getMessage());
        } catch (Exception e) {
            showNotification(
                    "Failed to send reset instructions. Please try again.",
                    NotificationVariant.LUMO_ERROR);
            log.error("Error during password reset request", e);
        } finally {
            submitButton.setEnabled(true);
            submitButton.setText("Send Reset Instructions");
        }
    }

    private void showVerificationDialog() {
        VerificationCodeDialog dialog =
                new VerificationCodeDialog(
                        "Enter Reset Code",
                        "Please enter the 6-digit code sent to your email.",
                        this::handleVerifyCode);
        dialog.open();
    }

    private void handleVerifyCode(String code, VerificationCodeDialog dialog) {
        dialog.close();
        UI.getCurrent().navigate(ResetPasswordView.class, code);
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
                .set(TEXT_ALIGN, CENTER);

        notification.add(text);
        notification.open();
    }

    @Setter
    private static class FormData {
        private String email;

        String email() {
            return email;
        }
    }
}
