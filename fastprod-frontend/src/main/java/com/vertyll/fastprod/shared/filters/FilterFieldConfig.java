package com.vertyll.fastprod.shared.filters;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.ItemLabelGenerator;

@SuppressWarnings("PMD.DataClass")

public final class FilterFieldConfig<T> {
    private final String id;
    private final String label;
    private final FilterFieldType type;
    private final @Nullable List<T> staticItems;
    private final @Nullable Supplier<List<T>> itemsSupplier;
    private final @Nullable ItemLabelGenerator<T> itemLabelGenerator;
    private final @Nullable String placeholder;

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

    public Optional<List<T>> staticItems() {
        return Optional.ofNullable(staticItems);
    }

    public Optional<Supplier<List<T>>> itemsSupplier() {
        return Optional.ofNullable(itemsSupplier);
    }

    public Optional<ItemLabelGenerator<T>> itemLabelGenerator() {
        return Optional.ofNullable(itemLabelGenerator);
    }

    public Optional<String> placeholder() {
        return Optional.ofNullable(placeholder);
    }

    public static <T> Builder<T> builder(String id, String label, FilterFieldType type) {
        return new Builder<>(id, label, type);
    }

    public static final class Builder<T> {
        private final String id;
        private final String label;
        private final FilterFieldType type;
        private @Nullable List<T> staticItems;
        private @Nullable Supplier<List<T>> itemsSupplier;
        private @Nullable ItemLabelGenerator<T> itemLabelGenerator;
        private @Nullable String placeholder;

        private Builder(String id, String label, FilterFieldType type) {
            this.id = Objects.requireNonNull(id, "id");
            this.label = Objects.requireNonNull(label, "label");
            this.type = Objects.requireNonNull(type, "type");
        }

        public Builder<T> items(List<T> items) {
            this.staticItems = List.copyOf(items);
            return this;
        }

        public Builder<T> itemsSupplier(Supplier<List<T>> supplier) {
            this.itemsSupplier = Objects.requireNonNull(supplier, "supplier");
            return this;
        }

        public Builder<T> itemLabel(ItemLabelGenerator<T> generator) {
            this.itemLabelGenerator = Objects.requireNonNull(generator, "generator");
            return this;
        }

        public Builder<T> placeholder(String placeholder) {
            this.placeholder = Objects.requireNonNull(placeholder, "placeholder");
            return this;
        }

        public FilterFieldConfig<T> build() {
            return new FilterFieldConfig<>(this);
        }
    }
}
