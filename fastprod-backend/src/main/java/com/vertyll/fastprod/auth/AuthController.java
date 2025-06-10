package com.vertyll.fastprod.auth;

import com.vertyll.fastprod.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth management APIs")
class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody @Valid RegisterRequestDto request
    ) throws MessagingException {
        authService.register(request);
        return ApiResponse.buildResponse(
                null,
                "User registered successfully",
                HttpStatus.OK
        );
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Authenticate user and get token")
    public ResponseEntity<ApiResponse<AuthResponseDto>> authenticate(
            @RequestBody @Valid AuthRequestDto request
    ) {
        AuthResponseDto response = authService.authenticate(request);
        return ApiResponse.buildResponse(
                response,
                "Authentication successful",
                HttpStatus.OK
        );
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify user account with code")
    public ResponseEntity<ApiResponse<Void>> verifyAccount(@RequestParam String code) {
        authService.verifyAccount(code);
        return ApiResponse.buildResponse(
                null,
                "Account verified successfully",
                HttpStatus.OK
        );
    }
}