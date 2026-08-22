package com.dotnt.booking_travel_backend.user.internal.service;

import com.dotnt.booking_travel_backend.user.api.AdminCreateUserRequest;
import com.dotnt.booking_travel_backend.user.api.AdminUpdateUserRequest;
import com.dotnt.booking_travel_backend.user.api.UserDto;
import com.dotnt.booking_travel_backend.user.internal.entity.Role;
import com.dotnt.booking_travel_backend.user.internal.entity.User;
import com.dotnt.booking_travel_backend.user.internal.repository.RoleRepository;
import com.dotnt.booking_travel_backend.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private String getCurrentUserRole() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return "CUSTOMER";
        for (var authority : auth.getAuthorities()) {
            String authName = authority.getAuthority();
            if (authName.startsWith("ROLE_")) {
                return authName.substring(5);
            }
        }
        return "CUSTOMER";
    }

    private String getCurrentUserEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return auth.getName();
    }

    public Page<UserDto> getAllUsers(String search, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String callerRole = getCurrentUserRole();

        List<String> allowedRoles;
        if ("ADMIN".equalsIgnoreCase(callerRole)) {
            allowedRoles = List.of("ADMIN", "MODERATOR", "STAFF", "CUSTOMER");
        } else if ("MODERATOR".equalsIgnoreCase(callerRole)) {
            allowedRoles = List.of("MODERATOR", "STAFF", "CUSTOMER");
        } else if ("STAFF".equalsIgnoreCase(callerRole)) {
            allowedRoles = List.of("STAFF", "CUSTOMER");
        } else {
            allowedRoles = List.of("CUSTOMER");
        }

        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.searchUsersByRoles(search.trim(), allowedRoles, pageRequest);
        } else {
            users = userRepository.findByRoleCodeInAndDeletedAtIsNull(allowedRoles, pageRequest);
        }
        return users.map(this::toDto);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String callerRole = getCurrentUserRole();
        String targetRole = user.getRoleCode();
        if ("MODERATOR".equalsIgnoreCase(callerRole) && "ADMIN".equalsIgnoreCase(targetRole)) {
            throw new RuntimeException("Access Denied: Moderator cannot view Admin details");
        }
        if ("STAFF".equalsIgnoreCase(callerRole) && ("ADMIN".equalsIgnoreCase(targetRole) || "MODERATOR".equalsIgnoreCase(targetRole))) {
            throw new RuntimeException("Access Denied: Staff cannot view Admin or Moderator details");
        }

        return toDto(user);
    }

    @Transactional
    public UserDto createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findByCodeIgnoreCase(request.getRole())
                .orElseThrow(() -> new RuntimeException("Invalid role: " + request.getRole()));

        String status = (request.getStatus() != null && !request.getStatus().isBlank())
                ? request.getStatus()
                : "active";

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullname(request.getFullname())
                .phone(request.getPhone())
                .role(role)
                .status(status)
                .reasonInactive(request.getReasonInactive())
                .kakaoTalkId(request.getKakaoTalkId())
                .zaloId(request.getZaloId())
                .whatsapp(request.getWhatsapp())
                .build();

        return toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(Long id, AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String callerRole = getCurrentUserRole();
        String callerEmail = getCurrentUserEmail();

        if ("MODERATOR".equalsIgnoreCase(callerRole)) {
            if (callerEmail != null && callerEmail.equalsIgnoreCase(user.getEmail())) {
                throw new RuntimeException("Moderator cannot edit their own account");
            }
            if ("ADMIN".equalsIgnoreCase(user.getRoleCode())) {
                throw new RuntimeException("Moderator cannot edit Admin");
            }
            if (request.getStatus() != null && !request.getStatus().isBlank()) {
                user.setStatus(request.getStatus());
            }
            if (request.getReasonInactive() != null) {
                user.setReasonInactive(request.getReasonInactive());
            }
            return toDto(userRepository.save(user));
        }

        if (!"ADMIN".equalsIgnoreCase(callerRole)) {
            throw new RuntimeException("Access Denied");
        }

        if (request.getFullname() != null) {
            user.setFullname(request.getFullname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            Role role = roleRepository.findByCodeIgnoreCase(request.getRole())
                    .orElseThrow(() -> new RuntimeException("Invalid role: " + request.getRole()));
            user.setRole(role);
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(request.getStatus());
        }
        if (request.getReasonInactive() != null) {
            user.setReasonInactive(request.getReasonInactive());
        }
        if (request.getKakaoTalkId() != null) {
            user.setKakaoTalkId(request.getKakaoTalkId());
        }
        if (request.getZaloId() != null) {
            user.setZaloId(request.getZaloId());
        }
        if (request.getWhatsapp() != null) {
            user.setWhatsapp(request.getWhatsapp());
        }

        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeletedAt(LocalDateTime.now());
        user.setStatus("inactive");
        userRepository.save(user);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullname(user.getFullname())
                .role(user.getRoleCode())
                .status(user.getStatus())
                .reasonInactive(user.getReasonInactive())
                .kakaoTalkId(user.getKakaoTalkId())
                .zaloId(user.getZaloId())
                .whatsapp(user.getWhatsapp())
                .build();
    }
}
