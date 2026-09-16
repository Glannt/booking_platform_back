package com.dotnt.booking_travel_backend.fleet.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addon_categories")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"translations", "addOns"})
@ToString(exclude = {"translations", "addOns"})
@Builder
public class AddOnCategory extends MasterEntity {

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

    @Column(name = "is_filterable", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean isFilterable = false;

    @OneToMany(mappedBy = "addonCategory", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<AddOnCategoryTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<AddOn> addOns = new ArrayList<>();

    // Domain helper methods
    public void addTranslation(AddOnCategoryTranslation translation) {
        translations.add(translation);
        translation.setAddonCategory(this);
    }

    public void removeTranslation(AddOnCategoryTranslation translation) {
        translations.remove(translation);
        translation.setAddonCategory(null);
    }

    public AddOnCategoryTranslation getTranslation(String locale) {
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
