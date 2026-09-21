package com.dotnt.booking_travel_backend.intercity.internal.entity;

import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
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

    @Column(name = "pricing_type", length = 20)
    @Builder.Default
    private String pricingType = "PER_KM";

    @Column(name = "fixed_price_one_way", precision = 15, scale = 2)
    private BigDecimal fixedPriceOneWay;

    @Column(name = "fixed_price_round_trip", precision = 15, scale = 2)
    private BigDecimal fixedPriceRoundTrip;

    @Column(name = "round_trip_price", precision = 15, scale = 2)
    private BigDecimal roundTripPrice;

    @Column(name = "round_trip_per_km_amount", precision = 15, scale = 2)
    private BigDecimal roundTripPerKmAmount;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Embedded
    private DepositPolicy depositPolicy;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}