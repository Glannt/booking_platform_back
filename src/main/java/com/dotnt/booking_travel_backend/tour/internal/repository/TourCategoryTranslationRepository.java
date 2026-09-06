package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourCategoryTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TourCategoryTranslationRepository extends JpaRepository<TourCategoryTranslation, Long> {
    List<TourCategoryTranslation> findByTourCategoryId(Long tourCategoryId);
    Optional<TourCategoryTranslation> findByTourCategoryIdAndLocale(Long tourCategoryId, String locale);
    Optional<TourCategoryTranslation> findBySlug(String slug);
}
