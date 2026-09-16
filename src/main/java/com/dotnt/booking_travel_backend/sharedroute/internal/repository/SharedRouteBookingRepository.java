package com.dotnt.booking_travel_backend.sharedroute.internal.repository;

import com.dotnt.booking_travel_backend.sharedroute.internal.entity.SharedRouteBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SharedRouteBookingRepository extends JpaRepository<SharedRouteBooking, Long> {

    List<SharedRouteBooking> findByBookingGroupId(Long bookingGroupId);

    List<SharedRouteBooking> findByTripDate(LocalDate tripDate);

    List<SharedRouteBooking> findAllByOrderByCreatedAtDesc();
}
