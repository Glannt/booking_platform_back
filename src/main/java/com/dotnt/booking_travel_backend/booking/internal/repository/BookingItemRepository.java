package com.dotnt.booking_travel_backend.booking.internal.repository;

import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {
}
