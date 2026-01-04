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
import com.vertyll.fastprod.shared.filters.FiltersValue;

import java.util.*;
import java.util.function.Consumer;

public class FiltersComponent extends HorizontalLayout {

    private static final String FLEX_WRAP = "flex-wrap";

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
        getStyle().set(FLEX_WRAP, "wrap");
        getStyle().set("gap", "var(--lumo-space-s)");

        toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        Button clearButton = new Button("Clear all");
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        summaryBar.setWidthFull();
        summaryBar.setSpacing(true);
        summaryBar.setAlignItems(Alignment.CENTER);
        Span selectedTitle = new Span("Selected filters:");
        selectedTitle.getStyle().set("white-space", "nowrap");
        summaryBar.getStyle().set(FLEX_WRAP, "nowrap");
        selectedChips.setSpacing(true);
        selectedChips.getStyle().set(FLEX_WRAP, "wrap");
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
        updateSelectedSummary();
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
        return switch (cfg.type()) {
            case TEXT -> createTextField(cfg);
            case SELECT -> createSelect(cfg);
            case MULTISELECT -> createMultiSelect(cfg);
            default -> new TextField(cfg.label());
        };
    }

    private TextField createTextField(FilterFieldConfig<?> cfg) {
        TextField tf = new TextField(cfg.label());
        if (cfg.placeholder() != null) {
            tf.setPlaceholder(cfg.placeholder());
        }
        tf.setClearButtonVisible(true);
        tf.setValueChangeMode(ValueChangeMode.LAZY);
        tf.setValueChangeTimeout(300);
        tf.addValueChangeListener(_ -> emitChange());
        return tf;
    }

    private Select<Object> createSelect(FilterFieldConfig<?> cfg) {
        Select<Object> select = new Select<>();
        select.setLabel(cfg.label());
        select.setEmptySelectionAllowed(false);
        
        if (cfg.placeholder() != null) {
            select.setPlaceholder(cfg.placeholder());
        }

        List<Object> items = getItemsForConfig(cfg);
        List<Object> menuItems = buildSelectMenuItems(cfg, items);
        
        configureSelectLabels(select, cfg);
        select.setItems(menuItems);
        setSelectInitialValue(select, cfg);
        select.addValueChangeListener(_ -> emitChange());
        
        return select;
    }

    private List<Object> getItemsForConfig(FilterFieldConfig<?> cfg) {
        List<?> rawItems;
        if (cfg.staticItems() != null) {
            rawItems = cfg.staticItems();
        } else if (cfg.itemsSupplier() != null) {
            rawItems = cfg.itemsSupplier().get();
        } else {
            rawItems = Collections.emptyList();
        }
        return normalizeItems(rawItems);
    }

    private List<Object> buildSelectMenuItems(FilterFieldConfig<?> cfg, List<Object> items) {
        List<Object> menuItems = new ArrayList<>();
        Object emptyToken = cfg.placeholder() != null ? new Object() : null;
        
        if (emptyToken != null) {
            selectEmptyTokens.put(cfg.id(), emptyToken);
            menuItems.add(emptyToken);
        }
        menuItems.addAll(items);
        
        return menuItems;
    }

    private void configureSelectLabels(Select<Object> select, FilterFieldConfig<?> cfg) {
        Object emptyToken = selectEmptyTokens.get(cfg.id());
        
        if (cfg.itemLabelGenerator() != null) {
            @SuppressWarnings("unchecked")
            var gen = (com.vaadin.flow.component.ItemLabelGenerator<Object>) cfg.itemLabelGenerator();
            select.setItemLabelGenerator(item -> generateSelectLabel(item, emptyToken, cfg.placeholder(), gen));
        } else {
            select.setItemLabelGenerator(item -> generateDefaultSelectLabel(item, emptyToken, cfg.placeholder()));
        }
    }

    private String generateSelectLabel(Object item, Object emptyToken, String placeholder, 
                                      com.vaadin.flow.component.ItemLabelGenerator<Object> generator) {
        if (emptyToken != null && Objects.equals(item, emptyToken)) {
            return placeholder;
        }
        return generator.apply(item);
    }

    private String generateDefaultSelectLabel(Object item, Object emptyToken, String placeholder) {
        if (emptyToken != null && Objects.equals(item, emptyToken)) {
            return placeholder;
        }
        return item == null ? "" : String.valueOf(item);
    }

    private void setSelectInitialValue(Select<Object> select, FilterFieldConfig<?> cfg) {
        Object emptyToken = selectEmptyTokens.get(cfg.id());
        if (emptyToken != null) {
            select.setValue(emptyToken);
        }
    }

    private MultiSelectComboBox<Object> createMultiSelect(FilterFieldConfig<?> cfg) {
        MultiSelectComboBox<Object> ms = new MultiSelectComboBox<>();
        ms.setLabel(cfg.label());
        
        if (cfg.placeholder() != null) {
            ms.setPlaceholder(cfg.placeholder());
        }

        List<Object> items = getItemsForConfig(cfg);
        configureMultiSelectLabels(ms, cfg);
        ms.setItems(items);
        ms.setClearButtonVisible(true);
        ms.addValueChangeListener(_ -> emitChange());
        
        return ms;
    }

    private void configureMultiSelectLabels(MultiSelectComboBox<Object> ms, FilterFieldConfig<?> cfg) {
        if (cfg.itemLabelGenerator() != null) {
            @SuppressWarnings("unchecked")
            var gen = (com.vaadin.flow.component.ItemLabelGenerator<Object>) cfg.itemLabelGenerator();
            ms.setItemLabelGenerator(gen);
        } else {
            ms.setItemLabelGenerator(item -> item == null ? "" : String.valueOf(item));
        }
    }

    private static List<Object> normalizeItems(List<?> items) {
        if (items == null) {
            return Collections.emptyList();
        }

        if (items.size() != 1) {
            return Collections.unmodifiableList(new ArrayList<>(items));
        }

        Object first = items.getFirst();
        List<Object> out = processSingleItem(first, items);
        return Collections.unmodifiableList(out);
    }

    private static List<Object> processSingleItem(Object first, List<?> items) {
        if (first == null) {
            return new ArrayList<>(items);
        }

        if (first.getClass().isArray()) {
            return processArrayItem(first);
        }

        if (first instanceof Collection<?> col) {
            return new ArrayList<>(col);
        }

        return new ArrayList<>(items);
    }

    private static List<Object> processArrayItem(Object arrayItem) {
        if (arrayItem instanceof Object[] arr) {
            return new ArrayList<>(Arrays.asList(arr));
        }
        return processPrimitiveArray(arrayItem);
    }

    private static List<Object> processPrimitiveArray(Object primitiveArray) {
        int length = java.lang.reflect.Array.getLength(primitiveArray);
        List<Object> result = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            result.add(java.lang.reflect.Array.get(primitiveArray, i));
        }
        return result;
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
        controls.forEach((key, component) -> setComponentValue(key, component, values.asMap().get(key)));
        updateSelectedSummary();
    }

    private void setComponentValue(String key, Component component, Object value) {
        if (component instanceof TextField tf) {
            setTextFieldValue(tf, value);
        } else if (component instanceof Select<?> select) {
            setSelectValue(key, select, value);
        } else if (component instanceof MultiSelectComboBox<?> multiSelect) {
            setMultiSelectValue(multiSelect, value);
        }
    }

    private void setTextFieldValue(TextField textField, Object value) {
        textField.setValue(value != null ? String.valueOf(value) : "");
    }

    private void setSelectValue(String key, Select<?> select, Object value) {
        @SuppressWarnings("unchecked")
        Select<Object> s = (Select<Object>) select;
        
        if (value == null) {
            Object token = selectEmptyTokens.get(key);
            if (token != null) {
                s.setValue(token);
            } else {
                s.clear();
            }
        } else {
            s.setValue(value);
        }
    }

    private void setMultiSelectValue(MultiSelectComboBox<?> multiSelect, Object value) {
        @SuppressWarnings("unchecked")
        MultiSelectComboBox<Object> ms = (MultiSelectComboBox<Object>) multiSelect;
        ms.clear();
        
        if (value instanceof Collection<?> col) {
            ms.setValue(new HashSet<>(col));
        }
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
        updateSelectedSummary();
        listeners.forEach(l -> l.accept(fv));
    }

    private void updateSelectedSummary() {
        selectedChips.removeAll();
        controls.forEach(this::addChipIfHasValue);
    }

    private void addChipIfHasValue(String id, Component component) {
        ChipData chipData = extractChipData(id, component);
        if (chipData != null) {
            createAndAddChip(chipData);
        }
    }

    private ChipData extractChipData(String id, Component component) {
        if (component instanceof TextField tf) {
            return extractTextFieldChipData(tf);
        } else if (component instanceof Select<?> select) {
            return extractSelectChipData(id, select);
        } else if (component instanceof MultiSelectComboBox<?> multiSelect) {
            return extractMultiSelectChipData(multiSelect);
        }
        return null;
    }

    private ChipData extractTextFieldChipData(TextField textField) {
        String value = textField.getValue();
        if (value != null && !value.isBlank()) {
            return new ChipData(textField.getLabel(), value);
        }
        return null;
    }

    private ChipData extractSelectChipData(String id, Select<?> select) {
        Object value = select.getValue();
        Object token = selectEmptyTokens.get(id);
        
        if (token != null && Objects.equals(value, token)) {
            return null;
        }
        
        if (value != null) {
            @SuppressWarnings("unchecked")
            var gen = (com.vaadin.flow.component.ItemLabelGenerator<Object>) select.getItemLabelGenerator();
            String valueText = gen != null ? gen.apply(value) : String.valueOf(value);
            return new ChipData(select.getLabel(), valueText);
        }
        return null;
    }

    private ChipData extractMultiSelectChipData(MultiSelectComboBox<?> multiSelect) {
        Set<?> selected = multiSelect.getSelectedItems();
        
        if (selected != null && !selected.isEmpty()) {
            @SuppressWarnings("unchecked")
            var gen = (com.vaadin.flow.component.ItemLabelGenerator<Object>) multiSelect.getItemLabelGenerator();
            
            List<String> labels = selected.stream()
                .map(o -> gen != null ? gen.apply(o) : String.valueOf(o))
                .toList();
            
            return new ChipData(multiSelect.getLabel(), String.join(", ", labels));
        }
        return null;
    }

    private void createAndAddChip(ChipData data) {
        Span chip = new Span(data.label + ": " + data.value);
        chip.getElement().getThemeList().add("badge contrast");
        selectedChips.add(chip);
    }

    private record ChipData(String label, String value) {}
}
