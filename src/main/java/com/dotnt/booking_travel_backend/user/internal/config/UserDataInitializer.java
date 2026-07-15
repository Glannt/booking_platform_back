package com.dotnt.booking_travel_backend.user.internal.config;

import com.dotnt.booking_travel_backend.user.internal.entity.Role;
import com.dotnt.booking_travel_backend.user.internal.entity.User;
import com.dotnt.booking_travel_backend.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Seeding default users...");

            User admin = User.builder()
                    .email("admin@gmail.com")
                    .phone("0987654321")
                    .password(passwordEncoder.encode("admin123"))
                    .fullname("System Admin")
                    .role(Role.ADMIN)
                    .status("active")
                    .build();

            User staff = User.builder()
                    .email("staff@gmail.com")
                    .phone("0987654322")
                    .password(passwordEncoder.encode("staff123"))
                    .fullname("Staff Member")
                    .role(Role.STAFF)
                    .status("active")
                    .build();

            User customer = User.builder()
                    .email("customer@gmail.com")
                    .phone("0987654323")
                    .password(passwordEncoder.encode("customer123"))
                    .fullname("Standard Customer")
                    .role(Role.CUSTOMER)
                    .status("active")
                    .build();

            userRepository.saveAll(List.of(admin, staff, customer));
            log.info("Successfully seeded default users!");
        }
    }
}
