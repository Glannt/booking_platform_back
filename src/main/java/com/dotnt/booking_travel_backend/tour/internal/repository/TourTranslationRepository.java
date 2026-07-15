package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourTranslationRepository extends JpaRepository<TourTranslation, Long> {
    List<TourTranslation> findByTourId(Long tourId);
}
