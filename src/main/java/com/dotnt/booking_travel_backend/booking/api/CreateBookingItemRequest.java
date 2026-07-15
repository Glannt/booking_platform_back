package com.dotnt.booking_travel_backend.booking.api;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingItemRequest {
    private Long serviceId;
    private Long serviceOptionId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String pickupLocation;
    private String dropoffLocation;
    private String dynamicDetails;
    private String flightNumber;
}
