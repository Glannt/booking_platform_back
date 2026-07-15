package com.dotnt.booking_travel_backend.airport.internal.repository;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportBookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AirportBookingDetailRepository extends JpaRepository<AirportBookingDetail, Long> {
    Optional<AirportBookingDetail> findByBookingItemId(Long bookingItemId);
}
