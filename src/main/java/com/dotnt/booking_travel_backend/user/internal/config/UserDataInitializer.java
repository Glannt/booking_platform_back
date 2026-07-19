package com.dotnt.booking_travel_backend.user.internal.config;

import com.dotnt.booking_travel_backend.user.internal.entity.Role;
import com.dotnt.booking_travel_backend.user.internal.entity.User;
import com.dotnt.booking_travel_backend.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(5)
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SEED_ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    @Value("${SEED_STAFF_PASSWORD:staff123}")
    private String staffPassword;

    @Value("${SEED_CUSTOMER_PASSWORD:customer123}")
    private String customerPassword;

    @Value("${SEED_MODERATOR_PASSWORD:moderator123}")
    private String moderatorPassword;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Seeding default users...");

            User admin = User.builder()
                    .email("admin@gmail.com")
                    .phone("0987654321")
                    .password(passwordEncoder.encode(adminPassword))
                    .fullname("System Admin")
                    .role(Role.ADMIN)
                    .status("active")
                    .build();

            User staff = User.builder()
                    .email("staff@gmail.com")
                    .phone("0987654322")
                    .password(passwordEncoder.encode(staffPassword))
                    .fullname("Staff Member")
                    .role(Role.STAFF)
                    .status("active")
                    .build();

            User customer = User.builder()
                    .email("customer@gmail.com")
                    .phone("0987654323")
                    .password(passwordEncoder.encode(customerPassword))
                    .fullname("Standard Customer")
                    .role(Role.CUSTOMER)
                    .status("active")
                    .build();

            User moderator = User.builder()
                    .email("moderator@gmail.com")
                    .phone("0987654324")
                    .password(passwordEncoder.encode(moderatorPassword))
                    .fullname("Moderator User")
                    .role(Role.MODERATOR)
                    .status("active")
                    .build();

            userRepository.saveAll(List.of(admin, staff, customer, moderator));
            log.info("Successfully seeded default users!");
        }
    }
}
