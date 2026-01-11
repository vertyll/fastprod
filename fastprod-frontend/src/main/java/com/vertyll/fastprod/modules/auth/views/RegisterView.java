package com.vertyll.fastprod.modules.auth.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import lombok.extern.slf4j.Slf4j;

import com.vertyll.fastprod.modules.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.modules.auth.dto.RegisterRequestDto.FormBuilder;
import com.vertyll.fastprod.modules.auth.service.AuthService;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import com.vertyll.fastprod.shared.exception.ApiException;

@Route("register")
@PageTitle("Sign Up | FastProd")
@Slf4j
public class RegisterView extends VerticalLayout {

    private static final String CREATE_ACCOUNT = "Create Account";
    private static final String COLOR = "color";
    private static final String MARGIN_BOTTOM = "margin-bottom";
    private static final String LUMO_SPACE_M = "var(--lumo-space-m)";

    private final transient AuthService authService;
    private final Binder<FormBuilder> binder;

    private TextField firstNameField;
    private TextField lastNameField;
    private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button registerButton;

    public RegisterView(AuthService authService) {
        this.authService = authService;
        this.binder = new Binder<>(FormBuilder.class);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle()
                .set(
                        "background",
                        "linear-gradient(135deg, var(--lumo-contrast-5pct), var(--lumo-contrast-10pct))")
                .set("padding", "var(--lumo-space-l)");

        createForm();
    }

    private void createForm() {
        Div card = new Div();
        card.addClassName("register-card");
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xl)")
                .set("padding", "var(--lumo-space-xl)")
                .set("max-width", "500px")
                .set("width", "100%");

        H1 title = new H1(CREATE_ACCOUNT);
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxxl)")
                .set("font-weight", "600")
                .set(COLOR, "var(--lumo-primary-text-color)");

        Paragraph subtitle = new Paragraph("Sign up to get started");
        subtitle.getStyle()
                .set("margin", "var(--lumo-space-xs) 0 var(--lumo-space-xl) 0")
                .set(COLOR, "var(--lumo-secondary-text-color)");

        firstNameField = new TextField("First Name");
        firstNameField.setRequiredIndicatorVisible(true);
        firstNameField.setClearButtonVisible(true);
        firstNameField.setWidthFull();

        lastNameField = new TextField("Last Name");
        lastNameField.setRequiredIndicatorVisible(true);
        lastNameField.setClearButtonVisible(true);
        lastNameField.setWidthFull();

        HorizontalLayout nameLayout = new HorizontalLayout(firstNameField, lastNameField);
        nameLayout.setWidthFull();
        nameLayout.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        emailField = new EmailField("Email");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setErrorMessage("Please enter a valid email address");
        emailField.setClearButtonVisible(true);
        emailField.setWidthFull();
        emailField.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        passwordField = new PasswordField("Password");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setHelperText("At least 8 characters with a letter and a digit");
        passwordField.setClearButtonVisible(true);
        passwordField.setWidthFull();
        passwordField.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        confirmPasswordField = new PasswordField("Confirm Password");
        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.setClearButtonVisible(true);
        confirmPasswordField.setWidthFull();
        confirmPasswordField.getStyle().set(MARGIN_BOTTOM, "var(--lumo-space-l)");

        registerButton = new Button(CREATE_ACCOUNT);
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        registerButton.setWidthFull();
        registerButton.addClickListener(_ -> handleRegistration());
        registerButton.getStyle().set(MARGIN_BOTTOM, LUMO_SPACE_M);

        RouterLink loginLink = new RouterLink("Sign in", LoginView.class);
        loginLink
                .getStyle()
                .set(COLOR, "var(--lumo-primary-color)")
                .set("text-decoration", "none")
                .set("font-weight", "500");

        Div loginContainer = new Div();
        loginContainer.getStyle().set("text-align", "center").set("margin-top", LUMO_SPACE_M);
        loginContainer.add(new Span("Already have an account? "), loginLink);

        configureBinder();

        card.add(
                title,
                subtitle,
                nameLayout,
                emailField,
                passwordField,
                confirmPasswordField,
                registerButton,
                loginContainer);

        add(card);
    }

    private void configureBinder() {
        binder.forField(firstNameField)
                .withValidator(new StringLengthValidator("First name is required", 1, null))
                .bind(FormBuilder::getFirstName, FormBuilder::setFirstName);

        binder.forField(lastNameField)
                .withValidator(new StringLengthValidator("Last name is required", 1, null))
                .bind(FormBuilder::getLastName, FormBuilder::setLastName);

        binder.forField(emailField)
                .withValidator(new EmailValidator("Please enter a valid email address"))
                .bind(FormBuilder::getEmail, FormBuilder::setEmail);

        binder.forField(passwordField)
                .withValidator(
                        password ->
                                password != null
                                        && password.matches("^(?=.*\\d)(?=.*[a-zA-Z]).{8,}$"),
                        "Password must be at least 8 characters long and contain a letter and a digit")
                .bind(FormBuilder::getPassword, FormBuilder::setPassword);

        binder.forField(confirmPasswordField)
                .withValidator(
                        confirmPassword -> {
                            String password = passwordField.getValue();
                            return password != null && password.equals(confirmPassword);
                        },
                        "Passwords must match")
                .bind(_ -> passwordField.getValue(), (_, _) -> {});
    }

    private void handleRegistration() {
        try {
            FormBuilder form = new FormBuilder();
            binder.writeBean(form);

            RegisterRequestDto registerRequest = form.toDto();

            registerButton.setEnabled(false);
            registerButton.setText("Signing up...");

            ApiResponse<Void> response = authService.register(registerRequest);

            String message =
                    response.message() != null
                            ? response.message()
                            : "Registration successful! Please check your email for verification code.";
            showNotification(message, NotificationVariant.LUMO_SUCCESS);

            String email = registerRequest.email();

            UI.getCurrent().navigate(VerifyAccountView.class, email);

        } catch (ValidationException e) {
            log.error("Validation error during registration", e);
        } catch (ApiException e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
            log.error(
                    "API error during registration: {} (status: {})",
                    e.getMessage(),
                    e.getStatusCode());
        } catch (Exception e) {
            showNotification(
                    "An unexpected error occurred. Please try again.",
                    NotificationVariant.LUMO_ERROR);
            log.error("Unexpected error during registration", e);
        } finally {
            registerButton.setEnabled(true);
            registerButton.setText(CREATE_ACCOUNT);
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
