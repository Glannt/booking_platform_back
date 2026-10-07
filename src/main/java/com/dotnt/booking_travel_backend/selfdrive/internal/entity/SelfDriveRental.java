package com.dotnt.booking_travel_backend.selfdrive.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "self_drive_rentals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDriveRental extends MasterEntity {
    @Column(name = "rental_type_id")
    private Long rentalTypeId;

    @Column(name = "vehicle_category_id")
    private Long vehicleCategoryId;

    @Column(name = "driver_required")
    private boolean driverRequired;

    @Column(name = "deposit", precision = 15, scale = 2)
    private BigDecimal deposit;

    @Column(name = "deposit_type", length = 20)
    private String depositType; // FIXED, PERCENT

    @Column(name = "deposit_rate", precision = 5, scale = 2)
    private BigDecimal depositRate;

    @Column(name = "minimum_days")
    private Integer minimumDays;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @Column(name = "is_contact_only", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean isContactOnly = false;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}

