package com.vertyll.fastprod.shared.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        Integer pageNumber,
        Integer pageSize,
        Long totalElements,
        Integer totalPages,
        Boolean first,
        Boolean last,
        Boolean empty
) {
}
