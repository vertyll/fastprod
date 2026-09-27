package com.vertyll.fastprod.shared.components;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

final class FilterChips {

    private FilterChips() {
    }

    static Optional<Chip> of(Component component, @Nullable Object selectEmptyToken) {
        return switch (component) {
            case TextField textField -> ofTextField(textField);
            case Select<?> select -> ofSelect(select, selectEmptyToken);
            case MultiSelectComboBox<?> multiSelect -> ofMultiSelect(multiSelect);
            default -> Optional.empty();
        };
    }

    private static Optional<Chip> ofTextField(TextField textField) {
        String value = textField.getValue();
        if (value != null && !value.isBlank()) {
            return Optional.of(new Chip(textField.getLabel(), value));
        }
        return Optional.empty();
    }

    private static Optional<Chip> ofSelect(Select<?> select, @Nullable Object emptyToken) {
        Object value = select.getValue();
        if (value == null || (emptyToken != null && Objects.equals(value, emptyToken))) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked") ItemLabelGenerator<Object> generator =
                (ItemLabelGenerator<Object>) select.getItemLabelGenerator();
        String text = generator.apply(value);
        return Optional.of(new Chip(select.getLabel(), text));
    }

    private static Optional<Chip> ofMultiSelect(MultiSelectComboBox<?> multiSelect) {
        Set<?> selected = multiSelect.getSelectedItems();
        if (selected.isEmpty()) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked") ItemLabelGenerator<Object> generator =
                (ItemLabelGenerator<Object>) multiSelect.getItemLabelGenerator();
        List<String> labels = selected.stream().map(generator).toList();
        return Optional.of(new Chip(multiSelect.getLabel(), String.join(", ", labels)));
    }

    record Chip(String label, String value) {
    }
}
