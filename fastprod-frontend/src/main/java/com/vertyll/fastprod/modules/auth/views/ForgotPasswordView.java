package com.vertyll.fastprod.modules.auth.views;

import java.io.Serial;

import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.components.Toast;
import com.vertyll.fastprod.shared.components.VerificationCodeDialog;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import lombok.extern.slf4j.Slf4j;

@Route("forgot-password")
@AnonymousAllowed
@Slf4j
public final class ForgotPasswordView extends VerticalLayout implements HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

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

    private void createView() {
        Div card = new Div();
        card.addClassName("forgot-password-card");
        card.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("border", "1px solid var(--lumo-contrast-10pct)")
            .set("padding", "var(--lumo-space-xl)")
            .set("max-width", "500px")
            .set("width", "100%")
            .set("box-sizing", "border-box")
            .set(TEXT_ALIGN, CENTER);

        Icon icon = VaadinIcon.KEY_O.create();
        icon.setSize("64px");
        icon.getStyle().set(COLOR, "var(--lumo-primary-color)").set(MARGIN_BOTTOM, LUMO_SPACE_M);

        H1 title = new H1(I18n.t("auth.forgot.title"));
        title.getStyle()
            .set("margin", "0")
            .set("font-size", "var(--lumo-font-size-xxxl)")
            .set("font-weight", "600")
            .set(COLOR, "var(--lumo-primary-text-color)");

        Paragraph description = new Paragraph(I18n.t("auth.forgot.description"));
        description.getStyle()
            .set("margin", "var(--lumo-space-s) 0 var(--lumo-space-xl) 0")
            .set(COLOR, "var(--lumo-secondary-text-color)");

        emailField = new EmailField(I18n.t("common.emailAddress"));
        emailField.setWidthFull();
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setPlaceholder(I18n.t("common.emailPlaceholder"));
        emailField.setRequiredIndicatorVisible(true);
        emailField.getStyle().set(MARGIN_BOTTOM, "var(--lumo-space-l)");

        binder.forField(emailField)
            .asRequired(I18n.t("validation.email.required"))
            .withValidator(new EmailValidator(I18n.t("validation.email.invalid")))
            .bind(FormData::email, FormData::setEmail);

        submitButton = new Button(I18n.t("auth.forgot.submit"), VaadinIcon.ENVELOPE_OPEN.create());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        submitButton.setWidthFull();
        submitButton.addClickListener(_ -> handleSubmit());
        submitButton.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        Button backButton = new Button(I18n.t("auth.backToLogin"), VaadinIcon.ARROW_LEFT.create());
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
        submitButton.setText(I18n.t("common.sending"));

        try {
            authService.requestPasswordReset(email);
            Toast.show(I18n.t("auth.forgot.sent"), NotificationVariant.LUMO_SUCCESS);
            emailField.setEnabled(false);
            showVerificationDialog();
        } catch (ApiException e) {
            Toast.show(e.getMessage(), NotificationVariant.LUMO_ERROR);
            log.error("API error during password reset request: {}", e.getMessage());
        } finally {
            submitButton.setEnabled(true);
            submitButton.setText(I18n.t("auth.forgot.submit"));
        }
    }

    private void showVerificationDialog() {
        VerificationCodeDialog dialog = new VerificationCodeDialog(
            I18n.t("auth.forgot.codeTitle"),
            I18n.t("auth.forgot.codeDescription"),
            this::handleVerifyCode
        );
        dialog.open();
    }

    private void handleVerifyCode(String code, VerificationCodeDialog dialog) {
        dialog.close();
        UI.getCurrent().navigate(ResetPasswordView.class, code);
    }

    private static final class FormData {
        private String email = "";

        String email() {
            return email;
        }

        void setEmail(String email) {
            this.email = email;
        }
    }

    @Override
    public String getPageTitle() {
        return I18n.t("pages.forgotPassword");
    }
}
