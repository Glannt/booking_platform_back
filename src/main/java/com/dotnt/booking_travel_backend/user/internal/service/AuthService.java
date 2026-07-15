package com.dotnt.booking_travel_backend.user.internal.service;

import com.dotnt.booking_travel_backend.user.api.*;
import com.dotnt.booking_travel_backend.user.internal.entity.*;
import com.dotnt.booking_travel_backend.user.internal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UserDto register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email is already in use!");
        }

        // SECURITY: Force CUSTOMER role on registration — never allow client-supplied role
        Role role = Role.CUSTOMER;

        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullname(request.getFullname())
                .role(role)
                .status("active")
                .kakaoTalkId(request.getKakaoTalkId())
                .build();

        User savedUser = userRepository.save(user);

        // Publish modulith creation event
        eventPublisher.publishEvent(new UserCreatedEvent(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name()));

        return toDto(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("User account is not active");
        }

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String refreshTokenString = UUID.randomUUID().toString();

        // Save refresh token, delete old ones for this user first
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(30)) // 30 days expiry
                .build();
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .user(toDto(user))
                .build();
    }

    @Transactional
    public LoginResponse refresh(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token was expired. Please make a new signin request");
        }

        User user = refreshToken.getUser();
        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String newRefreshTokenString = UUID.randomUUID().toString();

        // Rotate refresh token
        refreshTokenRepository.delete(refreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenString)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(30))
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshTokenString)
                .user(toDto(user))
                .build();
    }

    @Transactional
    public void logout(String accessToken, String refreshTokenString) {
        // 1. Delete refresh token from DB if present
        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            refreshTokenRepository.deleteByToken(refreshTokenString);
        }

        // 2. Blacklist access token
        if (accessToken != null && !accessToken.isBlank()) {
            if (accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);
            }

            if (jwtService.validateToken(accessToken)) {
                Date expirationDate = jwtService.getExpirationDateFromToken(accessToken);
                LocalDateTime expiry = expirationDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                        .token(accessToken)
                        .expiryDate(expiry)
                        .build();

                // Prevent duplicate blacklisting
                if (!blacklistedTokenRepository.existsByToken(accessToken)) {
                    blacklistedTokenRepository.save(blacklistedToken);
                }
            }
        }
    }

    public UserDto whoami(String email) {
        return userRepository.findByEmail(email).map(this::toDto).orElse(null);
    }

    private UserDto toDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullname(user.getFullname())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus())
                .build();
    }
}
