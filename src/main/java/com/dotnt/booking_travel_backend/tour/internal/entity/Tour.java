package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.media.internal.entity.Image;
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

    @ManyToMany
    @JoinTable(
        name = "tour_images",
        joinColumns = @JoinColumn(name = "tour_id"),
        inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    @Builder.Default
    private List<Image> images = new ArrayList<>();

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TourOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TourTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TourDay> days = new ArrayList<>();
}
