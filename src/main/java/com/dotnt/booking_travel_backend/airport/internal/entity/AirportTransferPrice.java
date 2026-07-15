package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "airport_transfer_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportTransferPrice extends MasterEntity {
    @Column(name = "airport_transfer_id", nullable = false)
    private Long airportTransferId;

    @ManyToOne
    @JoinColumn(name = "vehicle_category_id")
    private VehicleCategory vehicleCategory;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}

