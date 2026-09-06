package com.dotnt.booking_travel_backend.booking.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingTimelineDto {
    private Long id;
    private Long bookingId;
    private Long actorId;
    private String actorName;
    private String eventType;
    private String title;
    private String description;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
}
