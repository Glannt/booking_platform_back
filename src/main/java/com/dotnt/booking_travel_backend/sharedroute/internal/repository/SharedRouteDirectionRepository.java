package com.dotnt.booking_travel_backend.sharedroute.internal.repository;

import com.dotnt.booking_travel_backend.sharedroute.internal.entity.DirectionType;
import com.dotnt.booking_travel_backend.sharedroute.internal.entity.SharedRouteDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedRouteDirectionRepository extends JpaRepository<SharedRouteDirection, Long> {

    List<SharedRouteDirection> findByRouteId(Long routeId);

    Optional<SharedRouteDirection> findByRouteIdAndDirectionType(Long routeId, DirectionType directionType);

    List<SharedRouteDirection> findByOriginCityAndDestinationCityAndActiveTrue(String originCity, String destinationCity);

    @Query("SELECT d FROM SharedRouteDirection d " +
           "LEFT JOIN FETCH d.originOffices " +
           "LEFT JOIN FETCH d.destinationOffices " +
           "WHERE d.id = :id")
    Optional<SharedRouteDirection> findByIdWithOffices(@Param("id") Long id);
}
