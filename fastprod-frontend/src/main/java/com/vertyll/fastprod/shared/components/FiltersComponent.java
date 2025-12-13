package com.vertyll.fastprod.shared.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vertyll.fastprod.shared.filters.FilterFieldConfig;
import com.vertyll.fastprod.shared.filters.FilterFieldType;
import com.vertyll.fastprod.shared.filters.FiltersValue;

import java.util.*;
import java.util.function.Consumer;

public class FiltersComponent extends HorizontalLayout {

    private final Map<String, Component> controls = new LinkedHashMap<>();
    private final List<Consumer<FiltersValue>> listeners = new ArrayList<>();
    private final Map<String, Object> selectEmptyTokens = new HashMap<>();

    private int maxVisible = 6;
    private boolean expanded = false;
    private final Button toggleButton = new Button();

    private final HorizontalLayout summaryBar = new HorizontalLayout();
    private final HorizontalLayout selectedChips = new HorizontalLayout();

    public FiltersComponent() {
        setWidthFull();
        setSpacing(true);
        setPadding(false);
        getStyle().set("flex-wrap", "wrap");
        getStyle().set("gap", "var(--lumo-space-s)");

        toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        Button clearButton = new Button("Clear all");
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        summaryBar.setWidthFull();
        summaryBar.setSpacing(true);
        summaryBar.setAlignItems(Alignment.CENTER);
        Span selectedTitle = new Span("Selected filters:");
        selectedTitle.getStyle().set("white-space", "nowrap");
        summaryBar.getStyle().set("flex-wrap", "nowrap");
        selectedChips.setSpacing(true);
        selectedChips.getStyle().set("flex-wrap", "wrap");
        selectedChips.setWidthFull();
        summaryBar.add(selectedTitle, selectedChips, clearButton);
        summaryBar.expand(selectedChips);
        summaryBar.getStyle().set("flex-basis", "100%");

        toggleButton.addClickListener(_ -> {
            expanded = !expanded;
            updateToggleLabel();
            updateVisibility();
        });
        clearButton.addClickListener(_ -> clear());
    }

    public void setConfig(List<? extends FilterFieldConfig<?>> configs) {
        removeAll();
        controls.clear();
        selectEmptyTokens.clear();
        expanded = false;

        for (FilterFieldConfig<?> cfg : configs) {
            Component c = createControl(cfg);
            controls.put(cfg.id(), c);
            add(c);
        }

        add(toggleButton);
        add(summaryBar);
        updateToggleLabel();
        updateVisibility();
        updateSelectedSummary(FiltersValue.empty());
    }

    public void setMaxVisible(int maxVisible) {
        if (maxVisible < 1) maxVisible = 1;
        this.maxVisible = maxVisible;
        updateVisibility();
    }

    public void setToggleLabels(String showMore, String collapse) {
        toggleButton.getElement().setProperty("data-show-label", showMore);
        toggleButton.getElement().setProperty("data-hide-label", collapse);
        updateToggleLabel();
    }

    private void updateToggleLabel() {
        String customShow = toggleButton.getElement().getProperty("data-show-label");
        String customHide = toggleButton.getElement().getProperty("data-hide-label");
        String show = customShow != null ? customShow : "Show more";
        String hide = customHide != null ? customHide : "Collapse";
        toggleButton.setText(expanded ? hide : show);
    }

    private void updateVisibility() {
        int total = controls.size();
        boolean needToggle = total > maxVisible;
        toggleButton.setVisible(needToggle);

        int i = 0;
        for (Component c : controls.values()) {
            if (expanded) {
                c.setVisible(true);
            } else {
                c.setVisible(i < maxVisible);
            }
            i++;
        }
    }

