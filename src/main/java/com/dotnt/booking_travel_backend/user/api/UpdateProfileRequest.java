package com.dotnt.booking_travel_backend.user.api;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String fullname;
    private String phone;
    private String kakaoTalkId;
    private String zaloId;
    private String whatsapp;
}
