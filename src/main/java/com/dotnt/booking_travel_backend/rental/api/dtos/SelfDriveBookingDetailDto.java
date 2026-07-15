package com.dotnt.booking_travel_backend.rental.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDriveBookingDetailDto {
    private Long id;
    private Long bookingItemId;
    private Long vehicleCategoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer rentalDays;
    private String pickupLocation;
    private String dropoffLocation;
    private BigDecimal basePrice;
    private BigDecimal deposit;
    private BigDecimal totalPrice;
    private String currency;
    private String note;
    private String idCardImageUrl;
    private String driverLicenseImageUrl;
    private String contractImageUrl;
    private String depositStatus;
    private String status;
}
