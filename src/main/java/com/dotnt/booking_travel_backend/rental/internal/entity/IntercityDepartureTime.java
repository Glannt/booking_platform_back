package com.dotnt.booking_travel_backend.rental.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "intercity_departure_times")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntercityDepartureTime extends MasterEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private IntercityRoute route;

    @Column(name = "departure_time", nullable = false, length = 5)
    private String departureTime;

    @Column(name = "direction", length = 20)
    @Builder.Default
    private String direction = "OUTBOUND";

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private int orderIndex = 0;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean active = true;
}
