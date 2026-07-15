package com.dotnt.booking_travel_backend.shared.config;

import com.dotnt.booking_travel_backend.shared.entity.Permission;
import com.dotnt.booking_travel_backend.shared.entity.RolePermission;
import com.dotnt.booking_travel_backend.shared.repository.PermissionRepository;
import com.dotnt.booking_travel_backend.shared.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class PermissionDataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    private static final Map<String, String> ALL_PERMISSIONS = Map.ofEntries(
            // Airport
            Map.entry("AIRPORT_CREATE", "Create airports"),
            Map.entry("AIRPORT_UPDATE", "Update airports"),
            Map.entry("AIRPORT_DELETE", "Delete airports"),
            Map.entry("AIRPORT_ROUTE_CREATE", "Create airport routes"),
            Map.entry("AIRPORT_ROUTE_UPDATE", "Update airport routes"),
            Map.entry("AIRPORT_ROUTE_DELETE", "Delete airport routes"),
            Map.entry("SURCHARGE_CREATE", "Create surcharge rules"),
            Map.entry("SURCHARGE_UPDATE", "Update surcharge rules"),
            Map.entry("SURCHARGE_DELETE", "Delete surcharge rules"),

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
            Map.entry("INTERCITY_SURCHARGE_CREATE", "Create intercity surcharges"),
            Map.entry("INTERCITY_SURCHARGE_UPDATE", "Update intercity surcharges"),
            Map.entry("INTERCITY_SURCHARGE_DELETE", "Delete intercity surcharges"),

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
            Map.entry("ROLE_UPDATE", "Update role permissions")
    );

    private static final List<String> ADMIN_PERMISSIONS = ALL_PERMISSIONS.keySet().stream().toList();

    private static final List<String> STAFF_PERMISSIONS = List.of(
            "BOOKING_VIEW_ALL", "BOOKING_UPDATE",
            "TOUR_UPDATE",
            "VEHICLE_UPDATE",
            "INTERCITY_ROUTE_UPDATE",
            "MEDIA_CREATE",
            "FEEDBACK_UPDATE",
            "ROLE_VIEW"
    );

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        new Thread(() -> {
            try {
                seedPermissions();
                seedRolePermissions();
                log.info("Permission data initialization complete.");
            } catch (Exception e) {
                log.error("Failed to initialize permission data", e);
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
    public void seedRolePermissions() {
        assignPermissionsToRole("ADMIN", ADMIN_PERMISSIONS);
        assignPermissionsToRole("STAFF", STAFF_PERMISSIONS);
        log.info("Role permission mappings seeded.");
    }

    private void assignPermissionsToRole(String role, List<String> permissionCodes) {
        List<Permission> perms = permissionRepository.findByCodeIn(permissionCodes);
        int created = 0;
        for (Permission perm : perms) {
            if (!rolePermissionRepository.existsByRoleAndPermissionCode(role, perm.getCode())) {
                rolePermissionRepository.save(RolePermission.builder()
                        .role(role)
                        .permission(perm)
                        .build());
                created++;
            }
        }
        log.info("Role {}: assigned {} new permissions ({} total)", role, created, perms.size());
    }
}
