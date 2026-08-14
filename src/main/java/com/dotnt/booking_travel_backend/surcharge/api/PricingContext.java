package com.dotnt.booking_travel_backend.surcharge.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingContext {
    private BigDecimal basePrice;
    private LocalDateTime serviceTime;
    private String zoneCode;
    private Long vehicleCategoryId;
    private Long routeId;
    private Integer passengerCount;
    private String tripType;

    @Builder.Default
    private Map<String, Object> customAttributes = new HashMap<>();

    public LocalDate getServiceDate() {
        return serviceTime != null ? serviceTime.toLocalDate() : null;
    }

    public LocalTime getServiceLocalTime() {
        return serviceTime != null ? serviceTime.toLocalTime() : null;
    }
}
