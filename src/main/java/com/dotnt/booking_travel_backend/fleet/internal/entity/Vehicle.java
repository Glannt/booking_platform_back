package com.dotnt.booking_travel_backend.fleet.internal.entity;

import com.dotnt.booking_travel_backend.media.internal.entity.Image;
import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Builder
public class Vehicle extends MasterEntity {
    @Column(name = "brand")
    private String brand;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "year", nullable = true)
    private int year;

    @Column(name = "fuel_type", nullable = false)
    private String fuelType;

    @Column(name = "active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @Column(name = "can_rent", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean canRent;

    @Column(name = "luggage", nullable = true)
    private int luggage;

    @Column(name = "luggage_unit", nullable = true, length = 50)
    private String luggageUnit;

    @Column(name = "luggage_weight", nullable = true)
    private Integer luggageWeight;

    @Column(name = "thumbnail_id", nullable = true)
    private Long thumbnailId;

    @ManyToMany
    @JoinTable(
        name = "vehicle_images",
        joinColumns = @JoinColumn(name = "vehicle_id"),
        inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    @Builder.Default
    private List<Image> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_category_id", nullable = false)
    @JsonIgnoreProperties({"vehicles"})
    private VehicleCategory category;
}
