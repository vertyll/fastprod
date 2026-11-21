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

    private static <T> Optional<Chip> ofSelect(Select<T> select, @Nullable Object emptyToken) {
        T value = select.getValue();
        if (value == null || (emptyToken != null && Objects.equals(value, emptyToken))) {
            return Optional.empty();
        }
        String text = select.getItemLabelGenerator().apply(value);
        return Optional.of(new Chip(select.getLabel(), text));
    }

    private static <T> Optional<Chip> ofMultiSelect(MultiSelectComboBox<T> multiSelect) {
        Set<T> selected = multiSelect.getSelectedItems();
        if (selected.isEmpty()) {
            return Optional.empty();
        }
        ItemLabelGenerator<T> generator = multiSelect.getItemLabelGenerator();
        List<String> labels = selected.stream().map(generator).toList();
        return Optional.of(new Chip(multiSelect.getLabel(), String.join(", ", labels)));
    }

    record Chip(String label, String value) {
    }
}