    private Component createControl(FilterFieldConfig<?> cfg) {
        if (cfg.type() == FilterFieldType.TEXT) {
            TextField tf = new TextField(cfg.label());
            if (cfg.placeholder() != null) tf.setPlaceholder(cfg.placeholder());
            tf.setClearButtonVisible(true);
            tf.setValueChangeMode(ValueChangeMode.LAZY);
            tf.setValueChangeTimeout(300);
            tf.addValueChangeListener(_ -> emitChange());
            return tf;
        }
        if (cfg.type() == FilterFieldType.SELECT) {
            Select<Object> select = new Select<>();
            select.setLabel(cfg.label());
            select.setEmptySelectionAllowed(false);
            if (cfg.placeholder() != null) {
                select.setPlaceholder(cfg.placeholder());
            }
            List<?> rawItems = cfg.staticItems() != null ? cfg.staticItems() :
                    (cfg.itemsSupplier() != null ? cfg.itemsSupplier().get() : Collections.emptyList());
            List<Object> items = normalizeItems(rawItems);

            List<Object> menuItems = new ArrayList<>();
            final Object emptyTokenLocal = (cfg.placeholder() != null) ? new Object() : null;
            if (emptyTokenLocal != null) {
                selectEmptyTokens.put(cfg.id(), emptyTokenLocal);
                menuItems.add(emptyTokenLocal);
            }
            menuItems.addAll(items);

            if (cfg.itemLabelGenerator() != null) {
                @SuppressWarnings("unchecked")
                var gen = (com.vaadin.flow.component.ItemLabelGenerator<Object>) cfg.itemLabelGenerator();
                var wrapped = (com.vaadin.flow.component.ItemLabelGenerator<Object>) item -> {
                    if (emptyTokenLocal != null && Objects.equals(item, emptyTokenLocal)) {
                        return cfg.placeholder();
                    }
                    return gen.apply(item);
                };
                select.setItemLabelGenerator(wrapped);
            } else {
                select.setItemLabelGenerator(item -> {
                    if (emptyTokenLocal != null && Objects.equals(item, emptyTokenLocal)) {
                        return cfg.placeholder();
                    }
                    return item == null ? "" : String.valueOf(item);
                });
            }

            select.setItems(menuItems);
            if (emptyTokenLocal != null) {
                select.setValue(emptyTokenLocal);
            }
            select.addValueChangeListener(_ -> emitChange());
            return select;
        }
        if (cfg.type() == FilterFieldType.MULTISELECT) {
            MultiSelectComboBox<Object> ms = new MultiSelectComboBox<>();
            ms.setLabel(cfg.label());
            if (cfg.placeholder() != null) ms.setPlaceholder(cfg.placeholder());
            List<?> rawItems = cfg.staticItems() != null ? cfg.staticItems() :
                    (cfg.itemsSupplier() != null ? cfg.itemsSupplier().get() : Collections.emptyList());
            List<Object> items = normalizeItems(rawItems);
            if (cfg.itemLabelGenerator() != null) {
                @SuppressWarnings("unchecked")
                var gen = (com.vaadin.flow.component.ItemLabelGenerator<Object>) cfg.itemLabelGenerator();
                ms.setItemLabelGenerator(gen);
            } else {
                ms.setItemLabelGenerator(item -> item == null ? "" : String.valueOf(item));
            }
            ms.setItems(items);
            ms.setClearButtonVisible(true);
            ms.addValueChangeListener(_ -> emitChange());
            return ms;
        }
        return new TextField(cfg.label());
    }

