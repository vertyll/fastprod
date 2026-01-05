package com.vertyll.fastprod.shared.components;

import java.util.function.Consumer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import lombok.Getter;
import lombok.Setter;

public class PaginationComponent extends HorizontalLayout {

    private final Button previousButton;
    private final Button nextButton;
    private final TextField pageField;
    private final Span pageInfoSpan;
    private final Span totalElementsSpan;
    private final Select<Integer> pageSizeSelect;

    @Getter private int currentPage = 0;
    private int totalPages = 0;
    private long totalElements = 0;
    @Setter private transient Consumer<Integer> onPageChange;
    @Setter private transient Consumer<Integer> onPageSizeChange;

    public PaginationComponent() {
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        previousButton = createPreviousButton();
        nextButton = createNextButton();
        pageField = createPageField();
        pageInfoSpan = new Span("0");
        totalElementsSpan = new Span("0");
        pageSizeSelect = createPageSizeSelect();

        Span pageSizeLabel = new Span("Items per page:");
        pageSizeLabel.getStyle().set("margin-right", "var(--lumo-space-s)");

        add(
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
                pageSizeSelect);
    }

    private Button createPreviousButton() {
        Button button = new Button(VaadinIcon.ANGLE_LEFT.create());
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);
        button.addClickListener(_ -> handlePreviousPage());
        return button;
    }

    private Button createNextButton() {
        Button button = new Button(VaadinIcon.ANGLE_RIGHT.create());
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);
        button.addClickListener(_ -> handleNextPage());
        return button;
    }

    private TextField createPageField() {
        TextField field = new TextField();
        field.setWidth("60px");
        field.setValue("1");
        field.addBlurListener(_ -> handlePageFieldChange());
        return field;
    }

    private Select<Integer> createPageSizeSelect() {
        Select<Integer> select = new Select<>();
        select.setItems(5, 10, 20, 50, 100);
        select.setValue(10);
        select.setWidth("80px");
        select.addValueChangeListener(e -> handlePageSizeChange(e.getValue()));
        return select;
    }

    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            notifyPageChange();
            updateControls();
        }
    }

    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            notifyPageChange();
            updateControls();
        }
    }

    private void handlePageFieldChange() {
        try {
            int page = Integer.parseInt(pageField.getValue()) - 1;
            if (isValidPage(page)) {
                currentPage = page;
                notifyPageChange();
                updateControls();
            } else {
                resetPageField();
            }
        } catch (NumberFormatException _) {
            resetPageField();
        }
    }

    private void handlePageSizeChange(Integer newPageSize) {
        if (newPageSize != null && onPageSizeChange != null) {
            currentPage = 0;
            onPageSizeChange.accept(newPageSize);
            updateControls();
        }
    }

    private boolean isValidPage(int page) {
        return page >= 0 && page < totalPages;
    }

    private void resetPageField() {
        pageField.setValue(String.valueOf(currentPage + 1));
    }

    private void notifyPageChange() {
        if (onPageChange != null) {
            onPageChange.accept(currentPage);
        }
    }

    public void updatePagination(int currentPage, int totalPages, long totalElements) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        updateControls();
    }

    private void updateControls() {
        pageInfoSpan.setText(String.valueOf(totalPages));
        totalElementsSpan.setText(String.valueOf(totalElements));
        pageField.setValue(String.valueOf(currentPage + 1));
        previousButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);
    }

    public void setPageSize(int pageSize) {
        pageSizeSelect.setValue(pageSize);
    }

    public int getPageSize() {
        return pageSizeSelect.getValue() != null ? pageSizeSelect.getValue() : 10;
    }
}
