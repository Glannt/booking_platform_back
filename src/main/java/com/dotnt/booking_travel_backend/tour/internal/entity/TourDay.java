package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_days")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class TourDay extends MasterEntity {
    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "day_number", nullable = false)
    private int dayNumber;

    @OneToMany(mappedBy = "tourDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<TourDayTranslation> translations = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "tourDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<TourDayActivity> activities = new java.util.ArrayList<>();
}
