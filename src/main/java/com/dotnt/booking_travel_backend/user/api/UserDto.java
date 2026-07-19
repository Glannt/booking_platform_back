package com.dotnt.booking_travel_backend.user.api;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String phone;
    private String fullname;
    private String role;
    private String status;
    private String kakaoTalkId;
    private String zaloId;
}
