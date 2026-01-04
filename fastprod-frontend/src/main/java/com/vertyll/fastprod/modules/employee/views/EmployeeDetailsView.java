package com.vertyll.fastprod.modules.employee.views;

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
import com.vaadin.flow.router.*;
import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.modules.employee.service.EmployeeService;
import com.vertyll.fastprod.shared.components.DetailsTableComponent;
import com.vertyll.fastprod.shared.components.LoadingSpinner;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@Route(value = "employees/details/:id", layout = MainLayout.class)
@PageTitle("Employee Details | FastProd")
@Slf4j
public class EmployeeDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final transient EmployeeService employeeService;
    private final H2 titleLabel = new H2();
    private final DetailsTableComponent detailsTable = new DetailsTableComponent();
    private final LoadingSpinner loadingSpinner = new LoadingSpinner();

    private Long employeeId;

    public EmployeeDetailsView(EmployeeService employeeService) {
        this.employeeService = employeeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("position", "relative");

        createLayout();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        employeeId = event.getRouteParameters().get("id")
                .map(Long::parseLong)
                .orElse(null);

        if (employeeId != null) {
            loadEmployee(employeeId);
        } else {
            Notification.show("Invalid employee ID", 3000, Notification.Position.TOP_CENTER)
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

        Button backButton = new Button("Back to List", VaadinIcon.ARROW_LEFT.create());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClickListener(_ -> navigateToList());

        Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(_ -> navigateToForm(employeeId));

        Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(_ -> confirmDelete());

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        buttonLayout.add(backButton, new HorizontalLayout(editButton, deleteButton));

        titleLabel.getStyle()
                .set("margin-top", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-s)");

        header.add(buttonLayout, titleLabel);
        return header;
    }

    private void loadEmployee(Long id) {
        loadingSpinner.show();
        try {
            ApiResponse<EmployeeResponseDto> response = employeeService.getEmployee(id);
            if (response.data() != null) {
                EmployeeResponseDto employee = response.data();
                displayEmployee(employee);
            }
        } catch (Exception e) {
            log.error("Failed to load employee", e);
            Notification.show("Failed to load employee: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            navigateToList();
        } finally {
            loadingSpinner.hide();
        }
    }

    private void displayEmployee(EmployeeResponseDto employee) {
        titleLabel.setText(employee.firstName() + " " + employee.lastName());

        detailsTable.clear();
        detailsTable.addRow("ID", String.valueOf(employee.id()));
        detailsTable.addRow("First Name", employee.firstName());
        detailsTable.addRow("Last Name", employee.lastName());
        detailsTable.addRow("Email", employee.email());
        detailsTable.addRow("Roles", String.join(", ", employee.roles()));

        Span statusBadge = new Span(employee.isVerified() ? "Verified" : "Not Verified");
        statusBadge.getElement().getThemeList().clear();
        statusBadge.getElement().getThemeList().add("badge");
        if (employee.isVerified()) {
            statusBadge.getElement().getThemeList().add("success");
        } else {
            statusBadge.getElement().getThemeList().add("error");
        }
        detailsTable.addRow("Status", statusBadge);
    }

    private void navigateToForm(Long employeeId) {
        UI.getCurrent().navigate("employees/form/" + employeeId);
    }

    private void confirmDelete() {
        com.vaadin.flow.component.confirmdialog.ConfirmDialog dialog = new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
        dialog.setHeader("Delete Employee");
        dialog.setText("Are you sure you want to delete this employee? This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(_ -> deleteEmployee());
        dialog.open();
    }

    private void deleteEmployee() {
        try {
            employeeService.deleteEmployee(employeeId);
            Notification.show("Employee deleted successfully", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            navigateToList();
        } catch (Exception e) {
            log.error("Failed to delete employee", e);
            Notification.show("Failed to delete employee: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void navigateToList() {
        UI.getCurrent().navigate(EmployeeListView.class);
    }
}
