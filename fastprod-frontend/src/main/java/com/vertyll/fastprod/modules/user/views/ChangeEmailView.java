package com.vertyll.fastprod.modules.user.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.modules.user.dto.ChangeEmailDto;
import com.vertyll.fastprod.shared.components.VerificationCodeDialog;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import com.vertyll.fastprod.shared.security.SecurityService;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;

@Route(value = "profile/change-email", layout = MainLayout.class)
@PageTitle("Change Email | FastProd")
@PermitAll
@Slf4j
public class ChangeEmailView extends VerticalLayout {

    private final transient SecurityService securityService;
    private final Binder<ChangeEmailDto> binder;
    private final transient AuthService authService;

    private EmailField newEmailField;
    private PasswordField passwordField;

    public ChangeEmailView(SecurityService securityService, AuthService authService) {
        this.securityService = securityService;
        this.binder = new Binder<>(ChangeEmailDto.class);

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        createForm();
        this.authService = authService;
    }

    private void createForm() {
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidth("400px");
        formLayout.setPadding(true);
        formLayout.setSpacing(true);
        formLayout.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xl)");

        H2 title = new H2("Change Email");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        newEmailField = new EmailField("New Email Address");
        newEmailField.setWidthFull();
        newEmailField.setRequiredIndicatorVisible(true);

        passwordField = new PasswordField("Confirm Your Password");
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setHelperText("Enter your current password to confirm the change");

        binder.forField(newEmailField)
                .asRequired("Email is required")
                .withValidator(new EmailValidator("Please enter a valid email address"))
                .bind(ChangeEmailDto::newEmail, (_, _) -> {
                });

        binder.forField(passwordField)
                .asRequired("Password is required")
                .bind(ChangeEmailDto::currentPassword, (_, _) -> {
                });

        Button saveButton = new Button("Request Email Change");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidthFull();
        saveButton.addClickListener(_ -> handleChangeEmail());

        Button cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.setWidthFull();
        cancelButton.addClickListener(_ -> UI.getCurrent().navigate("profile"));

        formLayout.add(
                title,
                newEmailField,
                passwordField,
                saveButton,
                cancelButton
        );

        add(formLayout);
    }

    private void handleChangeEmail() {
        try {
            ChangeEmailDto dto = new ChangeEmailDto(
                    passwordField.getValue(),
                    newEmailField.getValue()
            );

            if (binder.validate().isOk()) {
                authService.requestEmailChange(dto);
                showNotification(
                        "Verification code sent to your new email address. Please check your inbox.",
                        NotificationVariant.LUMO_SUCCESS
                );
                clearFormAndValidation();
                showVerificationDialog();
            }
        } catch (Exception e) {
            log.error("Failed to request email change", e);
            showNotification(
                    "Failed to change email. Check your password or ensure the email is not already in use.",
                    NotificationVariant.LUMO_ERROR
            );
        }
    }

    private void clearFormAndValidation() {
        newEmailField.clear();
        passwordField.clear();

        newEmailField.setInvalid(false);
        passwordField.setInvalid(false);

        binder.readBean(null);
    }

    private void showVerificationDialog() {
        VerificationCodeDialog dialog = new VerificationCodeDialog(
                "Verify Email Change",
                "Enter the 6-digit verification code sent to your new email address to complete the email change.",
                this::handleVerifyCode
        );
        dialog.open();
    }

    private void handleVerifyCode(String code, VerificationCodeDialog dialog) {
        try {
            ApiResponse<AuthResponseDto> response = authService.verifyEmailChange(code);

            // Update token with new one (email changed, so JWT needs to be updated)
            if (response.data() != null) {
                securityService.login(response.data());
            }

            dialog.showSuccess("Email changed successfully! Please log in again with your new email.");
            // After email change, user needs to log in again
            UI.getCurrent().getPage().setLocation("/login");
        } catch (Exception e) {
            log.error("Failed to verify email change", e);
            dialog.showError("Verification failed. Please check your code and try again.");
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
