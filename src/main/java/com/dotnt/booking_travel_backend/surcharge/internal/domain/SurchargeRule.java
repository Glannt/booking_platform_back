package com.dotnt.booking_travel_backend.surcharge.internal.domain;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "surcharge_rule", indexes = {
        @Index(name = "idx_surcharge_rule_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurchargeRule extends MasterEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 20)
    private CalculationType calculationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_base", nullable = false, length = 20)
    private CalculationBase calculationBase;

    @Column(name = "rule_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal value;

    @Column(name = "currency", length = 10)
    @Builder.Default
    private String currency = "VND";

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "condition_data", columnDefinition = "TEXT")
    private JsonNode condition;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
