package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourDayActivityTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourDayActivityTranslationRepository extends JpaRepository<TourDayActivityTranslation, Long> {
	java.util.List<com.dotnt.booking_travel_backend.tour.internal.entity.TourDayActivityTranslation> findByTourDayActivityId(Long tourDayActivityId);
}
