package com.dotnt.booking_travel_backend.intercity.internal.repository;

import com.dotnt.booking_travel_backend.intercity.internal.entity.IntercityDepartureTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntercityDepartureTimeRepository extends JpaRepository<IntercityDepartureTime, Long> {
    List<IntercityDepartureTime> findByRouteIdAndActiveTrueOrderByOrderIndexAsc(Long routeId);

    List<IntercityDepartureTime> findByRouteIdOrderByOrderIndexAsc(Long routeId);

    List<IntercityDepartureTime> findByRouteIdAndDirectionAndActiveTrueOrderByOrderIndexAsc(Long routeId, String direction);

    List<IntercityDepartureTime> findByRouteIdAndDirectionOrderByOrderIndexAsc(Long routeId, String direction);
}
