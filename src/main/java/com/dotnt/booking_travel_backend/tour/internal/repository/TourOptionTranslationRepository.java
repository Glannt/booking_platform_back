package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourOptionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourOptionTranslationRepository extends JpaRepository<TourOptionTranslation, Long> {
    List<TourOptionTranslation> findByTourOptionId(Long tourOptionId);
}
