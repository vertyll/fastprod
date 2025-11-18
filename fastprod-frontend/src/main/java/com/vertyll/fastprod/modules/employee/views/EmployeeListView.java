package com.vertyll.fastprod.modules.employee.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.modules.employee.service.EmployeeService;
import com.vertyll.fastprod.shared.components.LoadingSpinner;
import com.vertyll.fastprod.shared.components.PagedGridComponent;
import com.vertyll.fastprod.shared.dto.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

@Route(value = "employees", layout = MainLayout.class)
@PageTitle("Employees | FastProd")
@RolesAllowed({"ADMIN", "MANAGER"})
@Slf4j
public class EmployeeListView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final PagedGridComponent<EmployeeResponseDto> pagedGrid;
    private final LoadingSpinner loadingSpinner;
    private final String sortBy = "id";
    private final String sortDirection = "ASC";

    public EmployeeListView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.pagedGrid = new PagedGridComponent<>(EmployeeResponseDto.class);
        this.loadingSpinner = new LoadingSpinner();

        setSizeFull();
        setSpacing(true);
        getStyle().set("position", "relative");

        H2 title = new H2("Employees");
        title.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.MEDIUM);
        add(title);

        createToolbar();
        configureGrid();

        pagedGrid.setOnPageChange(this::loadEmployees);
        pagedGrid.setInitialPageSize(10);

        add(pagedGrid);
        add(loadingSpinner);

        loadEmployees(0, 10);
    }

    private void createToolbar() {
        Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshButton.addClickListener(e -> loadEmployees(pagedGrid.getCurrentPage(), pagedGrid.getPageSize()));

        Button addButton = new Button("Add Employee", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> navigateToForm(null));

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSpacing(true);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.add(refreshButton, addButton);

        add(toolbar);
    }

    private void configureGrid() {
        Grid<EmployeeResponseDto> grid = pagedGrid.getGrid();

        grid.addColumn(EmployeeResponseDto::id).setHeader("ID").setSortable(true).setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);
        grid.addColumn(EmployeeResponseDto::firstName).setHeader("First Name").setSortable(true).setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);
        grid.addColumn(EmployeeResponseDto::lastName).setHeader("Last Name").setSortable(true).setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);
        grid.addColumn(EmployeeResponseDto::email).setHeader("Email").setSortable(true).setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(employee -> {
            String roles = String.join(", ", employee.roles());
            Span span = new Span(roles);
            span.getStyle().set("display", "flex").set("justify-content", "center");
            return span;
        }).setHeader("Roles").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(employee -> {
            Span badge = new Span(employee.isVerified() ? "Verified" : "Not Verified");
            badge.getElement().getThemeList().add(employee.isVerified() ? "badge success" : "badge error");
            HorizontalLayout layout = new HorizontalLayout(badge);
            layout.setJustifyContentMode(JustifyContentMode.CENTER);
            layout.setWidthFull();
            return layout;
        }).setHeader("Status").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);

        grid.addComponentColumn(employee -> {
            Button viewButton = new Button(VaadinIcon.EYE.create());
            viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewButton.getElement().setAttribute("title", "View");
            viewButton.getStyle()
                    .set("color", "var(--lumo-contrast)")
                    .set("background", "var(--lumo-contrast-10pct)");
            viewButton.addClickListener(e -> navigateToDetails(employee.id()));

            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Edit");
            editButton.getStyle()
                    .set("color", "var(--lumo-contrast)")
                    .set("background", "var(--lumo-contrast-10pct)");
            editButton.addClickListener(e -> navigateToForm(employee.id()));

            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            deleteButton.getElement().setAttribute("title", "Delete");
            deleteButton.getStyle()
                    .set("color", "var(--lumo-contrast)")
                    .set("background", "var(--lumo-contrast-10pct)");
            deleteButton.addClickListener(e -> confirmDelete(employee));

            HorizontalLayout actions = new HorizontalLayout(viewButton, editButton, deleteButton);
            actions.setSpacing(true);
            actions.setJustifyContentMode(JustifyContentMode.CENTER);
            return actions;
        }).setHeader("Actions").setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);
    }

    private void loadEmployees(int page, int pageSize) {
        loadingSpinner.show();
        try {
            PageResponse<EmployeeResponseDto> pageResponse = employeeService.getAllEmployees(
                    page,
                    pageSize,
                    sortBy,
                    sortDirection
            );
            pagedGrid.updateData(pageResponse);
        } catch (Exception e) {
            log.error("Failed to load employees", e);
            Notification.show("Failed to load employees: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } finally {
            loadingSpinner.hide();
        }
    }

    private void confirmDelete(EmployeeResponseDto employee) {
        com.vaadin.flow.component.confirmdialog.ConfirmDialog dialog = new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
        dialog.setHeader("Delete Employee");
        dialog.setText("Are you sure you want to delete " + employee.firstName() + " " + employee.lastName() + "?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> deleteEmployee(employee.id()));
        dialog.open();
    }

    private void deleteEmployee(Long employeeId) {
        try {
            employeeService.deleteEmployee(employeeId);
            Notification.show("Employee deleted successfully", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadEmployees(pagedGrid.getCurrentPage(), pagedGrid.getPageSize());
        } catch (Exception e) {
            log.error("Failed to delete employee", e);
            Notification.show("Failed to delete employee: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void navigateToForm(Long employeeId) {
        if (employeeId != null) {
            UI.getCurrent().navigate("employees/form/" + employeeId);
        } else {
            UI.getCurrent().navigate("employees/form");
        }
    }

    private void navigateToDetails(Long employeeId) {
        UI.getCurrent().navigate("employees/details/" + employeeId);
    }
}
