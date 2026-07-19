package com.dotnt.booking_travel_backend.rental.internal.repository;

import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityDepartureTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntercityDepartureTimeRepository extends JpaRepository<IntercityDepartureTime, Long> {
    List<IntercityDepartureTime> findByRouteIdAndActiveTrueOrderByOrderIndexAsc(Long routeId);

    List<IntercityDepartureTime> findByRouteIdOrderByOrderIndexAsc(Long routeId);
}
