package com.dotnt.booking_travel_backend.sharedroute.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shared_route_directions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRouteDirection extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private SharedRoute route;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction_type", nullable = false, length = 20)
    private DirectionType directionType;

    @Column(name = "origin_city", nullable = false, length = 100)
    private String originCity;

    @Column(name = "destination_city", nullable = false, length = 100)
    private String destinationCity;

    @Column(name = "origin_lat", precision = 10, scale = 7)
    private BigDecimal originLat;

    @Column(name = "origin_lng", precision = 10, scale = 7)
    private BigDecimal originLng;

    @Column(name = "destination_lat", precision = 10, scale = 7)
    private BigDecimal destinationLat;

    @Column(name = "destination_lng", precision = 10, scale = 7)
    private BigDecimal destinationLng;

    @ManyToMany
    @JoinTable(
        name = "shared_route_direction_origin_offices",
        joinColumns = @JoinColumn(name = "direction_id"),
        inverseJoinColumns = @JoinColumn(name = "office_id")
    )
    @Builder.Default
    private List<SharedOffice> originOffices = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "shared_route_direction_destination_offices",
        joinColumns = @JoinColumn(name = "direction_id"),
        inverseJoinColumns = @JoinColumn(name = "office_id")
    )
    @Builder.Default
    private List<SharedOffice> destinationOffices = new ArrayList<>();

    @Column(name = "price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal price = new BigDecimal("250000.00");

    @Column(name = "distance_km", precision = 8, scale = 1)
    private BigDecimal distanceKm;

    @Column(name = "estimated_hours", precision = 5, scale = 1)
    private BigDecimal estimatedHours;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "direction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SharedRouteSchedule> schedules = new ArrayList<>();
}
