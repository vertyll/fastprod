package com.vertyll.fastprod.shared.components;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.vertyll.fastprod.shared.filters.FilterFieldConfig;

import com.vaadin.flow.component.ItemLabelGenerator;

final class FilterItems {

    private FilterItems() {
    }

    static List<Object> itemsOf(FilterFieldConfig<?> cfg) {
        return cfg.staticItems().map(FilterItems::normalizeItems).orElseGet(List::of);
    }

    @SuppressWarnings("unchecked")
    static ItemLabelGenerator<Object> labelGeneratorOf(FilterFieldConfig<?> cfg) {
        return cfg.itemLabelGenerator()
            .map(generator -> (ItemLabelGenerator<Object>) generator)
            .orElse(FilterItems::defaultLabel);
    }

    private static String defaultLabel(@Nullable Object item) {
        return item == null ? "" : String.valueOf(item);
    }

    private static List<Object> normalizeItems(List<?> items) {
        if (items.size() != 1) {
            return List.copyOf(items);
        }

        Object first = items.getFirst();
        List<Object> out = processSingleItem(first, items);
        return Collections.unmodifiableList(out);
    }

    private static List<Object> processSingleItem(@Nullable Object first, List<?> items) {
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
        int length = Array.getLength(primitiveArray);
        List<Object> result = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            result.add(Array.get(primitiveArray, i));
        }
        return result;
    }
}
