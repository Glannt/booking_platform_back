package com.dotnt.booking_travel_backend.booking.internal.repository;

import com.dotnt.booking_travel_backend.booking.internal.entity.FlightTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlightTrackingRepository extends JpaRepository<FlightTracking, Long> {
    Optional<FlightTracking> findByFlightNumber(String flightNumber);
}
