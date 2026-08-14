package com.dotnt.booking_travel_backend.surcharge.api.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurchargeRuleDto {
    private Long id;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Calculation type is required")
    private String calculationType; // FIXED, PERCENT

    @NotNull(message = "Calculation base is required")
    private String calculationBase; // BASE_PRICE, RUNNING_TOTAL

    @NotNull(message = "Value is required")
    private BigDecimal value;

    private String currency;

    private Object condition;

    private Integer priority;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
