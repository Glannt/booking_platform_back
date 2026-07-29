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
@Order(15)
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.admin.email}")
    private String adminEmail;

    @Value("${seed.admin.password}")
    private String adminPassword;

    @Value("${seed.admin.phone}")
    private String adminPhone;

    @Value("${seed.admin.fullname}")
    private String adminFullname;

    @Value("${seed.moderator.email}")
    private String moderatorEmail;

    @Value("${seed.moderator.password}")
    private String moderatorPassword;

    @Value("${seed.moderator.phone}")
    private String moderatorPhone;

    @Value("${seed.moderator.fullname}")
    private String moderatorFullname;

    @Value("${seed.staff.email}")
    private String staffEmail;

    @Value("${seed.staff.password}")
    private String staffPassword;

    @Value("${seed.staff.phone}")
    private String staffPhone;

    @Value("${seed.staff.fullname}")
    private String staffFullname;

    @Value("${seed.customer.email}")
    private String customerEmail;

    @Value("${seed.customer.password}")
    private String customerPassword;

    @Value("${seed.customer.phone}")
    private String customerPhone;

    @Value("${seed.customer.fullname}")
    private String customerFullname;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding default users...");

            User admin = User.builder()
                    .email(adminEmail)
                    .phone(adminPhone)
                    .password(passwordEncoder.encode(adminPassword))
                    .fullname(adminFullname)
                    .role(Role.ADMIN)
                    .status("active")
                    .build();

            User staff = User.builder()
                    .email(staffEmail)
                    .phone(staffPhone)
                    .password(passwordEncoder.encode(staffPassword))
                    .fullname(staffFullname)
                    .role(Role.STAFF)
                    .status("active")
                    .build();

            User customer = User.builder()
                    .email(customerEmail)
                    .phone(customerPhone)
                    .password(passwordEncoder.encode(customerPassword))
                    .fullname(customerFullname)
                    .role(Role.CUSTOMER)
                    .status("active")
                    .build();

            User moderator = User.builder()
                    .email(moderatorEmail)
                    .phone(moderatorPhone)
                    .password(passwordEncoder.encode(moderatorPassword))
                    .fullname(moderatorFullname)
                    .role(Role.MODERATOR)
                    .status("active")
                    .build();

            userRepository.saveAll(List.of(admin, staff, customer, moderator));
            log.info("Successfully seeded default users!");
        }
    }
}
