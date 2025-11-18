package com.vertyll.fastprod.shared.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public class PaginationComponent extends HorizontalLayout {

    private final Button previousButton;
    private final Button nextButton;
    private final TextField pageField;
    private final Span pageInfoSpan;
    private final Span totalElementsSpan;
    private final ComboBox<Integer> pageSizeComboBox;

    @Getter
    private int currentPage = 0;
    private int totalPages = 0;
    private long totalElements = 0;
    @Setter
    private Consumer<Integer> onPageChange;
    @Setter
    private Consumer<Integer> onPageSizeChange;

    public PaginationComponent() {
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        previousButton = new Button(VaadinIcon.ANGLE_LEFT.create());
        previousButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        previousButton.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                if (onPageChange != null) {
                    onPageChange.accept(currentPage);
                }
                updateControls();
            }
        });

        nextButton = new Button(VaadinIcon.ANGLE_RIGHT.create());
        nextButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        nextButton.addClickListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                if (onPageChange != null) {
                    onPageChange.accept(currentPage);
                }
                updateControls();
            }
        });

        pageField = new TextField();
        pageField.setWidth("60px");
        pageField.setValue("1");
        pageField.addBlurListener(e -> {
            try {
                int page = Integer.parseInt(pageField.getValue()) - 1;
                if (page >= 0 && page < totalPages) {
                    currentPage = page;
                    if (onPageChange != null) {
                        onPageChange.accept(currentPage);
                    }
                    updateControls();
                } else {
                    pageField.setValue(String.valueOf(currentPage + 1));
                }
            } catch (NumberFormatException ex) {
                pageField.setValue(String.valueOf(currentPage + 1));
            }
        });

        pageInfoSpan = new Span("0");
        totalElementsSpan = new Span("0");

        pageSizeComboBox = new ComboBox<>();
        pageSizeComboBox.setItems(5, 10, 20, 50, 100);
        pageSizeComboBox.setValue(10);
        pageSizeComboBox.setWidth("80px");
        pageSizeComboBox.addValueChangeListener(e -> {
            if (e.getValue() != null && onPageSizeChange != null) {
                currentPage = 0;
                onPageSizeChange.accept(e.getValue());
                updateControls();
            }
        });

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
                pageSizeComboBox
        );
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
        pageSizeComboBox.setValue(pageSize);
    }

    public int getPageSize() {
        return pageSizeComboBox.getValue() != null ? pageSizeComboBox.getValue() : 10;
    }
}
