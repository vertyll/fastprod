package com.vertyll.fastprod.common.response;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Getter
@SuperBuilder
public class ValidationErrorResponse extends BaseResponse<Void> {
    private final Map<String, List<String>> errors;
}