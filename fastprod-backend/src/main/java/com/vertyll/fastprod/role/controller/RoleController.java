package com.vertyll.fastprod.role.controller;

import com.vertyll.fastprod.role.dto.RoleCreateDto;
import com.vertyll.fastprod.role.dto.RoleResponseDto;
import com.vertyll.fastprod.role.enums.RoleType;
import com.vertyll.fastprod.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> createRole(@RequestBody @Valid RoleCreateDto dto) {
        return ResponseEntity.ok(roleService.createRole(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDto> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @GetMapping("/types")
    public ResponseEntity<List<RoleType>> getAllRoleTypes() {
        return ResponseEntity.ok(Arrays.asList(RoleType.values()));
    }
}