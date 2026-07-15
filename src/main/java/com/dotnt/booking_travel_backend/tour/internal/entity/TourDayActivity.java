package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

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
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

}
