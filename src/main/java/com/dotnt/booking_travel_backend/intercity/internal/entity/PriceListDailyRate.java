package com.dotnt.booking_travel_backend.intercity.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "price_list_daily_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceListDailyRate extends MasterEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = false, unique = true)
    private PriceList priceList;

    @Column(name = "min_days", nullable = false)
    @Builder.Default
    private Integer minDays = 1;

    @Column(name = "max_days")
    private Integer maxDays;

    @Column(name = "km_included_per_day", precision = 10, scale = 2)
    private BigDecimal kmIncludedPerDay;

    @Column(name = "overage_price_per_km", precision = 15, scale = 2)
    private BigDecimal overagePricePerKm;
}
