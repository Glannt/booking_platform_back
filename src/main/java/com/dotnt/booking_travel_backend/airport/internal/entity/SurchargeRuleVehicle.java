package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "surcharge_rule_vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurchargeRuleVehicle extends MasterEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surcharge_rule_id", nullable = false)
    private SurchargeRule surchargeRule;

    @Column(name = "vehicle_category_id", nullable = false)
    private Long vehicleCategoryId;
}
