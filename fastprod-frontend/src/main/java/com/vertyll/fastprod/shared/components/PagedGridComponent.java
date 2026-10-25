package com.vertyll.fastprod.shared.components;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vertyll.fastprod.shared.dto.PageResponse;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.WindowSize;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.signals.Signal;

import lombok.Getter;

public final class PagedGridComponent<T> extends VerticalLayout {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final int NARROW_WINDOW_WIDTH = 992;

    @Getter
    private final Grid<T> grid;
    @Getter
    private final PaginationComponent pagination;

    private final List<Grid.Column<T>> wideWindowColumns = new ArrayList<>();
    private final Signal<WindowSize> windowSize;

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
        grid.setMinHeight("20rem");

        add(grid);
        add(pagination);

        windowSize =
                Objects.requireNonNull(UI.getCurrent(), "a grid is built inside a UI").getPage().windowSizeSignal();
        Signal.effect(this, () -> showColumnsFor(windowSize.get().width()));
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

    public void hideWhenNarrow(Grid.Column<T> column) {
        wideWindowColumns.add(column);
        column.setVisible(isWide(windowSize.peek().width()));
    }

    private void showColumnsFor(int windowWidth) {
        boolean wide = isWide(windowWidth);
        wideWindowColumns.forEach(column -> column.setVisible(wide));
    }

    private static boolean isWide(int windowWidth) {
        return windowWidth >= NARROW_WINDOW_WIDTH;
    }
}
