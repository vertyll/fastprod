package com.vertyll.fastprod.sharedinfrastructure.response;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ValidationErrorResponse extends BaseResponse<Void> {
    private final Map<String, List<String>> errors;
}
