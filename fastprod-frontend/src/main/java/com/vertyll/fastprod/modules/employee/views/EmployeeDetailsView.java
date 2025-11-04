package com.vertyll.fastprod.modules.employee.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
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
import com.vertyll.fastprod.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@Route(value = "employees/details/:id", layout = MainLayout.class)
@PageTitle("Employee Details | FastProd")
@Slf4j
public class EmployeeDetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final EmployeeService employeeService;
    private Long employeeId;

    private final H2 titleLabel = new H2();
    private final Span idLabel = new Span();
    private final Span firstNameLabel = new Span();
    private final Span lastNameLabel = new Span();
    private final Span emailLabel = new Span();
    private final Span rolesLabel = new Span();
    private final Span statusLabel = new Span();

    public EmployeeDetailsView(EmployeeService employeeService) {
        this.employeeService = employeeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

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
        add(createDetailsTable());
    }

    private VerticalLayout createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);

        titleLabel.getStyle().set("margin", "0");

        Button backButton = new Button("Back to List", VaadinIcon.ARROW_LEFT.create());
        backButton.addClickListener(e -> navigateToList());

        Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e -> navigateToForm(employeeId));

        Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDelete());

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        buttonLayout.add(backButton, new HorizontalLayout(editButton, deleteButton));

        header.add(titleLabel, buttonLayout);
        return header;
    }

    private Div createDetailsTable() {
        Div table = new Div();
        table.getStyle()
                .set("display", "table")
                .set("width", "100%")
                .set("max-width", "600px")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        table.add(
                createTableRow("ID", idLabel),
                createTableRow("First Name", firstNameLabel),
                createTableRow("Last Name", lastNameLabel),
                createTableRow("Email", emailLabel),
                createTableRow("Roles", rolesLabel),
                createTableRow("Status", statusLabel));

        return table;
    }

    private Div createTableRow(String label, Span valueSpan) {
        Div row = new Div();
        row.getStyle()
                .set("display", "table-row");

        Div labelCell = new Div();
        labelCell.setText(label);
        labelCell.getStyle()
                .set("display", "table-cell")
                .set("padding", "var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("font-weight", "500")
                .set("width", "30%")
                .set("background", "var(--lumo-contrast-5pct)");

        Div valueCell = new Div();
        valueCell.add(valueSpan);
        valueCell.getStyle()
                .set("display", "table-cell")
                .set("padding", "var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        row.add(labelCell, valueCell);
        return row;
    }

    private void loadEmployee(Long id) {
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
        }
    }

    private void displayEmployee(EmployeeResponseDto employee) {
        titleLabel.setText(employee.firstName() + " " + employee.lastName());
        idLabel.setText(String.valueOf(employee.id()));
        firstNameLabel.setText(employee.firstName());
        lastNameLabel.setText(employee.lastName());
        emailLabel.setText(employee.email());

        rolesLabel.setText(String.join(", ", employee.roles()));

        statusLabel.setText(employee.isVerified() ? "Verified" : "Not Verified");
        statusLabel.getElement().getThemeList().clear();
        statusLabel.getElement().getThemeList().add("badge");
        if (employee.isVerified()) {
            statusLabel.getElement().getThemeList().add("success");
        } else {
            statusLabel.getElement().getThemeList().add("error");
        }
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
        dialog.addConfirmListener(event -> deleteEmployee());
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
