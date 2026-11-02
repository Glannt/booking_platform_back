package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourDayTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourDayTranslationRepository extends JpaRepository<TourDayTranslation, Long> {
	java.util.List<TourDayTranslation> findByTourDayId(Long tourDayId);
}
