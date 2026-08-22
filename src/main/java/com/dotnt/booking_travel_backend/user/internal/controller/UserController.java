package com.dotnt.booking_travel_backend.user.internal.controller;

import com.dotnt.booking_travel_backend.shared.dto.ApiResponse;
import com.dotnt.booking_travel_backend.user.api.AdminCreateUserRequest;
import com.dotnt.booking_travel_backend.user.api.AdminUpdateUserRequest;
import com.dotnt.booking_travel_backend.user.api.UserDto;
import com.dotnt.booking_travel_backend.user.internal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW') or hasAnyRole('ADMIN', 'MODERATOR', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserDto> users = userService.getAllUsers(search, page, size);
        return ResponseEntity.ok(ApiResponse.<Page<UserDto>>builder()
                .statusCode(200)
                .message("Users retrieved successfully")
                .data(users)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW') or hasAnyRole('ADMIN', 'MODERATOR', 'STAFF')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .statusCode(200)
                .message("User retrieved successfully")
                .data(user)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_UPDATE') and hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        UserDto user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .statusCode(200)
                .message("User created successfully")
                .data(user)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        UserDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .statusCode(200)
                .message("User updated successfully")
                .data(user)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .statusCode(200)
                .message("User deleted successfully")
                .build());
    }
}
