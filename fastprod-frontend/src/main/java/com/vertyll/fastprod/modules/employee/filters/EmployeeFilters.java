package com.vertyll.fastprod.modules.employee.filters;

import com.vertyll.fastprod.shared.filters.FilterFieldConfig;
import com.vertyll.fastprod.shared.filters.FilterFieldType;
import com.vertyll.fastprod.shared.filters.FiltersValue;
import com.vertyll.fastprod.shared.security.RoleType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class EmployeeFilters {

    private EmployeeFilters() {
    }

    public static List<FilterFieldConfig<?>> configs() {
        return List.of(
                FilterFieldConfig.builder(
                                "firstName",
                                "First name",
                                FilterFieldType.TEXT
                        )
                        .placeholder("Search first name")
                        .build(),

                FilterFieldConfig.builder(
                                "lastName",
                                "Last name",
                                FilterFieldType.TEXT
                        )
                        .placeholder("Search last name")
                        .build(),

                FilterFieldConfig.builder(
                                "email",
                                "Email",
                                FilterFieldType.TEXT
                        )
                        .placeholder("Search email")
                        .build(),

                FilterFieldConfig.builder(
                                "isVerified",
                                "Verified",
                                FilterFieldType.SELECT
                        )
                        .items(List.of(Boolean.TRUE, Boolean.FALSE))
                        .itemLabel(v -> {
                            if (v == null) return "";
                            if (v instanceof Boolean b) return b ? "Verified" : "Not verified";
                            return Boolean.parseBoolean(String.valueOf(v)) ? "Verified" : "Not verified";
                        })
                        .placeholder("Any")
                        .build(),

                FilterFieldConfig.<RoleType>builder(
                                "roles",
                                "Roles",
                                FilterFieldType.MULTISELECT
                        )
                        .items(java.util.Arrays.stream(RoleType.values()).toList())
                        .itemLabel(v -> {
                            if (v == null) return "";
                            return v.name();
                        })
                        .placeholder("Any roles")
                        .build()
        );
    }

    public static FiltersValue normalize(FiltersValue raw) {
        FiltersValue out = FiltersValue.empty();
        if (raw == null) return out;

        out.set("firstName", raw.get("firstName"));
        out.set("lastName", raw.get("lastName"));
        out.set("email", raw.get("email"));
        out.set("isVerified", raw.get("isVerified"));

        Object rolesVal = raw.get("roles");
        if (rolesVal instanceof Collection<?> col) {
            List<String> names = col.stream()
                    .filter(Objects::nonNull)
                    .map(o -> {
                        if (o instanceof RoleType rt) return rt.name();
                        return String.valueOf(o);
                    })
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toCollection(ArrayList::new));
            out.set("roles", names);
        } else if (rolesVal != null) {
            if (rolesVal instanceof RoleType rt) {
                out.set("roles", List.of(rt.name()));
            } else {
                out.set("roles", List.of(String.valueOf(rolesVal)));
            }
        }

        return out;
    }
}
