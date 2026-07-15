package com.dotnt.booking_travel_backend.airport.internal.repository;

import com.dotnt.booking_travel_backend.airport.internal.entity.AirportTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportTranslationRepository extends JpaRepository<AirportTranslation, Long> {
    List<AirportTranslation> findByAirportId(Long airportId);
    AirportTranslation findByAirportIdAndLocale(Long airportId, String locale);
}
