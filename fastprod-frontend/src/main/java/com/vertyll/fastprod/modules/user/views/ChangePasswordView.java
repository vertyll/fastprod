package com.vertyll.fastprod.modules.user.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.modules.user.dto.ChangePasswordDto;
import com.vertyll.fastprod.shared.components.VerificationCodeDialog;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;

@Route(value = "profile/change-password", layout = MainLayout.class)
@PageTitle("Change Password | FastProd")
@PermitAll
@Slf4j
public class ChangePasswordView extends VerticalLayout {

    private final Binder<ChangePasswordDto> binder;
    private final AuthService authService;

    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;

    public ChangePasswordView(AuthService authService) {
        this.binder = new Binder<>(ChangePasswordDto.class);

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

        H2 title = new H2("Change Password");
        title.addClassNames(LumoUtility.Margin.Bottom.LARGE);

        currentPasswordField = new PasswordField("Current Password");
        currentPasswordField.setWidthFull();
        currentPasswordField.setRequiredIndicatorVisible(true);

        newPasswordField = new PasswordField("New Password");
        newPasswordField.setWidthFull();
        newPasswordField.setRequiredIndicatorVisible(true);
        newPasswordField.setHelperText("At least 8 characters with a letter and a digit");

        confirmPasswordField = new PasswordField("Confirm New Password");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequiredIndicatorVisible(true);

        binder.forField(currentPasswordField)
                .asRequired("Current password is required")
                .bind(ChangePasswordDto::currentPassword, (_, _) -> {
                });

        binder.forField(newPasswordField)
                .asRequired("New password is required")
                .withValidator(pwd -> pwd.length() >= 8, "Password must be at least 8 characters")
                .withValidator(pwd -> pwd.matches("^(?=.*[A-Za-z])(?=.*\\d).+$"),
                        "Password must contain at least one letter and one digit")
                .bind(ChangePasswordDto::newPassword, (_, _) -> {
                });

        Button saveButton = new Button("Change Password");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidthFull();
        saveButton.addClickListener(_ -> handleChangePassword());

        Button cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.setWidthFull();
        cancelButton.addClickListener(_ -> UI.getCurrent().navigate("profile"));

        formLayout.add(
                title,
                currentPasswordField,
                newPasswordField,
                confirmPasswordField,
                saveButton,
                cancelButton
        );

        add(formLayout);
    }

    private void handleChangePassword() {
        try {
            String newPwd = newPasswordField.getValue();
            String confirmPwd = confirmPasswordField.getValue();

            if (!newPwd.equals(confirmPwd)) {
                showNotification("Passwords do not match", NotificationVariant.LUMO_ERROR);
                return;
            }

            ChangePasswordDto dto = new ChangePasswordDto(
                    currentPasswordField.getValue(),
                    newPwd
            );

            if (binder.validate().isOk()) {
                authService.requestPasswordChange(dto);
                showNotification(
                        "Verification code sent to your email. Please check your inbox.",
                        NotificationVariant.LUMO_SUCCESS
                );
                clearFormAndValidation();
                showVerificationDialog();
            }
        } catch (Exception e) {
            log.error("Failed to request password change", e);
            showNotification("Failed to change password. Check your current password.", NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearFormAndValidation() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();

        currentPasswordField.setInvalid(false);
        newPasswordField.setInvalid(false);
        confirmPasswordField.setInvalid(false);

        binder.readBean(null);
    }

    private void showVerificationDialog() {
        VerificationCodeDialog dialog = new VerificationCodeDialog(
                "Verify Password Change",
                "Enter the 6-digit verification code sent to your email to complete the password change.",
                this::handleVerifyCode
        );
        dialog.open();
    }

    private void handleVerifyCode(String code, VerificationCodeDialog dialog) {
        try {
            authService.verifyPasswordChange(code);
            dialog.showSuccess("Password changed successfully!");
            UI.getCurrent().navigate("profile");
        } catch (Exception e) {
            log.error("Failed to verify password change", e);
            dialog.showError("Verification failed. Please check your code and try again.");
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification();
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(3000);

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
