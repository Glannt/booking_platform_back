package com.dotnt.booking_travel_backend.rental.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental extends MasterEntity {
    // No service_id as requested

    @Column(name = "rental_type_id")
    private Long rentalTypeId;

    @Column(name = "vehicle_category_id")
    private Long vehicleCategoryId;

    @Column(name = "driver_required")
    private boolean driverRequired;

    @Column(name = "deposit", precision = 15, scale = 2)
    private BigDecimal deposit;

    @Column(name = "minimum_days")
    private Integer minimumDays;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}


