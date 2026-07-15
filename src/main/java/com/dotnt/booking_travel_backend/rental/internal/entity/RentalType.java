package com.dotnt.booking_travel_backend.rental.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "rental_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalType extends MasterEntity {
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}

