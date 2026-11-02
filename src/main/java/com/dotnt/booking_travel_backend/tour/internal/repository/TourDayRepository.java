package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourDayRepository extends JpaRepository<TourDay, Long> {
	java.util.List<TourDay> findByTourId(Long tourId);
}
