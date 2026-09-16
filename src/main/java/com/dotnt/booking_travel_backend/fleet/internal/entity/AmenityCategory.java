package com.dotnt.booking_travel_backend.fleet.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "amenity_categories")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"translations", "amenities"})
@ToString(exclude = {"translations", "amenities"})
@Builder
public class AmenityCategory extends MasterEntity {

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

    @OneToMany(mappedBy = "amenityCategory", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<AmenityCategoryTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<Amenity> amenities = new ArrayList<>();

    // Domain helper methods
    public void addTranslation(AmenityCategoryTranslation translation) {
        translations.add(translation);
        translation.setAmenityCategory(this);
    }

    public void removeTranslation(AmenityCategoryTranslation translation) {
        translations.remove(translation);
        translation.setAmenityCategory(null);
    }

    public AmenityCategoryTranslation getTranslation(String locale) {
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
