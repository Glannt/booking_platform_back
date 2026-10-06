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
@EqualsAndHashCode(callSuper = true, exclude = {"tour", "translations", "prices"})
@ToString(exclude = {"tour", "translations", "prices"})
@Builder
public class TourPackage extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "tour_package_price_list_per_person",
        joinColumns = @JoinColumn(name = "tour_package_id"),
        inverseJoinColumns = @JoinColumn(name = "price_per_person_id")
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<PriceListPerPerson> prices = new ArrayList<>();

    @Column(name = "child_policy_note")
    @Builder.Default
    private String childPolicyNote = "Trẻ em 1m–1m29";

    @Column(name = "infant_policy_note")
    @Builder.Default
    private String infantPolicyNote = "Trẻ em dưới 1m: Miễn phí";

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

    /**
     * Whether this package may be switched on, i.e. whether it can actually be sold.
     *
     * <p>Price does not live on {@code tour_packages} — it lives in {@code price_list_per_person},
     * one row per passenger type. "Has a price" therefore means an <b>active ADULT row with a
     * figure above zero</b>: infant is free by policy and child pricing follows the itinerary, so
     * neither can stand in for an adult price. A 0đ adult row is not a price, it is a missing one.
     *
     * <p>A contact-only package is the deliberate exception: it is a quote request that still books,
     * and the booking row records that so it never reaches revenue reporting.
     *
     * <p>Single source of truth for the rule, called from both the create and the update path.
     */
    public boolean isPublishable() {
        if (isContactOnly) {
            return true;
        }
        return prices != null && prices.stream().anyMatch(price ->
                price.isActive()
                        && "ADULT".equalsIgnoreCase(price.getPassengerType())
                        && price.getPricePerPerson().signum() > 0);
    }
}
