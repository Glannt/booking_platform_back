package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.component.Component;
import com.dotnt.booking_travel_backend.shared.component.ComponentListConverter;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicies;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class Tour extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_category_id")
    private TourCategory category;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "duration_nights", nullable = false)
    private int durationNights;

    @Column(name = "min_guest", nullable = false)
    private int minGuest;

    @Column(name = "max_guest", nullable = false)
    private int maxGuest;

    @Column(name = "thumbnail_id")
    private Long thumbnailId;

    @ElementCollection
    @CollectionTable(
        name = "tour_images",
        joinColumns = @JoinColumn(name = "tour_id")
    )
    @Column(name = "image_id")
    @Builder.Default
    private List<Long> imageIds = new ArrayList<>();

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "slug", unique = true, length = 255)
    private String slug;

    @Column(name = "inclusions", columnDefinition = "TEXT")
    private String inclusions;

    @Column(name = "exclusions", columnDefinition = "TEXT")
    private String exclusions;

    @Column(name = "is_recommended")
    @Builder.Default
    private Boolean isRecommended = Boolean.FALSE;

    @Embedded
    private DepositPolicy depositPolicies;

    @Convert(converter = ComponentListConverter.class)
    @Column(name = "components", columnDefinition = "TEXT")
    @Builder.Default
    private List<Component> components = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TourPackage> packages = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TourOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TourTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TourDay> days = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void ensureSlug() {
        if (this.slug == null || this.slug.trim().isEmpty()) {
            if (this.translations != null && !this.translations.isEmpty()) {
                String candidate = this.translations.stream()
                        .filter(t -> t.getSlug() != null && !t.getSlug().trim().isEmpty())
                        .map(TourTranslation::getSlug)
                        .findFirst()
                        .orElse(null);
                if (candidate != null && !candidate.trim().isEmpty()) {
                    this.slug = candidate.trim();
                    return;
                }
            }
            this.slug = "tour-" + (getId() != null ? getId() : System.currentTimeMillis());
        }
    }
}
