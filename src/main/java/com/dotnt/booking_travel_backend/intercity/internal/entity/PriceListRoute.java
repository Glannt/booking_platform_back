package com.dotnt.booking_travel_backend.intercity.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "price_list_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceListRoute extends MasterEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = false, unique = true)
    private PriceList priceList;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "waiting_fee", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal waitingFee = BigDecimal.ZERO;

    @Column(name = "max_waiting_min")
    @Builder.Default
    private Integer maxWaitingMin = 0;
}
