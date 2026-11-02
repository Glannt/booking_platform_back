package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tour_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourPrice extends MasterEntity {
    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "vehicle_category_id")
    private Long vehicleCategoryId;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}


