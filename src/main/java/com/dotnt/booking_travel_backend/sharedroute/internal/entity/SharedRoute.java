package com.dotnt.booking_travel_backend.sharedroute.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shared_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRoute extends MasterEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "vehicle_name", length = 100)
    @Builder.Default
    private String vehicleName = "Dcar Limousine";

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;

    @Column(name = "slug", unique = true, length = 255)
    private String slug;

    @Column(name = "linked_intercity_route_id")
    private Long linkedIntercityRouteId;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SharedRouteDirection> directions = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void ensureSlug() {
        if (this.slug == null || this.slug.trim().isEmpty()) {
            String base = name != null ? name : "shared-route";
            String combined = Normalizer.normalize(base, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-+|-+$", "");
            this.slug = combined.isEmpty() ? "shared-route-" + System.currentTimeMillis() : combined;
        }
    }
}
