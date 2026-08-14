package com.dotnt.booking_travel_backend.rental.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "intercity_route_prices",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"route_id", "vehicle_category_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityRoutePrice extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private IntercityRoute route;

    @Column(name = "vehicle_category_id", nullable = false)
    private Long vehicleCategoryId;

    @Column(name = "per_km_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal perKmAmount;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}
