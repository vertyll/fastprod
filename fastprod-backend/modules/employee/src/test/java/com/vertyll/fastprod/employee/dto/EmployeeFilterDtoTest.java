package com.vertyll.fastprod.employee.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmployeeFilterDtoTest {
    private static EmployeeFilterDto sortedBy(String sortBy) {
        return new EmployeeFilterDto(null, null, sortBy, null, null, null, null, null, null, null);
    }

    @Test
    void aSortFieldEmployeesHaveIsKept() {
        assertEquals("lastName", sortedBy(" lastName ").sortBy());
    }

    @Test
    void aSortFieldEmployeesDoNotHaveFallsBackToTheId() {
        assertEquals("id", sortedBy("password").sortBy());
    }

    @Test
    void aBlankSortFieldFallsBackToTheId() {
        assertEquals("id", sortedBy("  ").sortBy());
    }

    @Test
    void textFiltersAreTrimmedAndABlankOneIsNoFilter() {
        EmployeeFilterDto filter =
                new EmployeeFilterDto(null, null, null, null, " Ada ", "  ", null, null, null, " x ");

        assertEquals("Ada", filter.firstName());
        assertNull(filter.lastName());
        assertNull(filter.email());
        assertEquals("x", filter.search());
    }

    @Test
    void thePageableUsesTheChecksOfTheBaseFilter() {
        EmployeeFilterDto filter = new EmployeeFilterDto(1, 500, "email", "desc", null, null, null, null, null, null);

        assertEquals(1, filter.toPageable().getPageNumber());
        assertEquals(10, filter.toPageable().getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "email"), filter.toPageable().getSort());
    }
}
