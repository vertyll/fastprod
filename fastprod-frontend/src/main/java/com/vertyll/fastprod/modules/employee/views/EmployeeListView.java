package com.vertyll.fastprod.modules.employee.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.modules.employee.dto.EmployeeResponseDto;
import com.vertyll.fastprod.modules.employee.service.EmployeeService;
import com.vertyll.fastprod.shared.dto.PageResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Route(value = "employees", layout = MainLayout.class)
@PageTitle("Employees | FastProd")
@Slf4j
public class EmployeeListView extends VerticalLayout {

    private final EmployeeService employeeService;
    private final Grid<EmployeeResponseDto> grid;

    private int currentPage = 0;
    private int pageSize = 10;
    private String sortBy = "id";
    private String sortDirection = "ASC";
    private long totalElements = 0;
    private int totalPages = 0;

    private Span pageInfoSpan;
    private Span totalElementsSpan;
    private Button previousButton;
    private Button nextButton;
    private TextField pageField;
    private ComboBox<Integer> pageSizeComboBox;
    private HorizontalLayout paginationLayout;

    public EmployeeListView(EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.grid = new Grid<>(EmployeeResponseDto.class, false);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createToolbar();
        configureGrid();
        createPaginationControls();
        loadEmployees();

        add(grid);
        addPaginationToLayout();
    }

    private void createToolbar() {
        Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshButton.addClickListener(e -> loadEmployees());

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
        grid.addColumn(EmployeeResponseDto::id).setHeader("ID").setSortable(true).setAutoWidth(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);
        grid.addColumn(EmployeeResponseDto::firstName).setHeader("First Name").setSortable(true).setAutoWidth(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);
        grid.addColumn(EmployeeResponseDto::lastName).setHeader("Last Name").setSortable(true).setAutoWidth(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);
        grid.addColumn(EmployeeResponseDto::email).setHeader("Email").setSortable(true).setAutoWidth(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);

        grid.addComponentColumn(employee -> {
            String roles = employee.roles().stream()
                    .collect(Collectors.joining(", "));
            Span span = new Span(roles);
            span.getStyle().set("display", "flex").set("justify-content", "center");
            return span;
        }).setHeader("Roles").setAutoWidth(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);

        grid.addComponentColumn(employee -> {
            Span badge = new Span(employee.isVerified() ? "Verified" : "Not Verified");
            badge.getElement().getThemeList().add(employee.isVerified() ? "badge success" : "badge error");
            HorizontalLayout layout = new HorizontalLayout(badge);
            layout.setJustifyContentMode(JustifyContentMode.CENTER);
            layout.setWidthFull();
            return layout;
        }).setHeader("Status").setAutoWidth(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);

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
        }).setHeader("Actions").setAutoWidth(true).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER);

        grid.setSizeFull();
    }

    private void loadEmployees() {
        try {
            PageResponse<EmployeeResponseDto> pageResponse = employeeService.getAllEmployees(currentPage, pageSize,
                    sortBy, sortDirection);
            grid.setItems(pageResponse.content());

            totalElements = pageResponse.totalElements();
            totalPages = pageResponse.totalPages();

            updatePaginationControls();
        } catch (Exception e) {
            log.error("Failed to load employees", e);
            Notification.show("Failed to load employees: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void createPaginationControls() {
        previousButton = new Button(VaadinIcon.ANGLE_LEFT.create());
        previousButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        previousButton.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadEmployees();
            }
        });

        nextButton = new Button(VaadinIcon.ANGLE_RIGHT.create());
        nextButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        nextButton.addClickListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadEmployees();
            }
        });

        pageField = new TextField();
        pageField.setWidth("60px");
        pageField.setValue(String.valueOf(currentPage + 1));
        pageField.addBlurListener(e -> {
            try {
                int page = Integer.parseInt(pageField.getValue()) - 1;
                if (page >= 0 && page < totalPages) {
                    currentPage = page;
                    loadEmployees();
                } else {
                    pageField.setValue(String.valueOf(currentPage + 1));
                }
            } catch (NumberFormatException ex) {
                pageField.setValue(String.valueOf(currentPage + 1));
            }
        });

        pageInfoSpan = new Span();
        totalElementsSpan = new Span();

        pageSizeComboBox = new ComboBox<>();
        pageSizeComboBox.setItems(5, 10, 20, 50, 100);
        pageSizeComboBox.setValue(pageSize);
        pageSizeComboBox.setWidth("80px");
        pageSizeComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                pageSize = e.getValue();
                currentPage = 0;
                loadEmployees();
            }
        });

        Span pageSizeLabel = new Span("Items per page:");
        pageSizeLabel.getStyle().set("margin-right", "var(--lumo-space-s)");

        paginationLayout = new HorizontalLayout();
        paginationLayout.setSpacing(true);
        paginationLayout.setAlignItems(Alignment.CENTER);
        paginationLayout.add(
                previousButton,
                new Span("Page"),
                pageField,
                new Span("of"),
                pageInfoSpan,
                nextButton,
                new Span("·"),
                totalElementsSpan,
                new Span("total"),
                pageSizeLabel,
                pageSizeComboBox);
    }

    private void addPaginationToLayout() {
        add(paginationLayout);
    }

    private void updatePaginationControls() {
        pageInfoSpan.setText(String.valueOf(totalPages));
        totalElementsSpan.setText(String.valueOf(totalElements));
        pageField.setValue(String.valueOf(currentPage + 1));
        previousButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);
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
            loadEmployees();
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
