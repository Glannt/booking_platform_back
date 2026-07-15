package com.dotnt.booking_travel_backend.booking.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flight_trackings")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class FlightTracking extends MasterEntity {

    @Column(name = "flight_number", nullable = false, unique = true, length = 50)
    private String flightNumber;

    @Column(name = "airline_name", length = 100)
    private String airlineName;

    @Column(name = "departure_airport", length = 100)
    private String departureAirport;

    @Column(name = "arrival_airport", length = 100)
    private String arrivalAirport;

    @Column(name = "scheduled_departure")
    private LocalDateTime scheduledDeparture;

    @Column(name = "scheduled_arrival")
    private LocalDateTime scheduledArrival;

    @Column(name = "actual_departure")
    private LocalDateTime actualDeparture;

    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

}
