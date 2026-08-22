package com.dotnt.booking_travel_backend.user.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullname;

    @Pattern(regexp = "^$|^[0-9]{9,15}$", message = "Phone must be 9-15 digits")
    private String phone;

    @Pattern(regexp = "^$|^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,128}$", message = "Password must be at least 8 characters long, contain at least 1 uppercase letter and 1 special character")
    private String password;

    @Pattern(regexp = "^$|ADMIN|CUSTOMER|STAFF|MODERATOR", message = "Role must be one of: ADMIN, CUSTOMER, STAFF, MODERATOR")
    private String role;

    @Pattern(regexp = "^$|active|inactive|blocked", message = "Status must be one of: active, inactive, blocked")
    private String status;

    @Size(max = 500, message = "Reason inactive must not exceed 500 characters")
    private String reasonInactive;

    private String kakaoTalkId;
    private String zaloId;
    private String whatsapp;
}
