package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "tour_day_activities")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class TourDayActivity extends MasterEntity {
    @ManyToOne
    @JoinColumn(name = "tour_day_id", nullable = false)
    private TourDay tourDay;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @OneToMany(mappedBy = "tourDayActivity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<TourDayActivityTranslation> translations = new java.util.ArrayList<>();
}
