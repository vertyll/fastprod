package com.vertyll.fastprod.modules.employee.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vertyll.fastprod.shared.filters.FilterFieldConfig;
import com.vertyll.fastprod.shared.filters.FilterFieldType;
import com.vertyll.fastprod.shared.filters.FiltersValue;
import com.vertyll.fastprod.shared.security.RoleType;

public final class EmployeeFilters {

    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String EMAIL = "email";
    private static final String IS_VERIFIED = "isVerified";
    private static final String ROLES = "roles";
    private static final String VERIFIED_LABEL = "Verified";

    private EmployeeFilters() {
    }

    @SuppressWarnings("java:S1452")
    public static List<FilterFieldConfig<?>> configs() {
        return List.of(
            FilterFieldConfig.builder(FIRST_NAME, "First name", FilterFieldType.TEXT)
                .placeholder("Search first name")
                .build(),
            FilterFieldConfig.builder(LAST_NAME, "Last name", FilterFieldType.TEXT)
                .placeholder("Search last name")
                .build(),
            FilterFieldConfig.builder(EMAIL, "Email", FilterFieldType.TEXT).placeholder("Search email").build(),
            FilterFieldConfig.builder(IS_VERIFIED, VERIFIED_LABEL, FilterFieldType.SELECT)
                .items(List.of(true, false))
                .itemLabel(v -> {
                    if (v == null)
                        return "";
                    if (v instanceof Boolean b)
                        return Boolean.TRUE.equals(b) ? VERIFIED_LABEL : "Not verified";
                    return Boolean.parseBoolean(String.valueOf(v)) ? VERIFIED_LABEL : "Not verified";
                })
                .placeholder("Any")
                .build(),
            FilterFieldConfig.<RoleType>builder(ROLES, "Roles", FilterFieldType.MULTISELECT)
                .items(java.util.Arrays.stream(RoleType.values()).toList())
                .itemLabel(v -> {
                    if (v == null)
                        return "";
                    return v.name();
                })
                .placeholder("Any roles")
                .build()
        );
    }

    public static FiltersValue normalize(FiltersValue raw) {
        Objects.requireNonNull(raw, "raw");
        FiltersValue out = FiltersValue.empty();

        raw.get(FIRST_NAME, String.class).ifPresent(value -> out.set(FIRST_NAME, value));
        raw.get(LAST_NAME, String.class).ifPresent(value -> out.set(LAST_NAME, value));
        raw.get(EMAIL, String.class).ifPresent(value -> out.set(EMAIL, value));
        raw.get(IS_VERIFIED, Boolean.class).ifPresent(value -> out.set(IS_VERIFIED, value));
        raw.get(ROLES, Object.class).ifPresent(value -> out.set(ROLES, toRoleNames(value)));

        return out;
    }

    private static List<String> toRoleNames(Object rolesValue) {
        if (rolesValue instanceof Collection<?> col) {
            return col.stream()
                .filter(Objects::nonNull)
                .map(EmployeeFilters::toRoleName)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(ArrayList::new));
        }
        return List.of(toRoleName(rolesValue));
    }

    private static String toRoleName(Object role) {
        return role instanceof RoleType rt ? rt.name() : String.valueOf(role);
    }
}
