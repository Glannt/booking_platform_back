package com.dotnt.booking_travel_backend.surcharge.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateSurchargeRequestDto {
    @NotBlank(message = "Module code is required")
    private String moduleCode;

    @NotNull(message = "Base price is required")
    private BigDecimal basePrice;

    private LocalDateTime serviceTime;
    private String zoneCode;
    private Long vehicleCategoryId;
    private Long routeId;
    private Integer passengerCount;
    private String tripType;
    private Map<String, Object> customAttributes;
}
