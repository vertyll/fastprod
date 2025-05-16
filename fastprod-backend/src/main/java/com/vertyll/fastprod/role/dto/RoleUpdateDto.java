package com.vertyll.fastprod.role.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleUpdateDto {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
