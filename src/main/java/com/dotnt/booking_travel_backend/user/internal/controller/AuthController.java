package com.dotnt.booking_travel_backend.user.internal.controller;

import com.dotnt.booking_travel_backend.shared.dto.ApiResponse;
import com.dotnt.booking_travel_backend.user.api.*;
import com.dotnt.booking_travel_backend.user.internal.service.AuthService;
import com.dotnt.booking_travel_backend.user.internal.service.PasswordResetService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    private void setAuthCookies(HttpServletResponse response, LoginResponse loginResponse) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", loginResponse.getAccessToken())
                .httpOnly(true)
                .secure(false) // Can set to true if running on HTTPS
                .path("/")
                .maxAge(24 * 60 * 60) // 1 day
                .sameSite("Lax")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(30L * 24 * 60 * 60) // 30 days
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody CreateUserRequest request) {
        UserDto registeredUser = authService.register(request);
        ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("User registered successfully")
                .data(registeredUser)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);
        setAuthCookies(response, loginResponse);
        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login successful")
                .data(loginResponse)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.refresh(request);
        setAuthCookies(response, loginResponse);
        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Token refreshed successfully")
                .data(loginResponse)
                .build();
        return ResponseEntity.ok(apiResponse);
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

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof String email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.<UserDto>builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Not authenticated")
                    .data(null)
                    .build());
        }
        UserDto updatedUser = authService.updateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Profile updated successfully")
                .data(updatedUser)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) TokenRefreshRequest refreshRequest,
            HttpServletResponse response) {
        String refreshToken = refreshRequest != null ? refreshRequest.getRefreshToken() : null;
        authService.logout(authHeader, refreshToken);
        clearAuthCookies(response);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Logout successful")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.getEmail());
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("If an account exists with this email, a reset code has been sent")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<ApiResponse<Void>> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        passwordResetService.verifyCode(request.getEmail(), request.getCode());
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Code verified successfully")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password has been reset successfully")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof String email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.<Void>builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Not authenticated")
                    .build());
        }
        authService.changePassword(email, request.getNewPassword());
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password changed successfully")
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
