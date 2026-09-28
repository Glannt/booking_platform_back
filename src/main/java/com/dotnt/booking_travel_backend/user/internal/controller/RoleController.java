package com.dotnt.booking_travel_backend.user.internal.controller;

import com.dotnt.booking_travel_backend.shared.entity.Permission;
import com.dotnt.booking_travel_backend.shared.service.PermissionService;
import com.dotnt.booking_travel_backend.user.internal.dto.CreatePermissionRequest;
import com.dotnt.booking_travel_backend.user.internal.dto.PermissionDto;
import com.dotnt.booking_travel_backend.user.internal.dto.RoleDto;
import com.dotnt.booking_travel_backend.user.internal.dto.UpdateRolePermissionsRequest;
import com.dotnt.booking_travel_backend.user.internal.entity.Role;
import com.dotnt.booking_travel_backend.user.internal.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final RoleRepository roleRepository;

    @GetMapping
    @Operation(summary = "Get all roles with their permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<Role> roles = roleRepository.findAllWithPermissions();
        List<RoleDto> dtos = roles.stream()
                .filter(r -> !Role.CODE_CUSTOMER.equalsIgnoreCase(r.getCode())) // CUSTOMER has no admin-granted permissions
                .map(r -> RoleDto.builder()
                        .name(r.getCode())
                        .permissions(r.getPermissions().stream()
                                .map(p -> p.getCode())
                                .sorted()
                                .toList())
                        .build())
                .toList();
        return ResponseEntity.ok(dtos);
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
        Role targetRole = roleRepository.findByCodeIgnoreCase(role)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + role));

        permissionService.setPermissionsForRole(targetRole.getCode(), request.getPermissions());
        return ResponseEntity.ok(RoleDto.builder()
                .name(targetRole.getCode())
                .permissions(permissionService.getPermissionsForRole(targetRole.getCode()))
                .build());
    }

    @PostMapping("/permissions")
    @Operation(summary = "Create a new permission")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<PermissionDto> createPermission(@RequestBody CreatePermissionRequest request) {
        Permission permission = permissionService.createPermission(
                request.getCode(), request.getName(), request.getDescription());
        return ResponseEntity.status(HttpStatus.CREATED).body(toPermissionDto(permission));
    }

    @PutMapping("/permissions/{code}")
    @Operation(summary = "Update name/description of an existing permission")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<PermissionDto> updatePermission(
            @PathVariable String code,
            @RequestBody CreatePermissionRequest request) {
        Permission permission = permissionService.updatePermission(code, request.getName(), request.getDescription());
        return ResponseEntity.ok(toPermissionDto(permission));
    }

    private PermissionDto toPermissionDto(Permission permission) {
        return PermissionDto.builder()
                .code(permission.getCode())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}
