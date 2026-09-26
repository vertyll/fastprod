package com.vertyll.fastprod.modules.employee.views;

import java.io.Serial;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.modules.employee.service.EmployeeService;
import com.vertyll.fastprod.shared.components.DetailsTableComponent;
import com.vertyll.fastprod.shared.components.LoadingSpinner;
import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import lombok.extern.slf4j.Slf4j;

@Route(value = "employees/details/:id", layout = MainLayout.class)
@Slf4j
public final class EmployeeDetailsView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient EmployeeService employeeService;
    private final H2 titleLabel = new H2();
    private final DetailsTableComponent detailsTable = new DetailsTableComponent();
    private final LoadingSpinner loadingSpinner = new LoadingSpinner();

    private @Nullable Long employeeId;

    public EmployeeDetailsView(EmployeeService employeeService) {
        super();
        this.employeeService = employeeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("position", "relative");

        createLayout();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long id = event.getRouteParameters().get("id").map(Long::parseLong).orElse(null);
        employeeId = id;

        if (id != null) {
            loadEmployee(id);
        } else {
            Notification.show(I18n.t("employees.invalidId"), 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateToList();
        }
    }

    private void createLayout() {
        add(createHeader());
        add(detailsTable);
        add(loadingSpinner);
    }

    private VerticalLayout createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(true);

        Button backButton = new Button(I18n.t("employees.backToList"), VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(_ -> navigateToList());

        Button editButton = new Button(I18n.t("common.edit"), VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(_ -> {
            Long id = employeeId;
            if (id != null) {
                navigateToForm(id);
            }
        });

        Button deleteButton = new Button(I18n.t("common.delete"), VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(_ -> confirmDelete());

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        buttonLayout.getStyle().set("flex-wrap", "wrap");
        buttonLayout.addClassName("page-header");
        buttonLayout.add(backButton, new HorizontalLayout(editButton, deleteButton));

        titleLabel.getStyle().set("margin-top", "var(--lumo-space-m)").set("margin-bottom", "var(--lumo-space-s)");

        header.add(buttonLayout, titleLabel);
        return header;
    }

    private void loadEmployee(Long id) {
        loadingSpinner.show();
        try {
            EmployeeResponseDto employee = employeeService.getEmployee(id);
            displayEmployee(employee);
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
        } finally {
            loadingSpinner.hide();
        }
    }

    private void displayEmployee(EmployeeResponseDto employee) {
        titleLabel.setText(employee.firstName() + " " + employee.lastName());

        detailsTable.clear();
        detailsTable.addRow(I18n.t("common.id"), String.valueOf(employee.id()));
        detailsTable.addRow(I18n.t("common.firstName"), employee.firstName());
        detailsTable.addRow(I18n.t("common.lastName"), employee.lastName());
        detailsTable.addRow(I18n.t("common.email"), employee.email());
        detailsTable.addRow(I18n.t("common.roles"), I18n.roles(employee.roles()));

        Span statusBadge = new Span(employee.isVerified() ? I18n.t("common.verified") : I18n.t("common.notVerified"));
        statusBadge.getElement().getThemeList().clear();
        statusBadge.getElement().getThemeList().add("badge");
        if (employee.isVerified()) {
            statusBadge.getElement().getThemeList().add("success");
        } else {
            statusBadge.getElement().getThemeList().add("error");
        }
        detailsTable.addRow(I18n.t("common.status"), statusBadge);
    }

    private void navigateToForm(Long employeeId) {
        UI.getCurrent().navigate("employees/form/" + employeeId);
    }

    private void confirmDelete() {
        com.vaadin.flow.component.confirmdialog.ConfirmDialog dialog =
                new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
        dialog.setHeader(I18n.t("employees.delete.title"));
        dialog.setText(I18n.t("employees.delete.confirmThis"));
        dialog.setCancelable(true);
        dialog.setConfirmText(I18n.t("common.delete"));
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(_ -> deleteEmployee());
        dialog.open();
    }

    private void deleteEmployee() {
        Long id = employeeId;
        if (id == null) {
            return;
        }
        try {
            employeeService.deleteEmployee(id);
            Notification.show(I18n.t("employees.delete.success"), 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            navigateToList();
        } catch (ApiException e) {
            log.error("Failed to delete employee", e);
            Notification
                .show(
                    I18n.t("employees.deleteFailed", Map.of("reason", I18n.error(e))),
                    3000,
                    Notification.Position.TOP_CENTER
                )
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void navigateToList() {
        UI.getCurrent().navigate(EmployeeListView.class);
    }

    @Override
    public String getPageTitle() {
        return I18n.t("pages.employeeDetails");
    }
}
