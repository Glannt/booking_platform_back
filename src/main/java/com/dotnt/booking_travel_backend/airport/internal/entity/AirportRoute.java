package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "airport_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportRoute extends MasterEntity {
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_id")
    private Airport airport;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "distance_km", precision = 8, scale = 1)
    private BigDecimal distanceKm;

    @Column(name = "origin_name", length = 255)
    private String originName;

    @Column(name = "destination_name", length = 255)
    private String destinationName;

    @Column(name = "origin_lat", precision = 10, scale = 7)
    private BigDecimal originLat;

    @Column(name = "origin_lng", precision = 10, scale = 7)
    private BigDecimal originLng;

    @Column(name = "destination_lat", precision = 10, scale = 7)
    private BigDecimal destinationLat;

    @Column(name = "destination_lng", precision = 10, scale = 7)
    private BigDecimal destinationLng;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}

