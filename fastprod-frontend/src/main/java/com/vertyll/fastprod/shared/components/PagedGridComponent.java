package com.vertyll.fastprod.shared.components;

import java.io.Serial;

import com.vertyll.fastprod.shared.dto.PageResponse;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableBiConsumer;

import lombok.Getter;

public final class PagedGridComponent<T> extends VerticalLayout {
    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final Grid<T> grid;
    @Getter
    private final PaginationComponent pagination;

    public PagedGridComponent(Class<T> beanType) {
        this(new Grid<>(beanType, false));
    }

    public PagedGridComponent(Grid<T> customGrid) {
        super();
        this.grid = customGrid;
        this.pagination = new PaginationComponent();

        setPadding(false);
        setSpacing(false);
        setSizeFull();

        grid.setSizeFull();

        add(grid);
        add(pagination);
    }

    public void setOnPageChange(SerializableBiConsumer<Integer, Integer> onPageChange) {
        pagination.setOnPageChange(page -> onPageChange.accept(page, pagination.getPageSize()));
        pagination.setOnPageSizeChange(pageSize -> onPageChange.accept(0, pageSize));
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
