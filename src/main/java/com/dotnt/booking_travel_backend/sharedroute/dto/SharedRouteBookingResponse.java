package com.dotnt.booking_travel_backend.sharedroute.dto;

import com.dotnt.booking_travel_backend.sharedroute.internal.entity.PickupType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRouteBookingResponse {

    private Long bookingGroupId;
    private BigDecimal totalAmount;
    private String status;
    private String customerName;
    private String customerPhone;

    @Builder.Default
    private List<BookingItemDto> bookings = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingItemDto {
        private Long id;
        private Long directionId;
        private String directionType;
        private String originCity;
        private String destinationCity;
        private Long scheduleId;

        @JsonFormat(pattern = "HH:mm:ss")
        private LocalTime departureTime;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate tripDate;

        private PickupType pickupType;
        private Long pickupOfficeId;
        private String pickupOfficeName;
        private String pickupAddress;

        private PickupType dropoffType;
        private Long dropoffOfficeId;
        private String dropoffOfficeName;
        private String dropoffAddress;

        private Integer passengerCount;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String status;
    }
}
