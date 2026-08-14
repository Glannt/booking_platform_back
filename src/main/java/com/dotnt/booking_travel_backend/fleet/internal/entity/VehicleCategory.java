package com.dotnt.booking_travel_backend.fleet.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicle_categories")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"translations"})
@ToString(exclude = {"translations"})
@Builder
public class VehicleCategory extends MasterEntity {
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "min_seat", nullable = false)
    private Integer minSeat;

    @Column(name = "max_seat", nullable = false)
    private Integer maxSeat;

    @Column(name = "min_luggage", nullable = true)
    private Integer minLuggage;

    @Column(name = "max_luggage", nullable = true)
    private Integer maxLuggage;

    @Column(name = "unit_luggage", nullable = true, length = 50)
    private String unitLuggage;

    @Column(name = "luggage_weight", nullable = true)
    private Integer luggageWeight;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @Column(name = "is_default", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDefault;

    @Column(name = "thumbnail_id", nullable = true)
    private Long thumbnailId;

    @OneToMany(mappedBy = "vehicleCategory", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    private List<VehicleCategoryTranslation> translations = new ArrayList<>();
}
