package com.dotnt.booking_travel_backend.fleet.internal.repository;

import com.dotnt.booking_travel_backend.fleet.internal.entity.AmenityCategoryTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmenityCategoryTranslationRepository extends JpaRepository<AmenityCategoryTranslation, Long> {
    Optional<AmenityCategoryTranslation> findByAmenityCategoryIdAndLocale(Long amenityCategoryId, String locale);
    List<AmenityCategoryTranslation> findByAmenityCategoryId(Long amenityCategoryId);
}
