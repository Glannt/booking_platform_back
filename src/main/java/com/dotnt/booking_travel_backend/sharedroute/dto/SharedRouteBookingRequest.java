package com.dotnt.booking_travel_backend.sharedroute.dto;

import com.dotnt.booking_travel_backend.sharedroute.internal.entity.PickupType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRouteBookingRequest {

    // Customer Contact
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String note;

    // Outbound leg
    private Long directionId;
    private Long scheduleId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tripDate;

    private PickupType pickupType;
    private Long pickupOfficeId;
    private String pickupAddress;

    private PickupType dropoffType;
    private Long dropoffOfficeId;
    private String dropoffAddress;

    @Builder.Default
    private Integer passengerCount = 1;

    // Optional return leg (if round-trip)
    private Boolean isRoundTrip;
    private Long returnDirectionId;
    private Long returnScheduleId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnTripDate;

    private PickupType returnPickupType;
    private Long returnPickupOfficeId;
    private String returnPickupAddress;

    private PickupType returnDropoffType;
    private Long returnDropoffOfficeId;
    private String returnDropoffAddress;
}
