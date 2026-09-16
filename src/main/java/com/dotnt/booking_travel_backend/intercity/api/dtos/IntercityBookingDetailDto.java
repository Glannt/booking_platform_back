package com.dotnt.booking_travel_backend.intercity.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityBookingDetailDto {
    private Long id;
    private Long bookingItemId;
    private Long routeId;
    private Long vehicleCategoryId;
    private String serviceType;
    private String tripType;
    private String pickupAddress;
    private String dropoffAddress;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private LocalDate returnDate;
    private LocalTime returnTime;
    private Integer passengerCount;
    private String note;
    private BigDecimal basePrice;
    private BigDecimal surchargeAmount;
    private BigDecimal totalPrice;
    private String currency;
    private String status;
}
