package com.dotnt.booking_travel_backend.booking.internal.repository;

import com.dotnt.booking_travel_backend.booking.internal.entity.BookingTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingTimelineEventRepository extends JpaRepository<BookingTimelineEvent, Long> {
    List<BookingTimelineEvent> findByBookingIdOrderByCreatedAtAsc(Long bookingId);
}
