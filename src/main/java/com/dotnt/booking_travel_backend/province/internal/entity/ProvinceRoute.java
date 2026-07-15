package com.dotnt.booking_travel_backend.province.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "province_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinceRoute extends MasterEntity {
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "origin", length = 255)
    private String origin;

    @Column(name = "destination", length = 255)
    private String destination;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;
}

