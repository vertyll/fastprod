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

    private EmployeeFilters() {}

    @SuppressWarnings("java:S1452")
    public static List<FilterFieldConfig<?>> configs() {
        return List.of(
                FilterFieldConfig.builder(FIRST_NAME, "First name", FilterFieldType.TEXT)
                        .placeholder("Search first name")
                        .build(),
                FilterFieldConfig.builder(LAST_NAME, "Last name", FilterFieldType.TEXT)
                        .placeholder("Search last name")
                        .build(),
                FilterFieldConfig.builder(EMAIL, "Email", FilterFieldType.TEXT)
                        .placeholder("Search email")
                        .build(),
                FilterFieldConfig.builder(IS_VERIFIED, VERIFIED_LABEL, FilterFieldType.SELECT)
                        .items(List.of(true, false))
                        .itemLabel(
                                v -> {
                                    if (v == null) return "";
                                    if (v instanceof Boolean b)
                                        return Boolean.TRUE.equals(b)
                                                ? VERIFIED_LABEL
                                                : "Not verified";
                                    return Boolean.parseBoolean(String.valueOf(v))
                                            ? VERIFIED_LABEL
                                            : "Not verified";
                                })
                        .placeholder("Any")
                        .build(),
                FilterFieldConfig.<RoleType>builder(ROLES, "Roles", FilterFieldType.MULTISELECT)
                        .items(java.util.Arrays.stream(RoleType.values()).toList())
                        .itemLabel(
                                v -> {
                                    if (v == null) return "";
                                    return v.name();
                                })
                        .placeholder("Any roles")
                        .build());
    }

    public static FiltersValue normalize(FiltersValue raw) {
        FiltersValue out = FiltersValue.empty();
        if (raw == null) return out;

        out.set(FIRST_NAME, raw.get(FIRST_NAME));
        out.set(LAST_NAME, raw.get(LAST_NAME));
        out.set(EMAIL, raw.get(EMAIL));
        out.set(IS_VERIFIED, raw.get(IS_VERIFIED));

        Object rolesVal = raw.get(ROLES);
        if (rolesVal instanceof Collection<?> col) {
            List<String> names =
                    col.stream()
                            .filter(Objects::nonNull)
                            .map(
                                    o -> {
                                        if (o instanceof RoleType rt) return rt.name();
                                        return String.valueOf(o);
                                    })
                            .filter(s -> !s.isBlank())
                            .collect(Collectors.toCollection(ArrayList::new));
            out.set(ROLES, names);
        } else if (rolesVal != null) {
            if (rolesVal instanceof RoleType rt) {
                out.set(ROLES, List.of(rt.name()));
            } else {
                out.set(ROLES, List.of(String.valueOf(rolesVal)));
            }
        }

        return out;
    }
}
