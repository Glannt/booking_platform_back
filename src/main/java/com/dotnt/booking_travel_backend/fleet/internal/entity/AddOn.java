package com.dotnt.booking_travel_backend.fleet.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "add_ons")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"translations", "vehicles", "vehicleCategories", "category"})
@ToString(exclude = {"translations", "vehicles", "vehicleCategories", "category"})
@Builder
public class AddOn extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addon_category_id", nullable = false)
    private AddOnCategory category;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "value", length = 255)
    private String value;

    @Column(name = "icon", columnDefinition = "TEXT")
    private String icon;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "addOn", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<AddOnTranslation> translations = new ArrayList<>();

    @ManyToMany(mappedBy = "addOns", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Vehicle> vehicles = new ArrayList<>();

    @ManyToMany(mappedBy = "addOns", fetch = FetchType.LAZY)
    @Builder.Default
    private List<VehicleCategory> vehicleCategories = new ArrayList<>();

    // Domain helper methods
    public void addTranslation(AddOnTranslation translation) {
        translations.add(translation);
        translation.setAddOn(this);
    }

    public void removeTranslation(AddOnTranslation translation) {
        translations.remove(translation);
        translation.setAddOn(null);
    }

    public AddOnTranslation getTranslation(String locale) {
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
