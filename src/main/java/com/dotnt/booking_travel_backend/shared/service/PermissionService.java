package com.dotnt.booking_travel_backend.shared.service;

import com.dotnt.booking_travel_backend.shared.entity.Permission;
import com.dotnt.booking_travel_backend.shared.entity.RolePermission;
import com.dotnt.booking_travel_backend.shared.repository.PermissionRepository;
import com.dotnt.booking_travel_backend.shared.repository.RolePermissionRepository;
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
    private final RolePermissionRepository rolePermissionRepository;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

    @Transactional(readOnly = true)
    public List<String> getPermissionsForRole(String role) {
        CacheEntry entry = cache.get(role);
        if (entry != null && !entry.isExpired()) {
            return entry.permissions;
        }

        List<RolePermission> rps = rolePermissionRepository.findByRole(role);
        List<String> codes = rps.stream()
                .map(rp -> rp.getPermission().getCode())
                .toList();

        cache.put(role, new CacheEntry(codes));
        return codes;
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> getAllRolePermissions() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String role : List.of("ADMIN", "STAFF", "CUSTOMER")) {
            result.put(role, getPermissionsForRole(role));
        }
        return result;
    }

    public void setPermissionsForRole(String role, List<String> permissionCodes) {
        rolePermissionRepository.deleteByRole(role);
        List<Permission> perms = permissionRepository.findByCodeIn(permissionCodes);
        List<RolePermission> rps = perms.stream()
                .map(p -> RolePermission.builder().role(role).permission(p).build())
                .toList();
        rolePermissionRepository.saveAll(rps);
        cache.remove(role);
        log.info("Updated permissions for role {}: {} permissions set", role, perms.size());
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
