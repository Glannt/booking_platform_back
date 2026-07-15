package com.dotnt.booking_travel_backend.user.internal.controller;

import com.dotnt.booking_travel_backend.shared.entity.Permission;
import com.dotnt.booking_travel_backend.shared.service.PermissionService;
import com.dotnt.booking_travel_backend.user.internal.dto.PermissionDto;
import com.dotnt.booking_travel_backend.user.internal.dto.RoleDto;
import com.dotnt.booking_travel_backend.user.internal.dto.UpdateRolePermissionsRequest;
import com.dotnt.booking_travel_backend.user.internal.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Endpoints for managing roles and permissions")
public class RoleController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "Get all roles with their permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<RoleDto> roles = List.of(Role.values()).stream()
                .filter(r -> r != Role.CUSTOMER) // CUSTOMER has no admin-granted permissions
                .map(r -> RoleDto.builder()
                        .name(r.name())
                        .permissions(permissionService.getPermissionsForRole(r.name()))
                        .build())
                .toList();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get all available permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        List<PermissionDto> permissions = permissionService.getAllPermissions().stream()
                .map(p -> PermissionDto.builder()
                        .code(p.getCode())
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .toList();
        return ResponseEntity.ok(permissions);
    }

    @PutMapping("/{role}/permissions")
    @Operation(summary = "Update permissions for a role")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<RoleDto> updateRolePermissions(
            @PathVariable String role,
            @RequestBody UpdateRolePermissionsRequest request) {
        Role.valueOf(role.toUpperCase()); // validate role exists
        permissionService.setPermissionsForRole(role.toUpperCase(), request.getPermissions());
        return ResponseEntity.ok(RoleDto.builder()
                .name(role.toUpperCase())
                .permissions(permissionService.getPermissionsForRole(role.toUpperCase()))
                .build());
    }
}
