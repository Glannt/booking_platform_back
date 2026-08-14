package com.dotnt.booking_travel_backend.surcharge.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleSurchargeMappingDto {
    private Long id;

    @NotBlank(message = "Module code is required")
    private String moduleCode;

    @NotNull(message = "Surcharge rule ID is required")
    private Long surchargeRuleId;

    private SurchargeRuleDto surchargeRule;

    private Integer applyOrder;

    private boolean isOptional;

    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
