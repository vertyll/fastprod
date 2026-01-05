package com.vertyll.fastprod.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateDto(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName) {}
