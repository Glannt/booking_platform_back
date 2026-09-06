package com.dotnt.booking_travel_backend.tour.api.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourPackageDto {
    private Long id;
    private Long tourId;
    private String name;
    private String pricingType; // PER_PERSON, PRIVATE_CHARTER, CONTACT
    private BigDecimal adultPrice;
    private BigDecimal childPrice;
    private BigDecimal infantPrice;
    private String childPolicyNote;
    private String infantPolicyNote;
    private BigDecimal fixedPrice;
    private boolean isContactOnly;
    private int minGuests;
    private int maxGuests;
    private String highlights;
    private String inclusions;
    private String exclusions;
    private boolean active;
    private int sortOrder;

    @Builder.Default
    private List<TourPackageTranslationDto> translations = new ArrayList<>();
}
