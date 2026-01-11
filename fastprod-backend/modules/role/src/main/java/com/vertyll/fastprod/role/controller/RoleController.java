package com.vertyll.fastprod.role.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.vertyll.fastprod.common.response.ApiResponse;
import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.dto.RoleUpdateDto;
import com.vertyll.fastprod.role.enums.RoleType;
import com.vertyll.fastprod.role.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management APIs")
class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new role")
    public ResponseEntity<ApiResponse<RoleResponseDto>> createRole(
            @RequestBody @Valid RoleCreateDto dto) {
        RoleResponseDto role = roleService.createRole(dto);
        return ApiResponse.buildResponse(role, "Role created successfully", HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update existing role")
    public ResponseEntity<ApiResponse<RoleResponseDto>> updateRole(
            @PathVariable Long id, @RequestBody @Valid RoleUpdateDto dto) {
        RoleResponseDto role = roleService.updateRole(id, dto);
        return ApiResponse.buildResponse(role, "Role updated successfully", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<ApiResponse<RoleResponseDto>> getRole(@PathVariable Long id) {
        RoleResponseDto role = roleService.getRoleById(id);
        return ApiResponse.buildResponse(role, "Role retrieved successfully", HttpStatus.OK);
    }

    @GetMapping("/types")
    @Operation(summary = "Get all available role types")
    public ResponseEntity<ApiResponse<List<RoleType>>> getAllRoleTypes() {
        List<RoleType> types = Arrays.asList(RoleType.values());
        return ApiResponse.buildResponse(types, "Role types retrieved successfully", HttpStatus.OK);
    }
}
