package com.dotnt.booking_travel_backend.rental.internal.repository;

import com.dotnt.booking_travel_backend.rental.internal.entity.IntercitySeasonalSurcharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IntercitySeasonalSurchargeRepository extends JpaRepository<IntercitySeasonalSurcharge, Long> {

    @Query("SELECT s FROM IntercitySeasonalSurcharge s WHERE s.active = true AND s.startDate <= :date AND s.endDate >= :date AND (s.routeId IS NULL OR s.routeId = :routeId)")
    List<IntercitySeasonalSurcharge> findActiveByDateAndRoute(@Param("date") LocalDate date, @Param("routeId") Long routeId);
}
