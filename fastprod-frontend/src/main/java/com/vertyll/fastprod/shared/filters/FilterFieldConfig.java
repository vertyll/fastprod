package com.vertyll.fastprod.shared.filters;

import com.vaadin.flow.component.ItemLabelGenerator;

import java.util.List;
import java.util.function.Supplier;

public class FilterFieldConfig<T> {
    private final String id;
    private final String label;
    private final FilterFieldType type;
    private final List<T> staticItems;
    private final Supplier<List<T>> itemsSupplier;
    private final ItemLabelGenerator<T> itemLabelGenerator;
    private final String placeholder;

    private FilterFieldConfig(Builder<T> b) {
        this.id = b.id;
        this.label = b.label;
        this.type = b.type;
        this.staticItems = b.staticItems;
        this.itemsSupplier = b.itemsSupplier;
        this.itemLabelGenerator = b.itemLabelGenerator;
        this.placeholder = b.placeholder;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public FilterFieldType type() {
        return type;
    }

    public List<T> staticItems() {
        return staticItems;
    }

    public Supplier<List<T>> itemsSupplier() {
        return itemsSupplier;
    }

    public ItemLabelGenerator<T> itemLabelGenerator() {
        return itemLabelGenerator;
    }

    public String placeholder() {
        return placeholder;
    }

    public static <T> Builder<T> builder(String id, String label, FilterFieldType type) {
        return new Builder<>(id, label, type);
    }

    public static class Builder<T> {
        private final String id;
        private final String label;
        private final FilterFieldType type;
        private List<T> staticItems;
        private Supplier<List<T>> itemsSupplier;
        private ItemLabelGenerator<T> itemLabelGenerator;
        private String placeholder;

        public Builder(String id, String label, FilterFieldType type) {
            this.id = id;
            this.label = label;
            this.type = type;
        }

        public Builder<T> items(List<T> items) {
            this.staticItems = items;
            return this;
        }

        public Builder<T> itemsSupplier(Supplier<List<T>> supplier) {
            this.itemsSupplier = supplier;
            return this;
        }

        public Builder<T> itemLabel(ItemLabelGenerator<T> generator) {
            this.itemLabelGenerator = generator;
            return this;
        }

        public Builder<T> placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public FilterFieldConfig<T> build() {
            return new FilterFieldConfig<>(this);
        }
    }
}
