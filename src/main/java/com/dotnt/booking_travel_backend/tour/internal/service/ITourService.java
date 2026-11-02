package com.dotnt.booking_travel_backend.tour.internal.service;

import com.dotnt.booking_travel_backend.tour.api.dtos.CreateTourRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITourService {
	com.dotnt.booking_travel_backend.tour.api.dtos.TourDto createTour(CreateTourRequest request);

	Page<com.dotnt.booking_travel_backend.tour.api.dtos.TourDto> getTours(Pageable pageable, String search, Long tourCategoryId);

	com.dotnt.booking_travel_backend.tour.api.dtos.TourDto getTour(Long id);

	com.dotnt.booking_travel_backend.tour.api.dtos.TourDto getTourBySlug(String slug);

	com.dotnt.booking_travel_backend.tour.api.dtos.TourDto updateTour(Long id, com.dotnt.booking_travel_backend.tour.api.dtos.TourDto tourDto);

	void deleteTour(Long id);
}
