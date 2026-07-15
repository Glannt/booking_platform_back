package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDto {
    private Long id;
    private int durationDays;
    private int durationNights;
    private String meetingPoint;
    private Instant startTime;
    private Instant endTime;
    private int minGuest;
    private int maxGuest;
    private boolean active;
}

