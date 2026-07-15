package com.dotnt.booking_travel_backend.airport.internal.repository;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTransferPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportTransferPriceRepository extends JpaRepository<AirportTransferPrice, Long> {
    List<AirportTransferPrice> findByAirportTransferId(Long airportTransferId);
}

