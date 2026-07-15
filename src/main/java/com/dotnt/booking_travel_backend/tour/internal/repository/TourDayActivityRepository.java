package com.dotnt.booking_travel_backend.tour.internal.repository;

import com.dotnt.booking_travel_backend.tour.internal.entity.TourDayActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourDayActivityRepository extends JpaRepository<TourDayActivity, Long> {
	java.util.List<TourDayActivity> findByTourDayId(Long tourDayId);
}
