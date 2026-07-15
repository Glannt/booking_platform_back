package com.dotnt.booking_travel_backend.rental.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import com.dotnt.booking_travel_backend.fleet.internal.entity.VehicleCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rental_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalPrice extends MasterEntity {
    @Column(name = "rental_id", nullable = false)
    private Long rentalId;

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

