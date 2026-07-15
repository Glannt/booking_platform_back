package com.dotnt.booking_travel_backend.rental.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "intercity_seasonal_surcharges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercitySeasonalSurcharge extends MasterEntity {

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "season_name", nullable = false, length = 100)
    private String seasonName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "surcharge_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal surchargePercent;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}
