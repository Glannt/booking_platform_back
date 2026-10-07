package com.dotnt.booking_travel_backend.booking.api;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingItemRequest {
    private Long serviceId;
    private Long serviceOptionId;
    private Integer quantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String pickupLocation;
    private String dropoffLocation;
    private String dynamicDetails;
    private String flightNumber;
    // No unitPrice: the server prices every line (see BookingServiceImpl.createBooking). The
    // browser identifies the line; the server decides what it costs.
}
