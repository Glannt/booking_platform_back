package com.dotnt.booking_travel_backend.airport.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "airport_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportRoute extends MasterEntity {
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_id")
    private Airport airport;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}

