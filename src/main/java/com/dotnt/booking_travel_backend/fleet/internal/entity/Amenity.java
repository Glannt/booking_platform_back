package com.dotnt.booking_travel_backend.fleet.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "amenities")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"translations", "vehicles", "category"})
@ToString(exclude = {"translations", "vehicles", "category"})
@Builder
public class Amenity extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "amenity_category_id", nullable = false)
    private AmenityCategory category;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "icon", columnDefinition = "TEXT")
    private String icon;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "amenity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<AmenityTranslation> translations = new ArrayList<>();

    @ManyToMany(mappedBy = "amenities", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Vehicle> vehicles = new ArrayList<>();

    // Domain helper methods
    public void addTranslation(AmenityTranslation translation) {
        translations.add(translation);
        translation.setAmenity(this);
    }

    public void removeTranslation(AmenityTranslation translation) {
        translations.remove(translation);
        translation.setAmenity(null);
    }

    public AmenityTranslation getTranslation(String locale) {
        if (translations == null || translations.isEmpty()) return null;
        String targetLocale = (locale == null || locale.isBlank()) ? "vi" : locale.toLowerCase();
        return translations.stream()
                .filter(t -> targetLocale.equalsIgnoreCase(t.getLocale()))
                .findFirst()
                .orElseGet(() -> translations.stream()
                        .filter(t -> "vi".equalsIgnoreCase(t.getLocale()))
                        .findFirst()
                        .orElse(translations.get(0)));
    }
}
