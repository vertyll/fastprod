package com.vertyll.fastprod.shared.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.shared.filters.FiltersValue;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

final class FilterControls {

    private FilterControls() {
    }

    static FiltersValue read(Map<String, Component> controls, Map<String, Object> emptyTokens) {
        FiltersValue values = FiltersValue.empty();
        controls.forEach((id, component) -> {
            if (component instanceof TextField textField) {
                values.set(id, textField.getValue());
            } else if (component instanceof Select<?> select) {
                Object value = select.getValue();
                if (isEmptyToken(emptyTokens.get(id), value)) {
                    values.remove(id);
                } else {
                    values.set(id, value);
                }
            } else if (component instanceof MultiSelectComboBox<?> multiSelect) {
                values.set(id, new ArrayList<>(multiSelect.getSelectedItems()));
            }
        });
        return values;
    }

    static void write(Map<String, Component> controls, Map<String, Object> emptyTokens, FiltersValue values) {
        controls.forEach((id, component) -> write(component, emptyTokens.get(id), values.asMap().get(id)));
    }

    static void clear(Map<String, Component> controls, Map<String, Object> emptyTokens) {
        controls.forEach((id, component) -> write(component, emptyTokens.get(id), null));
    }

    @SuppressWarnings("unchecked")
    private static void write(Component component, @Nullable Object emptyToken, @Nullable Object value) {
        if (component instanceof TextField textField) {
            textField.setValue(value != null ? String.valueOf(value) : "");
        } else if (component instanceof Select<?> select) {
            Select<Object> typed = (Select<Object>) select;
            if (value != null) {
                typed.setValue(value);
            } else if (emptyToken != null) {
                typed.setValue(emptyToken);
            } else {
                typed.clear();
            }
        } else if (component instanceof MultiSelectComboBox<?> multiSelect) {
            MultiSelectComboBox<Object> typed = (MultiSelectComboBox<Object>) multiSelect;
            typed.clear();
            if (value instanceof Collection<?> collection) {
                typed.setValue(new HashSet<>(collection));
            }
        }
    }

    private static boolean isEmptyToken(@Nullable Object emptyToken, @Nullable Object value) {
        return emptyToken != null && Objects.equals(value, emptyToken);
    }
}
