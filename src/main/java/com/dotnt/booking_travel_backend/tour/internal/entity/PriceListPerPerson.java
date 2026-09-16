package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "price_list_per_person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceListPerPerson extends MasterEntity {

    @Column(name = "passenger_type", length = 20, nullable = false)
    private String passengerType; // 'ADULT' | 'CHILD' | 'INFANT'

    @Column(name = "price_per_person", precision = 14, scale = 2, nullable = false)
    private BigDecimal pricePerPerson;

    @Column(name = "min_pax")
    @Builder.Default
    private Integer minPax = 1;

    @Column(name = "max_pax")
    private Integer maxPax;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;

    @Column(name = "apply_age_condition", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean applyAgeCondition = false;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "apply_height_condition", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean applyHeightCondition = false;

    @Column(name = "min_height")
    private Integer minHeight;

    @Column(name = "max_height")
    private Integer maxHeight;
}
