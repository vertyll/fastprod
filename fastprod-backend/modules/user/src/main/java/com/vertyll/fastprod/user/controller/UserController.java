package com.vertyll.fastprod.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.vertyll.fastprod.common.response.ApiResponse;
import com.vertyll.fastprod.user.dto.ProfileUpdateDto;
import com.vertyll.fastprod.user.dto.UserCreateDto;
import com.vertyll.fastprod.user.dto.UserResponseDto;
import com.vertyll.fastprod.user.dto.UserUpdateDto;
import com.vertyll.fastprod.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @RequestBody @Valid UserCreateDto dto) {
        UserResponseDto user = userService.createUser(dto);
        return ApiResponse.buildResponse(user, "User created successfully", HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update existing user")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id, @RequestBody @Valid UserUpdateDto dto) {
        UserResponseDto user = userService.updateUser(id, dto);
        return ApiResponse.buildResponse(user, "User updated successfully", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ApiResponse.buildResponse(user, "User retrieved successfully", HttpStatus.OK);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(
            Authentication authentication) {
        UserResponseDto user = userService.getCurrentUser(authentication.getName());
        return ApiResponse.buildResponse(user, "Profile retrieved successfully", HttpStatus.OK);
    }

    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            @RequestBody @Valid ProfileUpdateDto dto, Authentication authentication) {
        UserResponseDto user = userService.updateCurrentUserProfile(authentication.getName(), dto);
        return ApiResponse.buildResponse(user, "Profile updated successfully", HttpStatus.OK);
    }
}
