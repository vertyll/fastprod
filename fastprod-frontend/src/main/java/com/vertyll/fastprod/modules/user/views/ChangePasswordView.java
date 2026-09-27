package com.vertyll.fastprod.modules.user.views;

import java.io.Serial;
import java.util.Map;

import jakarta.annotation.security.PermitAll;

import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.modules.user.dto.ChangePasswordDto;
import com.vertyll.fastprod.shared.components.Toast;
import com.vertyll.fastprod.shared.components.VerificationCodeDialog;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import lombok.extern.slf4j.Slf4j;

@Route(value = "profile/change-password", layout = MainLayout.class)
@PermitAll
@Slf4j
public final class ChangePasswordView extends VerticalLayout implements HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Binder<ChangePasswordDto> binder;
    private final transient AuthService authService;

    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;

    public ChangePasswordView(AuthService authService) {
        super();
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
        formLayout.setMaxWidth("420px");
        formLayout.setWidth("100%");
        formLayout.setPadding(true);
        formLayout.setSpacing(true);
        formLayout.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("box-shadow", "var(--lumo-box-shadow-xl)")
            .set("box-sizing", "border-box");

        H2 title = new H2(I18n.t("account.password.title"));
        title.addClassNames(LumoUtility.Margin.Bottom.LARGE);

        currentPasswordField = new PasswordField(I18n.t("common.currentPassword"));
        currentPasswordField.setWidthFull();
        currentPasswordField.setRequiredIndicatorVisible(true);

        newPasswordField = new PasswordField(I18n.t("common.newPassword"));
        newPasswordField.setWidthFull();
        newPasswordField.setRequiredIndicatorVisible(true);
        newPasswordField.setHelperText(I18n.t("validation.password.hint"));

        confirmPasswordField = new PasswordField(I18n.t("common.confirmNewPassword"));
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequiredIndicatorVisible(true);

        binder.forField(currentPasswordField)
            .asRequired(I18n.t("validation.currentPassword.required"))
            .bind(ChangePasswordDto::currentPassword, (_, _) -> {
            });

        binder.forField(newPasswordField)
            .asRequired(I18n.t("validation.newPassword.required"))
            .withValidator(pwd -> pwd.length() >= 8, I18n.t("validation.password.minLength", Map.of("min", 8)))
            .withValidator(
                pwd -> pwd.matches("^(?=.*[A-Za-z])(?=.*\\d).+$"),
                I18n.t("validation.password.letterAndDigit")
            )
            .bind(ChangePasswordDto::newPassword, (_, _) -> {
            });

        Button saveButton = new Button(I18n.t("account.password.title"));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidthFull();
        saveButton.addClickListener(_ -> handleChangePassword());

        Button cancelButton = new Button(I18n.t("common.cancel"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.setWidthFull();
        cancelButton.addClickListener(_ -> UI.getCurrent().navigate("profile"));

        formLayout.add(title, currentPasswordField, newPasswordField, confirmPasswordField, saveButton, cancelButton);

        add(formLayout);
    }

    private void handleChangePassword() {
        try {
            String newPwd = newPasswordField.getValue();
            String confirmPwd = confirmPasswordField.getValue();

            if (!newPwd.equals(confirmPwd)) {
                Toast.show(I18n.t("validation.password.mismatch"), NotificationVariant.LUMO_ERROR);
                return;
            }

            ChangePasswordDto dto = new ChangePasswordDto(currentPasswordField.getValue(), newPwd);

            if (binder.validate().isOk()) {
                authService.requestPasswordChange(dto);
                Toast.show(I18n.t("account.password.codeSent"), NotificationVariant.LUMO_SUCCESS);
                clearFormAndValidation();
                showVerificationDialog();
            }
        } catch (ApiException e) {
            log.error("Failed to request password change", e);
            Toast.show(I18n.t("account.password.failed"), NotificationVariant.LUMO_ERROR);
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
            I18n.t("account.password.verifyTitle"),
            I18n.t("account.password.verifyDescription"),
            this::handleVerifyCode
        );
        dialog.open();
    }

    private void handleVerifyCode(String code, VerificationCodeDialog dialog) {
        try {
            authService.verifyPasswordChange(code);
            dialog.showSuccess(I18n.t("account.password.changed"));
            UI.getCurrent().navigate("profile");
        } catch (ApiException e) {
            log.error("Failed to verify password change", e);
            dialog.showError(I18n.t("verification.failed"));
        }
    }

    @Override
    public String getPageTitle() {
        return I18n.t("pages.changePassword");
    }
}
