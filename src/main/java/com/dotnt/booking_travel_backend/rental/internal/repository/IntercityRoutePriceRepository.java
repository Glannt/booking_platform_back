package com.dotnt.booking_travel_backend.rental.internal.repository;

import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoutePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntercityRoutePriceRepository extends JpaRepository<IntercityRoutePrice, Long> {
    List<IntercityRoutePrice> findByRouteId(Long routeId);

    List<IntercityRoutePrice> findByRouteIdAndActiveTrue(Long routeId);

    Optional<IntercityRoutePrice> findByRouteIdAndVehicleCategoryId(Long routeId, Long vehicleCategoryId);
}
