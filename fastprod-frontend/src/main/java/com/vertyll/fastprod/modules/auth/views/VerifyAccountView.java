package com.vertyll.fastprod.modules.auth.views;

import java.io.Serial;
import java.time.Duration;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.base.ui.DelayedNavigation;
import com.vertyll.fastprod.modules.auth.dto.VerifyAccountRequestDto;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.components.Toast;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import lombok.extern.slf4j.Slf4j;

@Route("verify-account")
@Slf4j
public final class VerifyAccountView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String TEXT_ALIGN = "text-align";
    private static final String CENTER = "center";
    private static final String MARGIN_BOTTOM = "margin-bottom";
    private static final String LUMO_SPACE_M = "var(--lumo-space-m)";
    private static final String COLOR = "color";

    private final transient AuthService authService;

    private TextField codeField;
    private TextField emailField;
    private Button verifyButton;
    private Button resendButton;

    public VerifyAccountView(AuthService authService) {
        super();
        this.authService = authService;

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
    public void setParameter(BeforeEvent event, @OptionalParameter @Nullable String email) {
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
            .set("border", "1px solid var(--lumo-contrast-10pct)")
            .set("padding", "var(--lumo-space-xl)")
            .set("max-width", "500px")
            .set("width", "100%")
            .set("box-sizing", "border-box")
            .set(TEXT_ALIGN, CENTER);

        Icon icon = VaadinIcon.ENVELOPE.create();
        icon.setSize("64px");
        icon.getStyle().set(COLOR, "var(--lumo-primary-color)").set(MARGIN_BOTTOM, LUMO_SPACE_M);

        H1 title = new H1(I18n.t("auth.verify.title"));
        title.getStyle()
            .set("margin", "0")
            .set("font-size", "var(--lumo-font-size-xxxl)")
            .set("font-weight", "600")
            .set(COLOR, "var(--lumo-primary-text-color)");

        Paragraph description = new Paragraph(I18n.t("auth.verify.description"));
        description.getStyle()
            .set("margin", "var(--lumo-space-s) 0 var(--lumo-space-xl) 0")
            .set(COLOR, "var(--lumo-secondary-text-color)");

        emailField = new TextField(I18n.t("common.emailAddress"));
        emailField.setWidthFull();
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        emailField.setPlaceholder(I18n.t("common.emailPlaceholder"));
        emailField.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        codeField = new TextField(I18n.t("verification.code"));
        codeField.setWidthFull();
        codeField.setPrefixComponent(VaadinIcon.KEY.create());
        codeField.setPlaceholder(I18n.t("verification.codePlaceholder"));
        codeField.setMaxLength(6);
        codeField.setPattern("[0-9]*");
        codeField.getStyle()
            .set(MARGIN_BOTTOM, "var(--lumo-space-l)")
            .set("letter-spacing", "0.3em")
            .set(TEXT_ALIGN, CENTER);

        verifyButton = new Button(I18n.t("auth.verify.submit"), VaadinIcon.CHECK.create());
        verifyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        verifyButton.setWidthFull();
        verifyButton.addClickListener(_ -> handleVerification());
        verifyButton.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        resendButton = new Button(I18n.t("auth.verify.resend"), VaadinIcon.REFRESH.create());
        resendButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resendButton.setWidthFull();
        resendButton.addClickListener(_ -> handleResendCode());
        resendButton.getStyle().set(MARGIN_BOTTOM, "var(--lumo-space-s)");

        Button backButton = new Button(I18n.t("auth.backToLogin"), VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        backButton.addClickListener(_ -> UI.getCurrent().navigate(LoginView.class));

        card.add(icon, title, description, emailField, codeField, verifyButton, resendButton, backButton);

        add(card);
    }

    private void handleVerification() {
        String code = codeField.getValue();

        if (code.isBlank()) {
            Toast.show(I18n.t("validation.verificationCode.required"), NotificationVariant.LUMO_ERROR);
            return;
        }

        if (code.length() != 6) {
            Toast.show(I18n.t("validation.verificationCode.length"), NotificationVariant.LUMO_ERROR);
            return;
        }

        verifyButton.setEnabled(false);
        verifyButton.setText(I18n.t("common.verifying"));

        try {
            VerifyAccountRequestDto verifyAccountRequest = new VerifyAccountRequestDto(code);
            authService.verifyAccount(verifyAccountRequest);
            Toast.show(I18n.t("auth.verify.success"), NotificationVariant.LUMO_SUCCESS);

            UI ui = UI.getCurrent();
            DelayedNavigation.navigate(ui, LoginView.class, Duration.ofSeconds(2));

        } catch (ApiException e) {
            Toast.show(e.getMessage(), NotificationVariant.LUMO_ERROR);
            log.error("API error during verification: {}", e.getMessage());
        } finally {
            verifyButton.setEnabled(true);
            verifyButton.setText(I18n.t("auth.verify.submit"));
        }
    }

    private void handleResendCode() {
        String email = emailField.getValue();

        if (email == null || email.isBlank()) {
            Toast.show(I18n.t("validation.email.required"), NotificationVariant.LUMO_ERROR);
            return;
        }

        resendButton.setEnabled(false);
        resendButton.setText(I18n.t("common.sending"));

        try {
            authService.resendVerificationCode(email);
            Toast.show(I18n.t("auth.verify.resent"), NotificationVariant.LUMO_SUCCESS);
            codeField.focus();
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                Toast.show(I18n.t("auth.verify.resendUnavailable"), NotificationVariant.LUMO_WARNING);
            } else {
                Toast.show(e.getMessage(), NotificationVariant.LUMO_ERROR);
            }
            log.error("API error during resend: {}", e.getMessage());
        } finally {
            resendButton.setEnabled(true);
            resendButton.setText(I18n.t("auth.verify.resend"));
        }
    }

    @Override
    public String getPageTitle() {
        return I18n.t("pages.verifyAccount");
    }
}
