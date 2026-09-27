package com.vertyll.fastprod.modules.employee.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vertyll.fastprod.shared.filters.FilterFieldConfig;
import com.vertyll.fastprod.shared.filters.FilterFieldType;
import com.vertyll.fastprod.shared.filters.FiltersValue;
import com.vertyll.fastprod.shared.i18n.I18n;
import com.vertyll.fastprod.shared.security.RoleType;

public final class EmployeeFilters {

    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String EMAIL = "email";
    private static final String IS_VERIFIED = "isVerified";
    private static final String ROLES = "roles";
    private static final String VERIFIED_LABEL_KEY = "common.verified";

    private EmployeeFilters() {
    }

    @SuppressWarnings("java:S1452")
    public static List<FilterFieldConfig<?>> configs() {
        return List.of(
            FilterFieldConfig.builder(FIRST_NAME, I18n.t("common.firstName"), FilterFieldType.TEXT)
                .placeholder(I18n.t("employees.filters.firstName"))
                .build(),
            FilterFieldConfig.builder(LAST_NAME, I18n.t("common.lastName"), FilterFieldType.TEXT)
                .placeholder(I18n.t("employees.filters.lastName"))
                .build(),
            FilterFieldConfig.builder(EMAIL, I18n.t("common.email"), FilterFieldType.TEXT)
                .placeholder(I18n.t("employees.filters.email"))
                .build(),
            FilterFieldConfig.builder(IS_VERIFIED, I18n.t(VERIFIED_LABEL_KEY), FilterFieldType.SELECT)
                .items(List.of(true, false))
                .itemLabel(v -> I18n.t(Boolean.TRUE.equals(v) ? VERIFIED_LABEL_KEY : "common.notVerified"))
                .placeholder(I18n.t("employees.filters.any"))
                .build(),
            FilterFieldConfig.<RoleType>builder(ROLES, I18n.t("common.roles"), FilterFieldType.MULTISELECT)
                .items(java.util.Arrays.stream(RoleType.values()).toList())
                .itemLabel(v -> I18n.role(v.name()))
                .placeholder(I18n.t("employees.filters.anyRoles"))
                .build()
        );
    }

    public static FiltersValue normalize(FiltersValue raw) {
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
