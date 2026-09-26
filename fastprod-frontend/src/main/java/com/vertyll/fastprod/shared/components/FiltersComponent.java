package com.vertyll.fastprod.shared.components;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import com.vertyll.fastprod.shared.filters.FilterFieldConfig;
import com.vertyll.fastprod.shared.filters.FiltersValue;
import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

public final class FiltersComponent extends HorizontalLayout {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FLEX_WRAP = "flex-wrap";

    private final Map<String, Component> controls = new LinkedHashMap<>();
    private final List<Consumer<FiltersValue>> listeners = new ArrayList<>();
    private final Map<String, Object> selectEmptyTokens = new HashMap<>();

    private static final int MAX_VISIBLE = 6;
    private boolean expanded = false;
    private final Button toggleButton = new Button();

    private final HorizontalLayout summaryBar = new HorizontalLayout();
    private final HorizontalLayout selectedChips = new HorizontalLayout();

    public FiltersComponent() {
        super();
        setWidthFull();
        setSpacing(true);
        setPadding(false);
        addClassName("filters-bar");
        getStyle().set(FLEX_WRAP, "wrap");
        getStyle().set("gap", "var(--lumo-space-s)");

        toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        Button clearButton = new Button(I18n.t("filters.clearAll"));
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        summaryBar.setWidthFull();
        summaryBar.setSpacing(true);
        summaryBar.setAlignItems(Alignment.CENTER);
        Span selectedTitle = new Span(I18n.t("filters.selected"));
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

    private void updateToggleLabel() {
        String customShow = toggleButton.getElement().getProperty("data-show-label");
        String customHide = toggleButton.getElement().getProperty("data-hide-label");
        String show = customShow != null ? customShow : I18n.t("filters.showMore");
        String hide = customHide != null ? customHide : I18n.t("filters.collapse");
        toggleButton.setText(expanded ? hide : show);
    }

    private void updateVisibility() {
        int total = controls.size();
        boolean needToggle = total > MAX_VISIBLE;
        toggleButton.setVisible(needToggle);

        int i = 0;
        for (Component c : controls.values()) {
            if (expanded) {
                c.setVisible(true);
            } else {
                c.setVisible(i < MAX_VISIBLE);
            }
            i++;
        }
    }

    private Component createControl(FilterFieldConfig<?> cfg) {
        return switch (cfg.type()) {
            case TEXT -> createTextField(cfg);
            case SELECT -> createSelect(cfg);
            case MULTISELECT -> createMultiSelect(cfg);
        };
    }

    private TextField createTextField(FilterFieldConfig<?> cfg) {
        TextField tf = new TextField(cfg.label());
        cfg.placeholder().ifPresent(tf::setPlaceholder);
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
        cfg.placeholder().ifPresent(select::setPlaceholder);

        List<Object> items = FilterItems.itemsOf(cfg);
        List<Object> menuItems = buildSelectMenuItems(cfg, items);

        configureSelectLabels(select, cfg);
        select.setItems(menuItems);
        setSelectInitialValue(select, cfg);
        select.addValueChangeListener(_ -> emitChange());

        return select;
    }

    private List<Object> buildSelectMenuItems(FilterFieldConfig<?> cfg, List<Object> items) {
        List<Object> menuItems = new ArrayList<>();

        if (cfg.placeholder().isPresent()) {
            Object emptyToken = new Object();
            selectEmptyTokens.put(cfg.id(), emptyToken);
            menuItems.add(emptyToken);
        }
        menuItems.addAll(items);

        return menuItems;
    }

    private void configureSelectLabels(Select<Object> select, FilterFieldConfig<?> cfg) {
        Object emptyToken = selectEmptyTokens.get(cfg.id());
        String placeholder = cfg.placeholder().orElse("");
        ItemLabelGenerator<Object> generator = FilterItems.labelGeneratorOf(cfg);

        select.setItemLabelGenerator(item -> {
            if (emptyToken != null && Objects.equals(item, emptyToken)) {
                return placeholder;
            }
            return generator.apply(item);
        });
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
        cfg.placeholder().ifPresent(ms::setPlaceholder);

        List<Object> items = FilterItems.itemsOf(cfg);
        configureMultiSelectLabels(ms, cfg);
        ms.setItems(items);
        ms.setClearButtonVisible(true);
        ms.addValueChangeListener(_ -> emitChange());

        return ms;
    }

    private static void configureMultiSelectLabels(MultiSelectComboBox<Object> ms, FilterFieldConfig<?> cfg) {
        ms.setItemLabelGenerator(FilterItems.labelGeneratorOf(cfg));
    }

    public FiltersValue getValues() {
        return FilterControls.read(controls, selectEmptyTokens);
    }

    public void setValues(FiltersValue values) {
        FilterControls.write(controls, selectEmptyTokens, values);
        updateSelectedSummary();
    }

    public void clear() {
        FilterControls.clear(controls, selectEmptyTokens);
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
        FilterChips.of(component, selectEmptyTokens.get(id)).ifPresent(this::createAndAddChip);
    }

    private void createAndAddChip(FilterChips.Chip data) {
        Span chip = new Span(data.label() + ": " + data.value());
        chip.getElement().getThemeList().add("badge contrast");
        selectedChips.add(chip);
    }
}
