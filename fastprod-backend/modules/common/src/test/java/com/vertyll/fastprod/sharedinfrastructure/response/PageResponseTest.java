package com.vertyll.fastprod.sharedinfrastructure.response;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageResponseTest {
    @Test
    void aPageIsDescribedByItsPosition() {
        PageResponse<String> response = PageResponse.from(new PageImpl<>(List.of("c"), PageRequest.of(1, 2), 3));

        assertEquals(List.of("c"), response.content());
        assertEquals(1, response.pageNumber());
        assertEquals(2, response.pageSize());
        assertEquals(3, response.totalElements());
        assertEquals(2, response.totalPages());
        assertFalse(response.first());
        assertTrue(response.last());
        assertFalse(response.empty());
    }

    @Test
    void anEmptyPageSaysSo() {
        PageResponse<String> response = PageResponse.from(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        assertTrue(response.empty());
        assertTrue(response.first());
    }

    @Test
    void theContentDoesNotFollowTheListItCameFrom() {
        List<String> content = new ArrayList<>(List.of("a"));
        PageResponse<String> response = new PageResponse<>(content, 0, 10, 1, 1, true, true, false);

        content.add("b");

        assertEquals(List.of("a"), response.content());
    }
}
