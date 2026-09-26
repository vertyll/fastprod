package com.vertyll.fastprod.modules.auth.views;

import java.io.Serial;
import java.time.Duration;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.base.ui.DelayedNavigation;
import com.vertyll.fastprod.modules.auth.dto.ResetPasswordRequestDto;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.exception.ApiException;

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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import lombok.extern.slf4j.Slf4j;

@Route("reset-password")
@PageTitle("Reset Password | FastProd")
@AnonymousAllowed
@Slf4j
public final class ResetPasswordView extends VerticalLayout implements HasUrlParameter<String> {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COLOR = "color";
    private static final String MARGIN_BOTTOM = "margin-bottom";
    private static final String LUMO_SPACE_M = "var(--lumo-space-m)";

    private final transient AuthService authService;
    private final Binder<FormData> binder;

    private @Nullable String resetToken;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private Button submitButton;

    public ResetPasswordView(AuthService authService) {
        super();
        this.authService = authService;
        this.binder = new Binder<>(FormData.class);

        setWidthFull();
        setMinHeight("100vh");
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "var(--lumo-base-color)")
            .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
            .set("box-sizing", "border-box");

        createView();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String token) {
        if (token != null && !token.isEmpty()) {
            this.resetToken = token;
        } else {
            showNotification("Invalid or missing reset token", NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate(LoginView.class);
        }
    }

    private void createView() {
        Div card = new Div();
        card.addClassName("reset-password-card");
        card.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("border", "1px solid var(--lumo-contrast-10pct)")
            .set("padding", "var(--lumo-space-xl)")
            .set("max-width", "500px")
            .set("width", "100%")
            .set("box-sizing", "border-box")
            .set("text-align", "center");

        Icon icon = VaadinIcon.LOCK.create();
        icon.setSize("64px");
        icon.getStyle().set(COLOR, "var(--lumo-primary-color)").set(MARGIN_BOTTOM, LUMO_SPACE_M);

        H1 title = new H1("Reset Your Password");
        title.getStyle()
            .set("margin", "0")
            .set("font-size", "var(--lumo-font-size-xxxl)")
            .set("font-weight", "600")
            .set(COLOR, "var(--lumo-primary-text-color)");

        Paragraph description = new Paragraph("Enter your new password below. Make sure it's strong and secure.");
        description.getStyle()
            .set("margin", "var(--lumo-space-s) 0 var(--lumo-space-xl) 0")
            .set(COLOR, "var(--lumo-secondary-text-color)");

        newPasswordField = new PasswordField("New Password");
        newPasswordField.setWidthFull();
        newPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
        newPasswordField.setRequiredIndicatorVisible(true);
        newPasswordField.setHelperText("At least 8 characters with a letter and a digit");
        newPasswordField.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        confirmPasswordField = new PasswordField("Confirm New Password");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setPrefixComponent(VaadinIcon.LOCK.create());
        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.getStyle().set(MARGIN_BOTTOM, "var(--lumo-space-l)");

        binder.forField(newPasswordField)
            .asRequired("Password is required")
            .withValidator(pwd -> pwd.length() >= 8, "Password must be at least 8 characters")
            .withValidator(
                pwd -> pwd.matches("^(?=.*[A-Za-z])(?=.*\\d).+$"),
                "Password must contain at least one letter and one digit"
            )
            .bind(FormData::newPassword, FormData::setNewPassword);

        binder.forField(confirmPasswordField)
            .asRequired("Please confirm your password")
            .bind(FormData::confirmPassword, FormData::setConfirmPassword);

        submitButton = new Button("Reset Password", VaadinIcon.CHECK.create());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        submitButton.setWidthFull();
        submitButton.addClickListener(_ -> handleSubmit());
        submitButton.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        Button backButton = new Button("Back to Login", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        backButton.addClickListener(_ -> UI.getCurrent().navigate(LoginView.class));

        card.add(icon, title, description, newPasswordField, confirmPasswordField, submitButton, backButton);

        add(card);
    }

    private void handleSubmit() {
        if (!binder.validate().isOk()) {
            return;
        }

        String newPwd = newPasswordField.getValue();
        String confirmPwd = confirmPasswordField.getValue();

        if (!newPwd.equals(confirmPwd)) {
            showNotification("Passwords do not match", NotificationVariant.LUMO_ERROR);
            return;
        }

        String token = resetToken;
        if (token == null || token.isEmpty()) {
            showNotification("Invalid reset token", NotificationVariant.LUMO_ERROR);
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Resetting...");

        try {
            ResetPasswordRequestDto request = new ResetPasswordRequestDto(newPwd);
            authService.resetPassword(token, request);

            showNotification(
                "Password reset successfully! You can now log in with your new password.",
                NotificationVariant.LUMO_SUCCESS
            );

            UI ui = UI.getCurrent();
            DelayedNavigation.navigate(ui, LoginView.class, Duration.ofSeconds(2));
        } catch (ApiException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
            log.error("API error during password reset: {}", e.getMessage());
        } finally {
            submitButton.setEnabled(true);
            submitButton.setText("Reset Password");
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification();
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(5000);

        Div text = new Div();
        text.setText(message);
        text.getStyle().set("white-space", "normal").set("max-width", "400px").set("text-align", "center");

        notification.add(text);
        notification.open();
    }
    @SuppressWarnings("PMD.DataClass")
    private static final class FormData {
        private String newPassword = "";
        private String confirmPassword = "";

        String newPassword() {
            return newPassword;
        }

        String confirmPassword() {
            return confirmPassword;
        }

        void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
