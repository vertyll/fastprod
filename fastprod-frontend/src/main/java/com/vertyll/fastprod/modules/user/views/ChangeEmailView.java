package com.vertyll.fastprod.modules.user.views;

import java.io.Serial;

import jakarta.annotation.security.PermitAll;

import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.modules.user.dto.ChangeEmailDto;
import com.vertyll.fastprod.shared.components.VerificationCodeDialog;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.i18n.I18n;
import com.vertyll.fastprod.shared.security.SecurityService;

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
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import lombok.extern.slf4j.Slf4j;

@Route(value = "profile/change-email", layout = MainLayout.class)
@PermitAll
@Slf4j
public final class ChangeEmailView extends VerticalLayout implements HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient SecurityService securityService;
    private final Binder<ChangeEmailDto> binder;
    private final transient AuthService authService;

    private EmailField newEmailField;
    private PasswordField passwordField;

    public ChangeEmailView(SecurityService securityService, AuthService authService) {
        super();
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
        formLayout.setMaxWidth("420px");
        formLayout.setWidth("100%");
        formLayout.setPadding(true);
        formLayout.setSpacing(true);
        formLayout.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("box-shadow", "var(--lumo-box-shadow-xl)")
            .set("box-sizing", "border-box");

        H2 title = new H2(I18n.t("account.email.title"));
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        newEmailField = new EmailField(I18n.t("account.email.newEmail"));
        newEmailField.setWidthFull();
        newEmailField.setRequiredIndicatorVisible(true);

        passwordField = new PasswordField(I18n.t("account.email.confirmPassword"));
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setHelperText(I18n.t("account.email.confirmPasswordHint"));

        binder.forField(newEmailField)
            .asRequired(I18n.t("validation.email.required"))
            .withValidator(new EmailValidator(I18n.t("validation.email.invalid")))
            .bind(ChangeEmailDto::newEmail, (_, _) -> {
            });

        binder.forField(passwordField)
            .asRequired(I18n.t("validation.password.required"))
            .bind(ChangeEmailDto::currentPassword, (_, _) -> {
            });

        Button saveButton = new Button(I18n.t("account.email.submit"));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidthFull();
        saveButton.addClickListener(_ -> handleChangeEmail());

        Button cancelButton = new Button(I18n.t("common.cancel"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.setWidthFull();
        cancelButton.addClickListener(_ -> UI.getCurrent().navigate("profile"));

        formLayout.add(title, newEmailField, passwordField, saveButton, cancelButton);

        add(formLayout);
    }

    private void handleChangeEmail() {
        try {
            ChangeEmailDto dto = new ChangeEmailDto(passwordField.getValue(), newEmailField.getValue());

            if (binder.validate().isOk()) {
                authService.requestEmailChange(dto);
                showNotification(I18n.t("account.email.codeSent"), NotificationVariant.LUMO_SUCCESS);
                clearFormAndValidation();
                showVerificationDialog();
            }
        } catch (ApiException e) {
            log.error("Failed to request email change", e);
            showNotification(I18n.t("account.email.failed"), NotificationVariant.LUMO_ERROR);
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
            I18n.t("account.email.verifyTitle"),
            I18n.t("account.email.verifyDescription"),
            this::handleVerifyCode
        );
        dialog.open();
    }

    private void handleVerifyCode(String code, VerificationCodeDialog dialog) {
        try {
            AuthResponseDto response = authService.verifyEmailChange(code);

            if (response != null) {
                securityService.login(response);
            }

            dialog.showSuccess(I18n.t("account.email.changed"));
            UI.getCurrent().getPage().setLocation("/login");
        } catch (ApiException e) {
            log.error("Failed to verify email change", e);
            dialog.showError(I18n.t("verification.failed"));
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

    @Override
    public String getPageTitle() {
        return I18n.t("pages.changeEmail");
    }
}
