package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "airport_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportRoute extends MasterEntity {
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "airport_name", length = 255)
    private String airportName;

    @Column(name = "pickup_location", length = 255)
    private String pickupLocation;

    @Column(name = "dropoff_location", length = 255)
    private String dropoffLocation;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "duration")
    private Integer duration; // minutes

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}

