package com.dotnt.booking_travel_backend.booking.internal.repository;

import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
    List<Booking> findAllByDeletedAtIsNullOrderByCreatedAtDesc();
    List<Booking> findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);
}
