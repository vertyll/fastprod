package com.vertyll.fastprod.modules.employee.views;

import java.io.Serial;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.security.RolesAllowed;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.modules.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.modules.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.modules.employee.service.EmployeeService;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import lombok.extern.slf4j.Slf4j;

@Route(value = "employees/form/:id?", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Slf4j
public final class EmployeeFormView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient EmployeeService employeeService;
    private final Binder<EmployeeFormData> binder;

    private final PasswordField passwordField = new PasswordField(I18n.t("common.password"));
    private final PasswordField confirmPasswordField = new PasswordField(I18n.t("common.confirmPassword"));
    private @Nullable Long employeeId;

    public EmployeeFormView(EmployeeService employeeService) {
        super();
        this.employeeService = employeeService;
        this.binder = new Binder<>(EmployeeFormData.class);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createForm();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long id = event.getRouteParameters().get("id").map(Long::parseLong).orElse(null);
        employeeId = id;

        if (id != null) {
            loadEmployee(id);
            passwordField.setLabel(I18n.t("employees.form.newPasswordOptional"));
            passwordField.setRequiredIndicatorVisible(false);
            confirmPasswordField.setLabel(I18n.t("common.confirmNewPassword"));
            confirmPasswordField.setRequiredIndicatorVisible(false);
        } else {
            passwordField.setLabel(I18n.t("common.password"));
            passwordField.setRequiredIndicatorVisible(true);
            confirmPasswordField.setLabel(I18n.t("common.confirmPassword"));
            confirmPasswordField.setRequiredIndicatorVisible(true);
        }
    }

    private void createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        TextField firstNameField = new TextField(I18n.t("common.firstName"));
        firstNameField.setRequiredIndicatorVisible(true);

        TextField lastNameField = new TextField(I18n.t("common.lastName"));
        lastNameField.setRequiredIndicatorVisible(true);

        EmailField emailField = new EmailField(I18n.t("common.email"));
        emailField.setRequiredIndicatorVisible(true);

        MultiSelectComboBox<String> rolesField = new MultiSelectComboBox<>(I18n.t("common.roles"));
        rolesField.setItems("EMPLOYEE", "ADMIN", "MANAGER");
        rolesField.select("EMPLOYEE");
        rolesField.setItemLabelGenerator(I18n::role);
        rolesField.setPlaceholder(I18n.t("employees.form.selectRoles"));
        rolesField.setRequiredIndicatorVisible(true);

        passwordField.setRequiredIndicatorVisible(true);

        confirmPasswordField.setRequiredIndicatorVisible(true);

        formLayout.add(firstNameField, lastNameField, emailField, rolesField, passwordField, confirmPasswordField);

        binder.forField(firstNameField)
            .asRequired(I18n.t("validation.firstName.required"))
            .bind(EmployeeFormData::getFirstName, EmployeeFormData::setFirstName);

        binder.forField(lastNameField)
            .asRequired(I18n.t("validation.lastName.required"))
            .bind(EmployeeFormData::getLastName, EmployeeFormData::setLastName);

        binder.forField(emailField)
            .asRequired(I18n.t("validation.email.required"))
            .withValidator(new EmailValidator(I18n.t("validation.email.invalid")))
            .bind(EmployeeFormData::getEmail, EmployeeFormData::setEmail);

        binder.forField(passwordField).withValidator(pass -> {
            if (employeeId != null) {
                return pass == null || pass.isEmpty() || pass.length() >= 6;
            }
            return pass != null && pass.length() >= 6;
        }, I18n.t("validation.password.minLength", Map.of("min", 6)))
            .bind(EmployeeFormData::getPassword, EmployeeFormData::setPassword);

        passwordField.addValueChangeListener(_ -> confirmPasswordField.setValue(""));
        confirmPasswordField.addValueChangeListener(_ -> {
            String password = passwordField.getValue();
            String confirm = confirmPasswordField.getValue();
            if (confirm != null && !confirm.isEmpty() && !confirm.equals(password)) {
                confirmPasswordField.setErrorMessage(I18n.t("validation.password.mismatch"));
                confirmPasswordField.setInvalid(true);
            } else {
                confirmPasswordField.setInvalid(false);
            }
        });

        binder.forField(rolesField).bind(EmployeeFormData::getRoleNames, EmployeeFormData::setRoleNames);

        Button saveButton = new Button(I18n.t("common.save"));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(_ -> saveEmployee());

        Button cancelButton = new Button(I18n.t("common.cancel"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(_ -> navigateToList());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("flex-wrap", "wrap");

        add(formLayout, buttonLayout);
    }

    private void loadEmployee(Long id) {
        try {
            EmployeeResponseDto employee = employeeService.getEmployee(id);
            EmployeeFormData formData = new EmployeeFormData();
            formData.setFirstName(employee.firstName());
            formData.setLastName(employee.lastName());
            formData.setEmail(employee.email());
            formData.setRoleNames(new HashSet<>(employee.roles()));

            binder.readBean(formData);
        } catch (ApiException e) {
            log.error("Failed to load employee", e);
            Notification
                .show(
                    I18n.t("employees.loadFailed", Map.of("reason", I18n.error(e))),
                    3000,
                    Notification.Position.TOP_CENTER
                )
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateToList();
        }
    }

    private void saveEmployee() {
        try {
            EmployeeFormData formData = new EmployeeFormData();
            binder.writeBean(formData);

            if (!formData.getPassword().isEmpty()) {
                String confirmPass = confirmPasswordField.getValue();
                if (!formData.getPassword().equals(confirmPass)) {
                    Notification.show(I18n.t("validation.password.mismatch"), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    confirmPasswordField.setInvalid(true);
                    return;
                }
            }

            Long id = employeeId;
            if (id != null) {
                String passwordToSend = formData.getPassword().isEmpty() ? null : formData.getPassword();

                EmployeeUpdateDto updateDto = new EmployeeUpdateDto(
                    formData.getFirstName(),
                    formData.getLastName(),
                    formData.getEmail(),
                    passwordToSend,
                    formData.getRoleNames()
                );
                employeeService.updateEmployee(id, updateDto);
                Notification.show(I18n.t("employees.form.updated"), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                EmployeeCreateDto createDto = new EmployeeCreateDto(
                    formData.getFirstName(),
                    formData.getLastName(),
                    formData.getEmail(),
                    formData.getPassword(),
                    formData.getRoleNames()
                );
                employeeService.createEmployee(createDto);
                Notification.show(I18n.t("employees.form.created"), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            navigateToList();
        } catch (ValidationException e) {
            log.error("Validation failed", e);
        } catch (ApiException e) {
            log.error("Failed to save employee", e);
            Notification
                .show(
                    I18n.t("employees.saveFailed", Map.of("reason", I18n.error(e))),
                    3000,
                    Notification.Position.TOP_CENTER
                )
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void navigateToList() {
        UI.getCurrent().navigate(EmployeeListView.class);
    }
    @SuppressWarnings("PMD.DataClass")
    public static class EmployeeFormData {
        private String firstName = "";
        private String lastName = "";
        private String email = "";
        private String password = "";
        private Set<String> roleNames = new HashSet<>();

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Set<String> getRoleNames() {
            return roleNames;
        }

        public void setRoleNames(Set<String> roleNames) {
            this.roleNames = roleNames;
        }
    }

    @Override
    public String getPageTitle() {
        return I18n.t("pages.employeeForm");
    }
}
