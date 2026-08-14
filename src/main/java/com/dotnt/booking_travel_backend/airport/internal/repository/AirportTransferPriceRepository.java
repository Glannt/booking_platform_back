package com.dotnt.booking_travel_backend.airport.internal.repository;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTransferPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportTransferPriceRepository extends JpaRepository<AirportTransferPrice, Long> {
    List<AirportTransferPrice> findByAirportRouteId(Long airportRouteId);
    Optional<AirportTransferPrice> findByAirportRouteIdAndVehicleCategoryId(Long airportRouteId, Long vehicleCategoryId);
    boolean existsByAirportRouteId(Long airportRouteId);
    boolean existsByVehicleCategoryId(Long vehicleCategoryId);
}

