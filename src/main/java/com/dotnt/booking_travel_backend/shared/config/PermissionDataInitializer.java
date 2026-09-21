package com.dotnt.booking_travel_backend.shared.config;

import com.dotnt.booking_travel_backend.shared.entity.Permission;
import com.dotnt.booking_travel_backend.shared.repository.PermissionRepository;
import com.dotnt.booking_travel_backend.user.internal.entity.Role;
import com.dotnt.booking_travel_backend.user.internal.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class PermissionDataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    private static final Map<String, String> ALL_PERMISSIONS = Map.ofEntries(
            // Airport
            Map.entry("AIRPORT_CREATE", "Create airports"),
            Map.entry("AIRPORT_UPDATE", "Update airports"),
            Map.entry("AIRPORT_DELETE", "Delete airports"),
            Map.entry("AIRPORT_ROUTE_CREATE", "Create airport routes"),
            Map.entry("AIRPORT_ROUTE_UPDATE", "Update airport routes"),
            Map.entry("AIRPORT_ROUTE_DELETE", "Delete airport routes"),
            Map.entry("SURCHARGE_RULE_CREATE", "Create surcharge rules"),
            Map.entry("SURCHARGE_RULE_UPDATE", "Update surcharge rules"),
            Map.entry("SURCHARGE_RULE_DELETE", "Delete surcharge rules"),
            Map.entry("SURCHARGE_MAPPING_MANAGE", "Manage module surcharge mappings"),

            // Tour
            Map.entry("TOUR_CREATE", "Create tours"),
            Map.entry("TOUR_UPDATE", "Update tours"),
            Map.entry("TOUR_DELETE", "Delete tours"),
            Map.entry("TOUR_CATEGORY_CREATE", "Create tour categories"),
            Map.entry("TOUR_CATEGORY_UPDATE", "Update tour categories"),
            Map.entry("TOUR_CATEGORY_DELETE", "Delete tour categories"),

            // Fleet
            Map.entry("VEHICLE_CREATE", "Create vehicles"),
            Map.entry("VEHICLE_UPDATE", "Update vehicles"),
            Map.entry("VEHICLE_DELETE", "Delete vehicles"),
            Map.entry("VEHICLE_CATEGORY_CREATE", "Create vehicle categories"),
            Map.entry("VEHICLE_CATEGORY_UPDATE", "Update vehicle categories"),
            Map.entry("VEHICLE_CATEGORY_DELETE", "Delete vehicle categories"),

            // Rental / Intercity
            Map.entry("INTERCITY_ROUTE_CREATE", "Create intercity routes"),
            Map.entry("INTERCITY_ROUTE_UPDATE", "Update intercity routes"),
            Map.entry("INTERCITY_ROUTE_DELETE", "Delete intercity routes"),
            Map.entry("INTERCITY_PRICE_CREATE", "Create intercity prices"),
            Map.entry("INTERCITY_PRICE_UPDATE", "Update intercity prices"),
            Map.entry("INTERCITY_PRICE_DELETE", "Delete intercity prices"),

            // Booking
            Map.entry("BOOKING_VIEW_ALL", "View all bookings"),
            Map.entry("BOOKING_UPDATE", "Update bookings"),
            Map.entry("BOOKING_DELETE", "Delete bookings"),

            // Media
            Map.entry("MEDIA_CREATE", "Upload media"),
            Map.entry("MEDIA_DELETE", "Delete media"),

            // Language
            Map.entry("LANGUAGE_CREATE", "Create languages"),
            Map.entry("LANGUAGE_UPDATE", "Update languages"),
            Map.entry("LANGUAGE_DELETE", "Delete languages"),

            // Feedback
            Map.entry("FEEDBACK_UPDATE", "Update feedbacks"),
            Map.entry("FEEDBACK_DELETE", "Delete feedbacks"),

            // User
            Map.entry("USER_VIEW", "View users"),
            Map.entry("USER_UPDATE", "Update users"),
            Map.entry("USER_DELETE", "Delete users"),

            // Section
            Map.entry("SECTION_CREATE", "Create sections"),
            Map.entry("SECTION_UPDATE", "Update sections"),
            Map.entry("SECTION_DELETE", "Delete sections"),

            // Role
            Map.entry("ROLE_VIEW", "View roles and permissions"),
            Map.entry("ROLE_UPDATE", "Update role permissions"),

            // Setting
            Map.entry("SETTING_UPDATE", "Update settings"),
            Map.entry("SETTING_DELETE", "Delete settings")
    );

    private static final List<String> ADMIN_PERMISSIONS = ALL_PERMISSIONS.keySet().stream().toList();

    private static final List<String> MODERATOR_PERMISSIONS = List.of(
            // Booking
            "BOOKING_VIEW_ALL", "BOOKING_UPDATE",
            // Tour
            "TOUR_CREATE", "TOUR_UPDATE", "TOUR_CATEGORY_CREATE", "TOUR_CATEGORY_UPDATE",
            // Vehicle
            "VEHICLE_CREATE", "VEHICLE_UPDATE", "VEHICLE_CATEGORY_CREATE", "VEHICLE_CATEGORY_UPDATE",
            // Intercity
            "INTERCITY_ROUTE_CREATE", "INTERCITY_ROUTE_UPDATE", "INTERCITY_PRICE_CREATE", "INTERCITY_PRICE_UPDATE",
            // Airport & Surcharges
            "AIRPORT_CREATE", "AIRPORT_UPDATE", "AIRPORT_ROUTE_CREATE", "AIRPORT_ROUTE_UPDATE",
            "SURCHARGE_RULE_CREATE", "SURCHARGE_RULE_UPDATE", "SURCHARGE_MAPPING_MANAGE",
            // Media & Content
            "MEDIA_CREATE", "MEDIA_DELETE",
            "FEEDBACK_UPDATE", "FEEDBACK_DELETE",
            "SECTION_CREATE", "SECTION_UPDATE", "SECTION_DELETE",
            // Language & Users
            "LANGUAGE_CREATE", "LANGUAGE_UPDATE",
            "USER_VIEW", "USER_UPDATE",
            // Setting
            "SETTING_UPDATE"
    );

    private static final List<String> STAFF_PERMISSIONS = List.of(
            // Daily operations
            "BOOKING_VIEW_ALL", "BOOKING_UPDATE",
            "TOUR_UPDATE",
            "VEHICLE_UPDATE",
            "INTERCITY_ROUTE_UPDATE",
            "AIRPORT_ROUTE_UPDATE",
            "MEDIA_CREATE",
            "FEEDBACK_UPDATE",
            "SECTION_CREATE", "SECTION_UPDATE",
            "USER_VIEW",
            "SETTING_UPDATE"
    );

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        new Thread(() -> {
            try {
                seedPermissions();
                seedRoles();
                seedRolePermissions();
                log.info("Permission and Role data initialization complete.");
            } catch (Exception e) {
                log.error("Failed to initialize permission/role data", e);
            }
        }).start();
    }

    @Transactional
    public void seedPermissions() {
        int created = 0;
        for (var entry : ALL_PERMISSIONS.entrySet()) {
            if (!permissionRepository.existsByCode(entry.getKey())) {
                permissionRepository.save(Permission.builder()
                        .code(entry.getKey())
                        .name(entry.getValue())
                        .description(entry.getKey())
                        .build());
                created++;
            }
        }
        log.info("Seeded {} new permissions ({} total)", created, ALL_PERMISSIONS.size());
    }

    @Transactional
    public void seedRoles() {
        List<Map<String, String>> defaultRoles = List.of(
                Map.of("code", Role.CODE_ADMIN, "name", "Quản trị viên", "description", "Toàn quyền quản trị hệ thống"),
                Map.of("code", Role.CODE_MODERATOR, "name", "Điều phối viên", "description", "Quản lý nội dung và điều phối đơn hàng"),
                Map.of("code", Role.CODE_STAFF, "name", "Nhân viên", "description", "Nhân viên vận hành và chăm sóc khách hàng"),
                Map.of("code", Role.CODE_CUSTOMER, "name", "Khách hàng", "description", "Người dùng thông thường")
        );

        for (var roleDef : defaultRoles) {
            String code = roleDef.get("code");
            if (!roleRepository.existsByCode(code)) {
                roleRepository.save(Role.builder()
                        .code(code)
                        .name(roleDef.get("name"))
                        .description(roleDef.get("description"))
                        .build());
                log.info("Seeded role: {}", code);
            }
        }
    }

    @Transactional
    public void seedRolePermissions() {
        assignPermissionsToRole(Role.CODE_ADMIN, ADMIN_PERMISSIONS);
        assignPermissionsToRole(Role.CODE_MODERATOR, MODERATOR_PERMISSIONS);
        assignPermissionsToRole(Role.CODE_STAFF, STAFF_PERMISSIONS);
        log.info("Role permission mappings seeded.");
    }

    private void assignPermissionsToRole(String roleCode, List<String> permissionCodes) {
        Optional<Role> roleOpt = roleRepository.findByCodeWithPermissions(roleCode);
        if (roleOpt.isEmpty()) {
            return;
        }

        Role role = roleOpt.get();
        Set<Permission> currentPerms = role.getPermissions();
        if (currentPerms == null) {
            currentPerms = new HashSet<>();
        }
        List<Permission> perms = permissionRepository.findByCodeIn(permissionCodes);
        currentPerms.addAll(perms);
        role.setPermissions(currentPerms);
        roleRepository.save(role);
        log.info("Role {}: configured with {} permissions", roleCode, currentPerms.size());
    }
}
