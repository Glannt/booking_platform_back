package com.dotnt.booking_travel_backend.user.internal.controller;

import com.dotnt.booking_travel_backend.shared.dto.ApiResponse;
import com.dotnt.booking_travel_backend.user.api.*;
import com.dotnt.booking_travel_backend.user.internal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody CreateUserRequest request) {
        UserDto registeredUser = authService.register(request);
        ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("User registered successfully")
                .data(registeredUser)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login successful")
                .data(loginResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestBody TokenRefreshRequest request) {
        LoginResponse loginResponse = authService.refresh(request);
        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Token refreshed successfully")
                .data(loginResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/whoami")
    public ResponseEntity<ApiResponse<UserDto>> whoami() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof String email)) {
            return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Not authenticated")
                    .data(null)
                    .build());
        }
        UserDto user = authService.whoami(email);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("OK")
                .data(user)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) TokenRefreshRequest refreshRequest) {
        String refreshToken = refreshRequest != null ? refreshRequest.getRefreshToken() : null;
        authService.logout(authHeader, refreshToken);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Logout successful")
                .build();
        return ResponseEntity.ok(response);
    }
}
