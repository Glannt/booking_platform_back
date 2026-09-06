package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tour_packages")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"tour", "translations"})
@ToString(exclude = {"tour", "translations"})
@Builder
public class TourPackage extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "pricing_type", nullable = false, length = 50)
    @Builder.Default
    private String pricingType = "PER_PERSON";

    @Column(name = "adult_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal adultPrice = BigDecimal.ZERO;

    @Column(name = "child_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal childPrice = BigDecimal.ZERO;

    @Column(name = "infant_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal infantPrice = BigDecimal.ZERO;

    @Column(name = "child_policy_note")
    @Builder.Default
    private String childPolicyNote = "Trẻ em 1m–1m29";

    @Column(name = "infant_policy_note")
    @Builder.Default
    private String infantPolicyNote = "Trẻ em dưới 1m: Miễn phí";

    @Column(name = "fixed_price", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal fixedPrice = BigDecimal.ZERO;

    @Column(name = "is_contact_only")
    @Builder.Default
    private boolean isContactOnly = false;

    @Column(name = "min_guests")
    @Builder.Default
    private int minGuests = 1;

    @Column(name = "max_guests")
    @Builder.Default
    private int maxGuests = 30;

    @Column(name = "highlights", columnDefinition = "TEXT")
    private String highlights;

    @Column(name = "inclusions", columnDefinition = "TEXT")
    private String inclusions;

    @Column(name = "exclusions", columnDefinition = "TEXT")
    private String exclusions;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "sort_order")
    @Builder.Default
    private int sortOrder = 0;

    @OneToMany(mappedBy = "tourPackage", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<TourPackageTranslation> translations = new ArrayList<>();
}
