package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "airport_transfer_prices",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"airport_route_id", "vehicle_category_id", "service_type"}))
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

    @Column(name = "service_type", nullable = false, length = 20)
    private String serviceType; // "ONE_WAY" | "ROUND_TRIP"

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}


