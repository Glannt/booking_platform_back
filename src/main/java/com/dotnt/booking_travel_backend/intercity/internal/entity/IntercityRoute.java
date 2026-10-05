package com.dotnt.booking_travel_backend.intercity.internal.entity;

import com.dotnt.booking_travel_backend.shared.component.Component;
import com.dotnt.booking_travel_backend.shared.component.ComponentListConverter;
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

    @ElementCollection
    @CollectionTable(name = "intercity_route_images", joinColumns = @JoinColumn(name = "route_id"))
    @Column(name = "image_id")
    @Builder.Default
    private List<Long> imageIds = new ArrayList<>();

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

    @Column(name = "is_contact_only", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean isContactOnly = false;

    @Column(name = "slug", unique = true, length = 255)
    private String slug;

    @Convert(converter = ComponentListConverter.class)
    @Column(name = "components", columnDefinition = "TEXT")
    @Builder.Default
    private List<Component> components = new ArrayList<>();

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntercityRouteTranslation> translations = new ArrayList<>();

    // ponytail: the intercity_route_prices table is left in place on purpose — dropping it
    // needs a migration and a data-export check. Nothing reads or writes it any more.

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IntercityDepartureTime> departureTimes = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void ensureSlug() {
        if (this.slug == null || this.slug.trim().isEmpty()) {
            String origin = originCity != null ? originCity : "origin";
            String dest = destinationCity != null ? destinationCity : "dest";
            String combined = java.text.Normalizer.normalize(origin + "-" + dest, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-+|-+$", "");
            this.slug = combined.isEmpty() ? "route-" + System.currentTimeMillis() : combined;
        }
    }
}
