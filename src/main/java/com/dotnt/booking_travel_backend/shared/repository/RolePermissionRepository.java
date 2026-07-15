package com.dotnt.booking_travel_backend.shared.repository;

import com.dotnt.booking_travel_backend.shared.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRole(String role);
    boolean existsByRoleAndPermissionCode(String role, String permissionCode);

    @Modifying
    void deleteByRole(String role);
}
