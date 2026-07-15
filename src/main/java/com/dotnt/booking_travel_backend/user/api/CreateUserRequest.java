package com.dotnt.booking_travel_backend.user.api;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
    private String email;
    private String phone;
    private String password;
    private String fullname;
    private String role;
    private String kakaoTalkId;
}
