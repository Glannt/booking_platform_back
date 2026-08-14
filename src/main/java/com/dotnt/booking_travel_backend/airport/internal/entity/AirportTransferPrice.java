package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "airport_transfer_prices",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"airport_route_id", "vehicle_category_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportTransferPrice extends MasterEntity {
    @Column(name = "airport_route_id", nullable = false)
    private Long airportRouteId;

    @Column(name = "vehicle_category_id", nullable = false)
    private Long vehicleCategoryId;

    @Column(name = "base_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseKm;

    @Column(name = "base_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "per_km_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal perKmAmount;

    @Column(name = "fixed_overcharge", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean fixedOvercharge;

    @Column(name = "fee_overcharge_fixed", precision = 15, scale = 2)
    private BigDecimal feeOverchargeFixed;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}


