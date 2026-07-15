package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "surcharge_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurchargeRule extends MasterEntity {
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "country", nullable = false, length = 10)
    private String country;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @OneToMany(mappedBy = "surchargeRule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SurchargeRuleVehicle> vehicles = new ArrayList<>();
}
