package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.internal.entity.AmenityTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmenityTranslationRepository extends JpaRepository<AmenityTranslation, Long> {
    Optional<AmenityTranslation> findByAmenityIdAndLocale(Long amenityId, String locale);
    List<AmenityTranslation> findByAmenityId(Long amenityId);
}
