package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "airport_booking_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportBookingDetail extends MasterEntity {
    @Column(name = "booking_item_id", nullable = false)
    private Long bookingItemId;

    @Column(name = "flight_number", length = 50)
    private String flightNumber;

    @Column(name = "terminal", length = 50)
    private String terminal;

    @Column(name = "landing_time")
    private LocalDateTime landingTime;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Column(name = "passenger")
    private Integer passenger;

    @Column(name = "note", columnDefinition = "text")
    private String note;
}

