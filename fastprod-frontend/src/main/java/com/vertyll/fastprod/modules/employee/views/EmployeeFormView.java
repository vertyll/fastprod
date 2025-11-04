package com.vertyll.fastprod.modules.employee.views;

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
import com.vaadin.flow.router.*;
import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.employee.dto.EmployeeCreateDto;
import com.vertyll.fastprod.modules.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.modules.employee.dto.EmployeeUpdateDto;
import com.vertyll.fastprod.modules.employee.service.EmployeeService;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Route(value = "employees/form/:id?", layout = MainLayout.class)
@PageTitle("Employee Form | FastProd")
@Slf4j
public class EmployeeFormView extends VerticalLayout implements BeforeEnterObserver {

    private final EmployeeService employeeService;
    private final Binder<EmployeeFormData> binder;

    private TextField firstNameField;
    private TextField lastNameField;
    private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private MultiSelectComboBox<String> rolesField;
    private Button saveButton;
    private Button cancelButton;

    private Long employeeId;
    private boolean isEditMode = false;

    public EmployeeFormView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.binder = new Binder<>(EmployeeFormData.class);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createForm();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        employeeId = event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .orElse(null);

        isEditMode = employeeId != null;

        if (isEditMode) {
            loadEmployee(employeeId);
            passwordField.setLabel("New Password (leave empty to keep current)");
            passwordField.setRequiredIndicatorVisible(false);
            confirmPasswordField.setLabel("Confirm New Password");
            confirmPasswordField.setRequiredIndicatorVisible(false);
        } else {
            passwordField.setLabel("Password");
            passwordField.setRequiredIndicatorVisible(true);
            confirmPasswordField.setLabel("Confirm Password");
            confirmPasswordField.setRequiredIndicatorVisible(true);
        }
    }

    private void createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        firstNameField = new TextField("First Name");
        firstNameField.setRequiredIndicatorVisible(true);

        lastNameField = new TextField("Last Name");
        lastNameField.setRequiredIndicatorVisible(true);

        emailField = new EmailField("Email");
        emailField.setRequiredIndicatorVisible(true);

        rolesField = new MultiSelectComboBox<>("Roles");
        rolesField.setItems("EMPLOYEE", "ADMIN", "MANAGER");
        rolesField.select("EMPLOYEE");
        rolesField.setPlaceholder("Select roles...");
        rolesField.setRequiredIndicatorVisible(true);

        passwordField = new PasswordField("Password");
        passwordField.setRequiredIndicatorVisible(true);

        confirmPasswordField = new PasswordField("Confirm Password");
        confirmPasswordField.setRequiredIndicatorVisible(true);

        formLayout.add(firstNameField, lastNameField, emailField, rolesField, passwordField, confirmPasswordField);

        // Bind fields
        binder.forField(firstNameField)
                .asRequired("First name is required")
                .bind(EmployeeFormData::getFirstName, EmployeeFormData::setFirstName);

        binder.forField(lastNameField)
                .asRequired("Last name is required")
                .bind(EmployeeFormData::getLastName, EmployeeFormData::setLastName);

        binder.forField(emailField)
                .asRequired("Email is required")
                .withValidator(new EmailValidator("Invalid email format"))
                .bind(EmployeeFormData::getEmail, EmployeeFormData::setEmail);

        binder.forField(passwordField)
                .withValidator(pass -> {
                    if (isEditMode) {
                        return pass == null || pass.isEmpty() || pass.length() >= 6;
                    }
                    return pass != null && !pass.isEmpty() && pass.length() >= 6;
                }, "Password must be at least 6 characters")
                .bind(EmployeeFormData::getPassword, EmployeeFormData::setPassword);

        passwordField.addValueChangeListener(e -> confirmPasswordField.setValue(""));
        confirmPasswordField.addValueChangeListener(e -> {
            String password = passwordField.getValue();
            String confirm = confirmPasswordField.getValue();
            if (confirm != null && !confirm.isEmpty() && !confirm.equals(password)) {
                confirmPasswordField.setErrorMessage("Passwords must match");
                confirmPasswordField.setInvalid(true);
            } else {
                confirmPasswordField.setInvalid(false);
            }
        });

        binder.forField(rolesField)
                .bind(EmployeeFormData::getRoleNames, EmployeeFormData::setRoleNames);

        saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveEmployee());

        cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> navigateToList());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        add(formLayout, buttonLayout);
    }

    private void loadEmployee(Long id) {
        try {
            ApiResponse<EmployeeResponseDto> response = employeeService.getEmployee(id);
            if (response.data() != null) {
                EmployeeResponseDto employee = response.data();
                EmployeeFormData formData = new EmployeeFormData();
                formData.setFirstName(employee.firstName());
                formData.setLastName(employee.lastName());
                formData.setEmail(employee.email());
                formData.setRoleNames(new HashSet<>(employee.roles()));

                binder.readBean(formData);
            }
        } catch (Exception e) {
            log.error("Failed to load employee", e);
            Notification.show("Failed to load employee: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateToList();
        }
    }

    private void saveEmployee() {
        try {
            EmployeeFormData formData = new EmployeeFormData();
            binder.writeBean(formData);

            if (formData.getPassword() != null && !formData.getPassword().isEmpty()) {
                String confirmPass = confirmPasswordField.getValue();
                if (confirmPass == null || !formData.getPassword().equals(confirmPass)) {
                    Notification.show("Passwords must match", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    confirmPasswordField.setInvalid(true);
                    return;
                }
            }

            if (isEditMode) {
                // Only send password if it's not empty
                String passwordToSend = (formData.getPassword() != null && !formData.getPassword().isEmpty())
                        ? formData.getPassword()
                        : null;

                EmployeeUpdateDto updateDto = new EmployeeUpdateDto(
                        formData.getFirstName(),
                        formData.getLastName(),
                        formData.getEmail(),
                        passwordToSend,
                        formData.getRoleNames());
                employeeService.updateEmployee(employeeId, updateDto);
                Notification.show("Employee updated successfully", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                EmployeeCreateDto createDto = new EmployeeCreateDto(
                        formData.getFirstName(),
                        formData.getLastName(),
                        formData.getEmail(),
                        formData.getPassword(),
                        formData.getRoleNames());
                employeeService.createEmployee(createDto);
                Notification.show("Employee created successfully", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            navigateToList();
        } catch (ValidationException e) {
            log.error("Validation failed", e);
            Notification.show("Please fix validation errors", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            log.error("Failed to save employee", e);
            Notification.show("Failed to save employee: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void navigateToList() {
        UI.getCurrent().navigate(EmployeeListView.class);
    }

    public static class EmployeeFormData {
        private String firstName;
        private String lastName;
        private String email;
        private String password;
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
}
