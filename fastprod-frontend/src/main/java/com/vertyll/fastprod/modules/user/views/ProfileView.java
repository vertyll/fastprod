package com.vertyll.fastprod.modules.user.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.user.dto.ProfileUpdateDto;
import com.vertyll.fastprod.modules.user.dto.UserProfileDto;
import com.vertyll.fastprod.modules.user.service.UserService;
import com.vertyll.fastprod.shared.components.DetailsTableComponent;
import com.vertyll.fastprod.shared.components.LoadingSpinner;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("My Profile | FastProd")
@PermitAll
@Slf4j
public class ProfileView extends VerticalLayout {

    private final UserService userService;
    private final Binder<ProfileUpdateDto> binder;
    private final LoadingSpinner loadingSpinner;

    private UserProfileDto currentUser;
    private TextField firstNameField;
    private TextField lastNameField;
    private DetailsTableComponent detailsTable;
    private Div editFormContainer;
    private Div detailsContainer;

    public ProfileView(UserService userService) {
        this.userService = userService;
        this.binder = new Binder<>(ProfileUpdateDto.class);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        loadingSpinner = new LoadingSpinner();
        getStyle().set("position", "relative");

        createHeader();
        createContent();
        loadUserProfile();

        add(loadingSpinner);
    }

    private void createHeader() {
        H2 title = new H2("My Profile");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        Button changePasswordBtn = new Button("Change Password", VaadinIcon.KEY.create());
        changePasswordBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        changePasswordBtn.addClickListener(e -> UI.getCurrent().navigate("profile/change-password"));

        Button changeEmailBtn = new Button("Change Email", VaadinIcon.ENVELOPE.create());
        changeEmailBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        changeEmailBtn.addClickListener(e -> UI.getCurrent().navigate("profile/change-email"));

        Button editBtn = new Button("Edit Profile", VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editBtn.addClickListener(e -> showEditForm());

        HorizontalLayout actions = new HorizontalLayout(changePasswordBtn, changeEmailBtn, editBtn);
        actions.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(title, actions);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        add(header);
    }

    private void createContent() {
        // Details view
        detailsTable = new DetailsTableComponent();

        VerticalLayout detailsLayout = new VerticalLayout(detailsTable);
        detailsLayout.setSpacing(true);
        detailsLayout.setPadding(true);
        detailsLayout.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        detailsContainer = new Div(detailsLayout);
        detailsContainer.setWidthFull();

        // Edit form (initially hidden)
        editFormContainer = createEditForm();
        editFormContainer.setVisible(false);

        add(detailsContainer, editFormContainer);
    }

    private Div createEditForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        firstNameField = new TextField("First Name");
        firstNameField.setRequiredIndicatorVisible(true);

        lastNameField = new TextField("Last Name");
        lastNameField.setRequiredIndicatorVisible(true);

        binder.forField(firstNameField)
                .asRequired("First name is required")
                .bind(ProfileUpdateDto::firstName, (dto, value) -> {
                });

        binder.forField(lastNameField)
                .asRequired("Last name is required")
                .bind(ProfileUpdateDto::lastName, (dto, value) -> {
                });

        formLayout.add(firstNameField, lastNameField);

        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> handleSave());

        Button cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(e -> hideEditForm());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setSpacing(true);

        VerticalLayout form = new VerticalLayout(formLayout, buttons);
        form.setSpacing(true);
        form.setPadding(true);
        form.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        Div container = new Div(form);
        container.setWidthFull();
        return container;
    }

    private void loadUserProfile() {
        try {
            ApiResponse<UserProfileDto> response = userService.getCurrentUser();
            if (response.data() != null) {
                currentUser = response.data();
                updateDetailsView();
            }
        } catch (Exception e) {
            log.error("Failed to load user profile", e);
            showNotification("Failed to load profile", NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateDetailsView() {
        detailsTable.removeAll();
        detailsTable.addRow("First Name", currentUser.firstName());
        detailsTable.addRow("Last Name", currentUser.lastName());
        detailsTable.addRow("Email", currentUser.email());

        String rolesText = String.join(", ", currentUser.roles());
        detailsTable.addRow("Roles", rolesText);

        Span verifiedBadge = new Span(currentUser.isVerified() ? "Verified" : "Not Verified");
        verifiedBadge.getElement().getThemeList().add(
                currentUser.isVerified() ? "badge success" : "badge error"
        );
        detailsTable.addRow("Status", verifiedBadge);
    }

    private void showEditForm() {
        firstNameField.setValue(currentUser.firstName());
        lastNameField.setValue(currentUser.lastName());
        detailsContainer.setVisible(false);
        editFormContainer.setVisible(true);
    }

    private void hideEditForm() {
        editFormContainer.setVisible(false);
        detailsContainer.setVisible(true);
        binder.readBean(null);
    }

    private void handleSave() {
        loadingSpinner.show();
        try {
            ProfileUpdateDto dto = new ProfileUpdateDto(
                    firstNameField.getValue(),
                    lastNameField.getValue()
            );

            if (binder.validate().isOk()) {
                ApiResponse<UserProfileDto> response = userService.updateProfile(dto);
                if (response.data() != null) {
                    currentUser = response.data();
                    updateDetailsView();
                    hideEditForm();
                    showNotification("Profile updated successfully", NotificationVariant.LUMO_SUCCESS);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update profile", e);
            showNotification("Failed to update profile", NotificationVariant.LUMO_ERROR);
        } finally {
            loadingSpinner.hide();
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
