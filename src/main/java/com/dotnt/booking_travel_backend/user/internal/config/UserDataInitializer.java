package com.dotnt.booking_travel_backend.user.internal.config;

import com.dotnt.booking_travel_backend.user.internal.entity.Role;
import com.dotnt.booking_travel_backend.user.internal.entity.User;
import com.dotnt.booking_travel_backend.user.internal.repository.RoleRepository;
import com.dotnt.booking_travel_backend.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(15)
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${seed.admin.email:tongnguyenthanhdo@gmail.com}")
    private String adminEmail;

    @Value("${seed.admin.password:Do200904sad@}")
    private String adminPassword;

    @Value("${seed.admin.phone:0987654321}")
    private String adminPhone;

    @Value("${seed.admin.fullname:Minh Khoa Admin}")
    private String adminFullname;

    @Value("${seed.moderator.email:minhkhoa.travel.vn@gmail.com}")
    private String moderatorEmail;

    @Value("${seed.moderator.password:Minhtpt18@}")
    private String moderatorPassword;

    @Value("${seed.moderator.phone:0901234567}")
    private String moderatorPhone;

    @Value("${seed.moderator.fullname:Minh Khoa Moderator}")
    private String moderatorFullname;

    @Override
    public void run(String... args) {
        try {
            // Drop obsolete check constraint if it was created on old column
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
        } catch (Exception e) {
            log.debug("Notice on dropping users_role_check: {}", e.getMessage());
        }

        // Ensure default roles exist in DB
        Role adminRole = roleRepository.findByCode(Role.CODE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .code(Role.CODE_ADMIN)
                        .name("Quản trị viên")
                        .description("Toàn quyền quản trị hệ thống")
                        .build()));

        Role moderatorRole = roleRepository.findByCode(Role.CODE_MODERATOR)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .code(Role.CODE_MODERATOR)
                        .name("Điều phối viên")
                        .description("Quản lý nội dung và điều phối đơn hàng")
                        .build()));

        // Ensure CUSTOMER and STAFF exist
        if (!roleRepository.existsByCode(Role.CODE_CUSTOMER)) {
            roleRepository.save(Role.builder()
                    .code(Role.CODE_CUSTOMER)
                    .name("Khách hàng")
                    .description("Người dùng thông thường")
                    .build());
        }
        if (!roleRepository.existsByCode(Role.CODE_STAFF)) {
            roleRepository.save(Role.builder()
                    .code(Role.CODE_STAFF)
                    .name("Nhân viên")
                    .description("Nhân viên vận hành và chăm sóc khách hàng")
                    .build());
        }

        // Seed Admin user if not exists
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Seeding default admin user: {}", adminEmail);
            User admin = User.builder()
                    .email(adminEmail)
                    .phone(adminPhone)
                    .password(passwordEncoder.encode(adminPassword))
                    .fullname(adminFullname)
                    .role(adminRole)
                    .status("active")
                    .build();
            userRepository.save(admin);
            log.info("Successfully seeded admin user: {}", adminEmail);
        }

        // Seed Moderator user if not exists
        if (!userRepository.existsByEmail(moderatorEmail)) {
            log.info("Seeding default moderator user: {}", moderatorEmail);
            User moderator = User.builder()
                    .email(moderatorEmail)
                    .phone(moderatorPhone)
                    .password(passwordEncoder.encode(moderatorPassword))
                    .fullname(moderatorFullname)
                    .role(moderatorRole)
                    .status("active")
                    .build();
            userRepository.save(moderator);
            log.info("Successfully seeded moderator user: {}", moderatorEmail);
        }
    }
}
