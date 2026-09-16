package com.dotnt.booking_travel_backend.tour.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "tour_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourConfig extends MasterEntity {

    @Column(name = "recommended_limit", nullable = false)
    @Builder.Default
    private int recommendedLimit = 6;

    @Column(name = "auto_recommend_enabled", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean autoRecommendEnabled = true;
}
