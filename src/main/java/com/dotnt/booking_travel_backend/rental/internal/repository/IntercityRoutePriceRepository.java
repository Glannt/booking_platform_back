package com.dotnt.booking_travel_backend.rental.internal.repository;

import com.dotnt.booking_travel_backend.rental.internal.entity.IntercityRoutePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntercityRoutePriceRepository extends JpaRepository<IntercityRoutePrice, Long> {
    List<IntercityRoutePrice> findByRouteId(Long routeId);

    List<IntercityRoutePrice> findByRouteIdAndActiveTrue(Long routeId);
}
