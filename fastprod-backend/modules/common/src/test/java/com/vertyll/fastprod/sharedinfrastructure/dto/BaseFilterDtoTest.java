package com.vertyll.fastprod.sharedinfrastructure.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseFilterDtoTest {
    @Test
    void aFilterWithNothingSetReadsTheFirstPageById() {
        BaseFilterDto filter = new BaseFilterDto(null, null, null, null);

        assertEquals(0, filter.page());
        assertEquals(10, filter.size());
        assertEquals("id", filter.sortBy());
        assertEquals("ASC", filter.sortDirection());
    }

    @Test
    void aPageOrSizeOutOfRangeFallsBackToTheDefault() {
        assertEquals(0, new BaseFilterDto(-1, 10, null, null).page());
        assertEquals(10, new BaseFilterDto(0, 0, null, null).size());
        assertEquals(10, new BaseFilterDto(0, 101, null, null).size());
    }

    @Test
    void theLargestSizeIsAllowed() {
        assertEquals(100, new BaseFilterDto(0, 100, null, null).size());
    }

    @Test
    void blankSortingFallsBackToTheDefault() {
        BaseFilterDto filter = new BaseFilterDto(0, 10, "  ", " ");

        assertEquals("id", filter.sortBy());
        assertEquals("ASC", filter.sortDirection());
    }

    @Test
    void sortingIsTrimmedAndTheDirectionUppercased() {
        BaseFilterDto filter = new BaseFilterDto(0, 10, " name ", " desc ");

        assertEquals("name", filter.sortBy());
        assertEquals("DESC", filter.sortDirection());
    }

    @Test
    void thePageableCarriesThePageAndTheSorting() {
        Pageable pageable = new BaseFilterDto(2, 20, "name", "desc").toPageable();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "name"), pageable.getSort());
    }

    @Test
    void aDirectionThatIsNotDescendingSortsAscending() {
        Pageable pageable = new BaseFilterDto(0, 10, "name", "sideways").toPageable();

        assertEquals(Sort.by(Sort.Direction.ASC, "name"), pageable.getSort());
    }
}
