package com.vertyll.fastprod.shared.components;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class DetailsTableComponent extends Div {

    private static final String DISPLAY = "display";
    private static final String BORDER_STYLE = "1px solid var(--lumo-contrast-10pct)";

    private final Map<String, Component> rows = new LinkedHashMap<>();

    public DetailsTableComponent() {
        getStyle()
                .set(DISPLAY, "table")
                .set("width", "100%")
                .set("max-width", "600px")
                .set("border", BORDER_STYLE)
                .set("border-radius", "var(--lumo-border-radius-m)");
    }

    public void addRow(String label, String value) {
        Span valueSpan = new Span(value);
        addRow(label, valueSpan);
    }

    public void addRow(String label, Component valueComponent) {
        Div row = createTableRow(label, valueComponent);
        rows.put(label, valueComponent);
        add(row);
    }

    public void updateRow(String label, String value) {
        Component component = rows.get(label);
        if (component instanceof Span span) {
            span.setText(value);
        }
    }

    public void updateRow(String label, Component valueComponent) {
        rows.put(label, valueComponent);
        rebuildTable();
    }

    private void rebuildTable() {
        removeAll();
        rows.forEach((label, component) -> add(createTableRow(label, component)));
    }

    private Div createTableRow(String label, Component valueComponent) {
        Div row = new Div();
        row.getStyle().set(DISPLAY, "table-row");

        Div labelCell = new Div();
        labelCell.setText(label);
        labelCell
                .getStyle()
                .set(DISPLAY, "table-cell")
                .set("padding", "var(--lumo-space-s)")
                .set("border-bottom", BORDER_STYLE)
                .set("font-weight", "500")
                .set("width", "30%")
                .set("background", "var(--lumo-contrast-5pct)");

        Div valueCell = new Div();
        valueCell.add(valueComponent);
        valueCell
                .getStyle()
                .set(DISPLAY, "table-cell")
                .set("padding", "var(--lumo-space-s)")
                .set("border-bottom", BORDER_STYLE);

        row.add(labelCell, valueCell);
        return row;
    }

    public void clear() {
        rows.clear();
        removeAll();
    }
}