    private static List<Object> normalizeItems(List<?> items) {
        if (items == null) return Collections.emptyList();
        if (items.size() == 1) {
            Object first = items.getFirst();
            if (first != null && first.getClass().isArray()) {
                // Object[] case
                if (first instanceof Object[] arr) {
                    return Arrays.asList(arr);
                }
                // Primitive arrays fallback: copy via reflection
                int length = java.lang.reflect.Array.getLength(first);
                List<Object> out = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    out.add(java.lang.reflect.Array.get(first, i));
                }
                return out;
            }
            // If single element is a Collection -> unwrap
            if (first instanceof Collection<?> col) {
                return new ArrayList<>(col);
            }
        }
        // Otherwise return shallow copy as List<Object>
        List<Object> out = new ArrayList<>(items.size());
        out.addAll(items);
        return out;
    }

    public FiltersValue getValues() {
        FiltersValue fv = FiltersValue.empty();
        for (Map.Entry<String, Component> e : controls.entrySet()) {
            String id = e.getKey();
            Component c = e.getValue();
            if (c instanceof TextField tf) {
                fv.set(id, tf.getValue());
            } else if (c instanceof Select<?> sel) {
                Object value = sel.getValue();
                Object token = selectEmptyTokens.get(id);
                if (token != null && Objects.equals(value, token)) {
                    fv.set(id, null);
                } else {
                    fv.set(id, value);
                }
            } else if (c instanceof MultiSelectComboBox<?> ms) {
                fv.set(id, new ArrayList<>(ms.getSelectedItems()));
            }
        }
        return fv;
    }

    public void setValues(FiltersValue values) {
        for (Map.Entry<String, Component> e : controls.entrySet()) {
            Object v = values.asMap().get(e.getKey());
            Component c = e.getValue();
            if (c instanceof TextField tf) {
                tf.setValue(v != null ? String.valueOf(v) : "");
            } else if (c instanceof Select<?> sel) {
                @SuppressWarnings("unchecked")
                Select<Object> s = (Select<Object>) sel;
                if (v == null) {
                    Object token = selectEmptyTokens.get(e.getKey());
                    if (token != null) {
                        s.setValue(token);
                    } else {
                        s.clear();
                    }
                } else {
                    s.setValue(v);
                }
            } else if (c instanceof MultiSelectComboBox<?> anyMs) {
                @SuppressWarnings("unchecked")
                MultiSelectComboBox<Object> ms = (MultiSelectComboBox<Object>) anyMs;
                ms.clear();
                if (v instanceof Collection<?> col) {
                    Set<Object> set = new HashSet<>(col);
                    ms.setValue(set);
                }
            }
        }
        updateSelectedSummary(getValues());
    }

    public void clear() {
        for (Map.Entry<String, Component> e : controls.entrySet()) {
            Component c = e.getValue();
            if (c instanceof TextField tf) tf.clear();
            if (c instanceof Select<?> sel) {
                @SuppressWarnings("unchecked")
                Select<Object> s = (Select<Object>) sel;
                Object token = selectEmptyTokens.get(e.getKey());
                if (token != null) {
                    s.setValue(token);
                } else {
                    s.clear();
                }
            }
            if (c instanceof MultiSelectComboBox<?> ms) ms.clear();
        }
        emitChange();
    }

    public void addValueChangeListener(Consumer<FiltersValue> listener) {
        listeners.add(listener);
    }

    private void emitChange() {
        FiltersValue fv = getValues();
        updateSelectedSummary(fv);
        listeners.forEach(l -> l.accept(fv));
    }

    private void updateSelectedSummary(FiltersValue ignoredValues) {
        selectedChips.removeAll();

        for (Map.Entry<String, Component> e : controls.entrySet()) {
            String id = e.getKey();
            Component c = e.getValue();
            String labelText = null;
            String valueText = null;

            if (c instanceof TextField tf) {
                String val = tf.getValue();
                if (val != null && !val.isBlank()) {
                    labelText = tf.getLabel();
                    valueText = val;
                }
            } else if (c instanceof Select<?> sel) {
                Object value = sel.getValue();
                Object token = selectEmptyTokens.get(id);
                if (token != null && Objects.equals(value, token)) {
                    value = null;
                }
                if (value != null) {
                    labelText = sel.getLabel();
                    @SuppressWarnings("unchecked")
                    var gen = (com.vaadin.flow.component.ItemLabelGenerator<Object>) sel.getItemLabelGenerator();
                    valueText = gen != null ? gen.apply(value) : String.valueOf(value);
                }
            } else if (c instanceof MultiSelectComboBox<?> ms) {
                Set<?> selected = ms.getSelectedItems();
                if (selected != null && !selected.isEmpty()) {
                    labelText = ms.getLabel();
                    @SuppressWarnings("unchecked")
                    var gen = (com.vaadin.flow.component.ItemLabelGenerator<Object>) ms.getItemLabelGenerator();
                    List<String> labels = new ArrayList<>();
                    for (Object o : selected) {
                        labels.add(gen != null ? gen.apply(o) : String.valueOf(o));
                    }
                    valueText = String.join(", ", labels);
                }
            }

            if (labelText != null && valueText != null) {
                Span chip = new Span(labelText + ": " + valueText);
                chip.getElement().getThemeList().add("badge contrast");
                selectedChips.add(chip);
            }
        }
    }
}
