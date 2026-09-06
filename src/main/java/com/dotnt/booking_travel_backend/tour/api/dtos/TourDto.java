package com.dotnt.booking_travel_backend.tour.api.dtos;

import com.dotnt.booking_travel_backend.media.api.MediaDto;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDto {
    private Long id;
    private int durationDays;
    private int durationNights;
    private int minGuest;
    private int maxGuest;
    private boolean active;
    private String status;
    private String inclusions;
    private String exclusions;
    private Long tourCategoryId;
    private TourCategoryDTO tourCategory;
    private Long thumbnailId;
    private List<Long> imageIds;
    private List<MediaDto> images;
    private List<TourDayDto> days;
    private List<TourOptionDto> options;
    private List<TourTranslationDto> translations;
    private List<TourPriceDto> prices;
    private List<TourPackageDto> packages;
}
