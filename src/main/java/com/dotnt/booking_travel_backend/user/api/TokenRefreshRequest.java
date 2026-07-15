package com.dotnt.booking_travel_backend.user.api;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshRequest {
    private String refreshToken;
}
