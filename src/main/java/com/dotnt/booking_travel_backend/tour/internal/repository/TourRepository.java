package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.Tour;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    Page<Tour> findByCategoryId(Long categoryId, Pageable pageable);

    Optional<Tour> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @org.springframework.data.jpa.repository.Query(value = """
        SELECT tp.tour_id, 
               COUNT(DISTINCT b.id) AS successful_bookings, 
               COALESCE(SUM(bi.total_price), 0) AS total_revenue
        FROM booking_items bi
        JOIN bookings b ON bi.booking_id = b.id
        JOIN tour_booking_details tbd ON bi.tour_booking_detail_id = tbd.id
        JOIN tour_packages tp ON tbd.tour_package_id = tp.id
        WHERE UPPER(b.booking_status) IN ('CONFIRMED', 'COMPLETED', 'PAID')
          AND COALESCE(bi.is_contact_only, FALSE) = FALSE
        GROUP BY tp.tour_id
        ORDER BY successful_bookings DESC, total_revenue DESC
    """, nativeQuery = true)
    java.util.List<Object[]> findTourBookingStats();
}

