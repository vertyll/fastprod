package com.vertyll.fastprod.auth.controller;

import com.vertyll.fastprod.auth.dto.AuthRequestDto;
import com.vertyll.fastprod.auth.dto.AuthResponseDto;
import com.vertyll.fastprod.auth.dto.RegisterRequestDto;
import com.vertyll.fastprod.auth.dto.RegisterResponseDto;
import com.vertyll.fastprod.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody @Valid RegisterRequestDto request) throws MessagingException {
        RegisterResponseDto response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Authenticate user and get token")
    public ResponseEntity<AuthResponseDto> authenticate(@RequestBody @Valid AuthRequestDto request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify user account with code")
    public ResponseEntity<Void> verifyAccount(@RequestParam String code) {
        authService.verifyAccount(code);
        return ResponseEntity.ok().build();
    }
}