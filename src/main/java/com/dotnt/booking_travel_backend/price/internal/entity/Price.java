package com.dotnt.booking_travel_backend.price.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Price extends MasterEntity {
    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType; // TOUR | RENTAL | AIRPORT | PROVINCE

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "vehicle_category_id")
    private Long vehicleCategoryId;

    @Column(name = "pricing_unit", length = 50)
    private String pricingUnit;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "effective_from")
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}

