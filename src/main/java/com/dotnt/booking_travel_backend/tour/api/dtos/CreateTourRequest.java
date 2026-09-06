package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTourRequest {
	private int durationDays;
	private int durationNights;
	private int minGuest;
	private int maxGuest;
	private boolean active;
	private String status;
	private String inclusions;
	private String exclusions;
	private List<TourDayDto> tourDays;
	private Long tourCategoryId;
	private Long thumbnailId;
	private List<Long> imageIds;
	private List<TourOptionDto> options;
	private List<TourTranslationDto> translations;
	private List<TourPriceDto> prices;
	private List<TourPackageDto> packages;
}
