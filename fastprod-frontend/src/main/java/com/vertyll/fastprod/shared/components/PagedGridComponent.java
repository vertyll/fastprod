package com.vertyll.fastprod.shared.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vertyll.fastprod.shared.dto.PageResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BiConsumer;

public class PagedGridComponent<T> extends VerticalLayout {

    @Getter
    private final Grid<T> grid;
    @Getter
    private final PaginationComponent pagination;
    @Setter
    private BiConsumer<Integer, Integer> onPageChange;

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

        pagination.setOnPageChange(page -> {
            if (onPageChange != null) {
                onPageChange.accept(page, pagination.getPageSize());
            }
        });

        pagination.setOnPageSizeChange(pageSize -> {
            if (onPageChange != null) {
                onPageChange.accept(0, pageSize);
            }
        });

        add(grid);
        add(pagination);
    }

    public void updateData(PageResponse<T> pageResponse) {
        grid.setItems(pageResponse.content());
        pagination.updatePagination(
                pageResponse.pageNumber(),
                pageResponse.totalPages(),
                pageResponse.totalElements()
        );
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
