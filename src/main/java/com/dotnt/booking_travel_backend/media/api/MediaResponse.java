package com.dotnt.booking_travel_backend.media.api;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaResponse {
    private MediaDto media;
    private String message;
}
