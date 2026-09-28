package com.dotnt.booking_travel_backend.shared.service;

import com.dotnt.booking_travel_backend.shared.entity.Permission;
import com.dotnt.booking_travel_backend.shared.repository.PermissionRepository;
import com.dotnt.booking_travel_backend.user.internal.entity.Role;
import com.dotnt.booking_travel_backend.user.internal.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

    @Transactional(readOnly = true)
    public List<String> getPermissionsForRole(String roleCode) {
        if (roleCode == null) {
            return Collections.emptyList();
        }
        CacheEntry entry = cache.get(roleCode.toUpperCase());
        if (entry != null && !entry.isExpired()) {
            return entry.permissions;
        }

        Optional<Role> roleOpt = roleRepository.findByCodeWithPermissions(roleCode.toUpperCase());
        List<String> codes = roleOpt.map(role -> role.getPermissions().stream()
                .map(Permission::getCode)
                .sorted()
                .toList())
                .orElse(Collections.emptyList());

        cache.put(roleCode.toUpperCase(), new CacheEntry(codes));
        return codes;
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional
    public Permission createPermission(String code, String name, String description) {
        String normalizedCode = (code == null ? "" : code.trim().toUpperCase());
        if (normalizedCode.isBlank()) {
            throw new IllegalArgumentException("Permission code must not be blank");
        }
        String normalizedName = (name == null ? "" : name.trim());
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Permission name must not be blank");
        }
        if (permissionRepository.existsByCode(normalizedCode)) {
            throw new IllegalArgumentException("Permission code already exists: " + normalizedCode);
        }
        Permission permission = Permission.builder()
                .code(normalizedCode)
                .name(normalizedName)
                .description(description == null ? null : description.trim())
                .build();
        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission updatePermission(String code, String name, String description) {
        String normalizedName = (name == null ? "" : name.trim());
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("Permission name must not be blank");
        }
        Permission permission = permissionRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + code));
        permission.setName(normalizedName);
        permission.setDescription(description == null ? null : description.trim());
        return permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getAllRolePermissions() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        List<Role> roles = roleRepository.findAllWithPermissions();
        for (Role role : roles) {
            List<String> codes = role.getPermissions().stream()
                    .map(Permission::getCode)
                    .sorted()
                    .toList();
            result.put(role.getCode(), codes);
        }
        return result;
    }

    @Transactional
    public void setPermissionsForRole(String roleCode, List<String> permissionCodes) {
        Role role = roleRepository.findByCodeIgnoreCase(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleCode));

        List<Permission> perms = (permissionCodes != null && !permissionCodes.isEmpty())
                ? permissionRepository.findByCodeIn(permissionCodes)
                : Collections.emptyList();

        role.setPermissions(new HashSet<>(perms));
        roleRepository.save(role);

        cache.remove(roleCode.toUpperCase());
        log.info("Updated permissions for role {}: {} permissions set", roleCode, perms.size());
    }

    public void refreshCache() {
        cache.clear();
    }

    private static class CacheEntry {
        final List<String> permissions;
        final long createdAt;

        CacheEntry(List<String> permissions) {
            this.permissions = List.copyOf(permissions);
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > CACHE_TTL_MS;
        }
    }
}
