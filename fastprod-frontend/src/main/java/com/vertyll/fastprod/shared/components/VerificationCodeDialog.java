package com.vertyll.fastprod.shared.components;

import java.io.Serial;

import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializableBiConsumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class VerificationCodeDialog extends Dialog {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String VERIFY_BUTTON_TEXT_KEY = "verification.submit";

    private final TextField codeField;
    private final Button verifyButton;
    private final SerializableBiConsumer<String, VerificationCodeDialog> onVerify;

    public VerificationCodeDialog(
        String title,
        String description,
        SerializableBiConsumer<String, VerificationCodeDialog> onVerify
    ) {
        super();
        this.onVerify = onVerify;

        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        H3 titleHeader = new H3(title);
        titleHeader.getStyle()
            .set("margin", "0 0 var(--lumo-space-m) 0")
            .set("color", "var(--lumo-primary-text-color)");

        Paragraph descriptionText = new Paragraph(description);
        descriptionText.getStyle()
            .set("margin", "0 0 var(--lumo-space-l) 0")
            .set("color", "var(--lumo-secondary-text-color)");

        codeField = new TextField(I18n.t("verification.code"));
        codeField.setWidthFull();
        codeField.setPrefixComponent(VaadinIcon.KEY.create());
        codeField.setPlaceholder(I18n.t("verification.codePlaceholder"));
        codeField.setMaxLength(6);
        codeField.setPattern("[0-9]*");
        codeField.setAutofocus(true);
        codeField.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("text-align", "center");

        verifyButton = new Button(I18n.t(VERIFY_BUTTON_TEXT_KEY), VaadinIcon.CHECK.create());
        verifyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        verifyButton.setWidthFull();
        verifyButton.addClickListener(_ -> handleVerification());

        Button cancelButton = new Button(I18n.t("common.cancel"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.setWidthFull();
        cancelButton.addClickListener(_ -> close());

        layout.add(titleHeader, descriptionText, codeField, verifyButton, cancelButton);

        add(layout);
    }

    private void handleVerification() {
        String code = codeField.getValue();

        if (code == null || code.isBlank()) {
            showNotification(I18n.t("validation.verificationCode.required"), NotificationVariant.LUMO_ERROR);
            return;
        }

        if (code.length() != 6) {
            showNotification(I18n.t("validation.verificationCode.length"), NotificationVariant.LUMO_ERROR);
            return;
        }

        verifyButton.setEnabled(false);
        verifyButton.setText(I18n.t("common.verifying"));

        try {
            onVerify.accept(code, this);
        } catch (ApiException e) {
            log.error("Verification failed", e);
            verifyButton.setEnabled(true);
            verifyButton.setText(I18n.t(VERIFY_BUTTON_TEXT_KEY));
        }
    }

    public void showError(String message) {
        showNotification(message, NotificationVariant.LUMO_ERROR);
        verifyButton.setEnabled(true);
        verifyButton.setText(I18n.t(VERIFY_BUTTON_TEXT_KEY));
        codeField.clear();
        codeField.focus();
    }

    public void showSuccess(String message) {
        showNotification(message, NotificationVariant.LUMO_SUCCESS);
        close();
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}
