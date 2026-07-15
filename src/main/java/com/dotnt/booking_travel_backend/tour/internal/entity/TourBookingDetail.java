package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "tour_booking_details")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class TourBookingDetail extends MasterEntity {

    @Column(name = "booking_item_id", nullable = true)
    private Long bookingItemId;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;
    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "meeting_point")
    private String meetingPoint;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;
}
