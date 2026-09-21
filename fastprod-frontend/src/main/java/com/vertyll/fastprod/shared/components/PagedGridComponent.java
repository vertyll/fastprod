package com.vertyll.fastprod.shared.components;

import java.util.Objects;

import com.vertyll.fastprod.shared.dto.PageResponse;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableBiConsumer;

import lombok.Getter;

public class PagedGridComponent<T> extends VerticalLayout {

    @Getter
    private final Grid<T> grid;
    @Getter
    private final PaginationComponent pagination;

    private SerializableBiConsumer<Integer, Integer> onPageChange = (_, _) -> {
    };

    public PagedGridComponent(Class<T> beanType) {
        this(new Grid<>(beanType, false));
    }

    public PagedGridComponent(Grid<T> customGrid) {
        this.grid = customGrid;
        this.pagination = new PaginationComponent();

        setPadding(false);
        setSpacing(false);
        setSizeFull();

        grid.setSizeFull();

        pagination.setOnPageChange(page -> onPageChange.accept(page, pagination.getPageSize()));
        pagination.setOnPageSizeChange(pageSize -> onPageChange.accept(0, pageSize));

        add(grid);
        add(pagination);
    }

    public void setOnPageChange(SerializableBiConsumer<Integer, Integer> onPageChange) {
        this.onPageChange = Objects.requireNonNull(onPageChange, "onPageChange");
    }

    public void updateData(PageResponse<T> pageResponse) {
        grid.setItems(pageResponse.content());
        pagination.updatePagination(pageResponse.pageNumber(), pageResponse.totalPages(), pageResponse.totalElements());
    }

    public void setInitialPageSize(int pageSize) {
        pagination.setPageSize(pageSize);
    }

    public int getCurrentPage() {
        return pagination.getCurrentPage();
    }

    public int getPageSize() {
        return pagination.getPageSize();
    }
}
