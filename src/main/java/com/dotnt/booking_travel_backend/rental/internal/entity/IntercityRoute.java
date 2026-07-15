package com.dotnt.booking_travel_backend.rental.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "intercity_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityRoute extends MasterEntity {

    @Column(name = "origin_city", nullable = false, length = 100)
    private String originCity;

    @Column(name = "destination_city", nullable = false, length = 100)
    private String destinationCity;

    @Column(name = "distance_km", precision = 8, scale = 1)
    private BigDecimal distanceKm;

    @Column(name = "estimated_hours", precision = 5, scale = 1)
    private BigDecimal estimatedHours;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntercityRouteTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntercityRoutePrice> prices = new ArrayList<>();
}
