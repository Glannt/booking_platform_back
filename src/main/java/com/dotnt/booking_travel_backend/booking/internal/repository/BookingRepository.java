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

    /**
     * Revenue = sum of booking totals, excluding quote-only bookings (every line item
     * contact-only). Contact-only items total 0đ by construction, but the item flag is the
     * permanent stamp: unticking contact-only later can never turn an old quote into revenue.
     */
    @org.springframework.data.jpa.repository.Query(value = """
        SELECT COALESCE(SUM(b.total_amount), 0)
        FROM bookings b
        WHERE b.deleted_at IS NULL
          AND EXISTS (
            SELECT 1 FROM booking_items bi
            WHERE bi.booking_id = b.id
              AND COALESCE(bi.is_contact_only, FALSE) = FALSE
          )
        """, nativeQuery = true)
    java.math.BigDecimal sumTotalAmountExcludingContactOnly();
}
