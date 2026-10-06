package com.dotnt.booking_travel_backend.tour.api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String childPolicyNote;
    private String infantPolicyNote;
    private String pricingType;
    private BigDecimal adultPrice;
    private BigDecimal childPrice;
    private BigDecimal infantPrice;

    // Chưa cấu hình = null (không default, không prefill): PERCENT tính trên tổng tiền vé sau
    // discount, FIXED là số tiền cố định. Admin tự nhập.
    private String depositType;
    private BigDecimal depositRate;
    private BigDecimal depositAmount;

    @Builder.Default
    private List<PriceListPerPersonDto> prices = new ArrayList<>();

    @JsonProperty("isContactOnly")
    @JsonAlias({"contactOnly", "isContactOnly"})
    @Builder.Default
    private Boolean isContactOnly = false;

    @Builder.Default
    private Integer minGuests = 1;

    @Builder.Default
    private Integer maxGuests = 30;

    private String highlights;
    private String inclusions;
    private String exclusions;

    @JsonProperty("active")
    @JsonAlias({"isActive", "active"})
    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private List<TourPackageTranslationDto> translations = new ArrayList<>();

    @JsonIgnore
    public boolean isContactOnly() {
        return Boolean.TRUE.equals(this.isContactOnly);
    }

    @JsonIgnore
    public boolean isActive() {
        return active == null || active;
    }
}
