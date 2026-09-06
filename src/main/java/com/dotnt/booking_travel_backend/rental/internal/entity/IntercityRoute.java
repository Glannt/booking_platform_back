package com.dotnt.booking_travel_backend.rental.internal.entity;

import com.dotnt.booking_travel_backend.media.internal.entity.Image;
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

    @Column(name = "thumbnail_id")
    private Long thumbnailId;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @ManyToMany
    @JoinTable(
        name = "intercity_route_images",
        joinColumns = @JoinColumn(name = "route_id"),
        inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    @Builder.Default
    private List<Image> images = new ArrayList<>();

    @Column(name = "origin_city", nullable = false, length = 100)
    private String originCity;

    @Column(name = "destination_city", nullable = false, length = 100)
    private String destinationCity;

    @Column(name = "distance_km", precision = 8, scale = 1)
    private BigDecimal distanceKm;

    @Column(name = "estimated_hours", precision = 5, scale = 1)
    private BigDecimal estimatedHours;

    @Column(name = "origin_lat", precision = 10, scale = 7)
    private BigDecimal originLat;

    @Column(name = "origin_lng", precision = 10, scale = 7)
    private BigDecimal originLng;

    @Column(name = "destination_lat", precision = 10, scale = 7)
    private BigDecimal destinationLat;

    @Column(name = "destination_lng", precision = 10, scale = 7)
    private BigDecimal destinationLng;

    @Column(name = "origin_name", length = 255)
    private String originName;

    @Column(name = "destination_name", length = 255)
    private String destinationName;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @Column(name = "has_departure_times", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean hasDepartureTimes = false;

    @Column(name = "supports_shared", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean supportsShared = true;

    @Column(name = "shared_vehicle_name", length = 100)
    @Builder.Default
    private String sharedVehicleName = "Dcar Limousine";

    @Column(name = "shared_price_one_way", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal sharedPriceOneWay = new BigDecimal("250000.00");

    @Column(name = "shared_price_round_trip", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal sharedPriceRoundTrip = new BigDecimal("500000.00");

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntercityRouteTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntercityRoutePrice> prices = new ArrayList<>();

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntercityDepartureTime> departureTimes = new ArrayList<>();
}
