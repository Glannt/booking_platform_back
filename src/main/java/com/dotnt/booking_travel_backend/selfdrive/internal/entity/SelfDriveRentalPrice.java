package com.dotnt.booking_travel_backend.selfdrive.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "self_drive_rental_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfDriveRentalPrice extends MasterEntity {
    @Column(name = "rental_id", nullable = false)
    private Long rentalId;

    @Column(name = "vehicle_category_id")
    private Long vehicleCategoryId;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}
