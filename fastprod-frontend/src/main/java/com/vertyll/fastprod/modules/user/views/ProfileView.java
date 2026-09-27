package com.vertyll.fastprod.modules.user.views;

import java.io.Serial;

import jakarta.annotation.security.PermitAll;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.user.dto.ProfileUpdateDto;
import com.vertyll.fastprod.modules.user.dto.UserProfileDto;
import com.vertyll.fastprod.modules.user.service.UserService;
import com.vertyll.fastprod.shared.components.DetailsTableComponent;
import com.vertyll.fastprod.shared.components.LoadingSpinner;
import com.vertyll.fastprod.shared.components.Toast;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import lombok.extern.slf4j.Slf4j;

@Route(value = "profile", layout = MainLayout.class)
@PermitAll
@Slf4j
public final class ProfileView extends VerticalLayout implements HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient UserService userService;
    private final Binder<ProfileUpdateDto> binder;
    private final LoadingSpinner loadingSpinner;

    private transient @Nullable UserProfileDto currentUser;
    private final TextField firstNameField = new TextField(I18n.t("common.firstName"));
    private final TextField lastNameField = new TextField(I18n.t("common.lastName"));
    private DetailsTableComponent detailsTable;
    private Div editFormContainer;
    private Div detailsContainer;

    public ProfileView(UserService userService) {
        super();
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
        H2 title = new H2(I18n.t("profile.title"));
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        Button changePasswordBtn = new Button(I18n.t("account.password.title"), VaadinIcon.KEY.create());
        changePasswordBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        changePasswordBtn.addClickListener(_ -> UI.getCurrent().navigate("profile/change-password"));

        Button changeEmailBtn = new Button(I18n.t("account.email.title"), VaadinIcon.ENVELOPE.create());
        changeEmailBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        changeEmailBtn.addClickListener(_ -> UI.getCurrent().navigate("profile/change-email"));

        Button editBtn = new Button(I18n.t("profile.edit"), VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editBtn.addClickListener(_ -> showEditForm());

        HorizontalLayout actions = new HorizontalLayout(changePasswordBtn, changeEmailBtn, editBtn);
        actions.setSpacing(true);
        actions.getStyle().set("flex-wrap", "wrap");

        HorizontalLayout header = new HorizontalLayout(title, actions);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("flex-wrap", "wrap");
        header.addClassName("page-header");

        add(header);
    }

    private void createContent() {
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

        editFormContainer = createEditForm();
        editFormContainer.setVisible(false);

        add(detailsContainer, editFormContainer);
    }

    private Div createEditForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        firstNameField.setRequiredIndicatorVisible(true);
        lastNameField.setRequiredIndicatorVisible(true);

        binder.forField(firstNameField)
            .asRequired(I18n.t("validation.firstName.required"))
            .bind(ProfileUpdateDto::firstName, (_, _) -> {
            });

        binder.forField(lastNameField)
            .asRequired(I18n.t("validation.lastName.required"))
            .bind(ProfileUpdateDto::lastName, (_, _) -> {
            });

        formLayout.add(firstNameField, lastNameField);

        Button saveButton = new Button(I18n.t("common.save"));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(_ -> handleSave());

        Button cancelButton = new Button(I18n.t("common.cancel"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButton.addClickListener(_ -> hideEditForm());

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
            UserProfileDto user = userService.getCurrentUser();
            currentUser = user;
            updateDetailsView(user);
        } catch (ApiException e) {
            log.error("Failed to load user profile", e);
            Toast.show(I18n.t("profile.loadFailed"), NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateDetailsView(UserProfileDto user) {
        detailsTable.removeAll();
        detailsTable.addRow(I18n.t("common.firstName"), user.firstName());
        detailsTable.addRow(I18n.t("common.lastName"), user.lastName());
        detailsTable.addRow(I18n.t("common.email"), user.email());

        String rolesText = I18n.roles(user.roles());
        detailsTable.addRow(I18n.t("common.roles"), rolesText);

        Span verifiedBadge = new Span(I18n.t(user.isVerified() ? "common.verified" : "common.notVerified"));
        verifiedBadge.getElement().getThemeList().add(user.isVerified() ? "badge success" : "badge error");
        detailsTable.addRow(I18n.t("common.status"), verifiedBadge);
    }

    private void showEditForm() {
        UserProfileDto user = currentUser;
        if (user == null) {
            return;
        }
        firstNameField.setValue(user.firstName());
        lastNameField.setValue(user.lastName());
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
            ProfileUpdateDto dto = new ProfileUpdateDto(firstNameField.getValue(), lastNameField.getValue());

            if (binder.validate().isOk()) {
                UserProfileDto user = userService.updateProfile(dto);
                currentUser = user;
                updateDetailsView(user);
                hideEditForm();
                Toast.show(I18n.t("profile.updated"), NotificationVariant.LUMO_SUCCESS);
            }
        } catch (ApiException e) {
            log.error("Failed to update profile", e);
            Toast.show(I18n.t("profile.updateFailed"), NotificationVariant.LUMO_ERROR);
        } finally {
            loadingSpinner.hide();
        }
    }

    @Override
    public String getPageTitle() {
        return I18n.t("pages.profile");
    }
}
