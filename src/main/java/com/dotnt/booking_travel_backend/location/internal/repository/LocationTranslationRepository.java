package com.dotnt.booking_travel_backend.location.internal.repository;

import com.dotnt.booking_travel_backend.location.internal.entity.LocationTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationTranslationRepository extends JpaRepository<LocationTranslation, Long> {

    List<LocationTranslation> findByLocationId(Long locationId);

    Optional<LocationTranslation> findByLocationIdAndLocale(Long locationId, String locale);
}
