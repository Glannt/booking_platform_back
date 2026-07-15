package com.dotnt.booking_travel_backend.user.api;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UserDto user;
    private String message;
}
