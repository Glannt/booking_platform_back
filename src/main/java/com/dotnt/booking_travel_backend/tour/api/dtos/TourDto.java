package com.dotnt.booking_travel_backend.tour.api.dtos;

import com.dotnt.booking_travel_backend.media.api.MediaDto;
import com.dotnt.booking_travel_backend.shared.component.Component;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDto {
    private Long id;

    @Builder.Default
    private Integer durationDays = 1;

    @Builder.Default
    private Integer durationNights = 0;

    @Builder.Default
    private Integer minGuest = 1;

    @Builder.Default
    private Integer maxGuest = 20;

    @Builder.Default
    private Boolean active = true;

    private String status;
    private String slug;
    private String inclusions;
    private String exclusions;
    private List<Component> components;
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

    @Builder.Default
    private Boolean isRecommended = false;
    private Long successfulBookingsCount;
    private java.math.BigDecimal totalRevenue;

    @Builder.Default
    private String depositType = "PERCENT";
    @Builder.Default
    private BigDecimal depositRate = new BigDecimal("30.00");
    private BigDecimal depositAmount;

    @JsonIgnore
    public boolean isActive() {
        return active == null || active;
    }
}
