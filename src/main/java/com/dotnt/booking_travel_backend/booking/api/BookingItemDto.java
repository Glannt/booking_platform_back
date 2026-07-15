package com.dotnt.booking_travel_backend.booking.api;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItemDto {
    private Long id;
    private Long serviceId;
    private Long serviceOptionId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String pickupLocation;
    private String dropoffLocation;
    private String dynamicDetails;
    private String flightNumber;
    private Map<String, String> serviceName;
    private Map<String, String> serviceOptionName;
    private Long vehicleCategoryId;
    private Long intercityBookingDetailId;
    private Long selfDriveBookingDetailId;
    private Long priceId;
    private String priceSnapshot; // JSON string snapshot of applied price
}
