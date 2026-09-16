package com.dotnt.booking_travel_backend.fleet.internal.entity;

import com.dotnt.booking_travel_backend.shared.entity.MasterEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"imageIds", "category", "addOns", "amenities"})
@ToString(exclude = {"imageIds", "category", "addOns", "amenities"})
@Builder
public class Vehicle extends MasterEntity {
    @Column(name = "brand")
    private String brand;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "license_plate", length = 50)
    private String licensePlate;

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

    @Column(name = "thumbnail_id", nullable = true)
    private Long thumbnailId;

    @ElementCollection
    @CollectionTable(
        name = "vehicle_images",
        joinColumns = @JoinColumn(name = "vehicle_id")
    )
    @Column(name = "image_id")
    @BatchSize(size = 50)
    @Builder.Default
    private List<Long> imageIds = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_category_id", nullable = false)
    @JsonIgnoreProperties({"vehicles"})
    private VehicleCategory category;

    @ManyToMany
    @JoinTable(
        name = "vehicle_addons",
        joinColumns = @JoinColumn(name = "vehicle_id"),
        inverseJoinColumns = @JoinColumn(name = "addon_id")
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<AddOn> addOns = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "vehicle_amenities",
        joinColumns = @JoinColumn(name = "vehicle_id"),
        inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @BatchSize(size = 50)
    @Builder.Default
    private List<Amenity> amenities = new ArrayList<>();

    // Domain helper methods
    public void addAddOn(AddOn addOn) {
        if (!addOns.contains(addOn)) {
            addOns.add(addOn);
            if (!addOn.getVehicles().contains(this)) {
                addOn.getVehicles().add(this);
            }
        }
    }

    public void removeAddOn(AddOn addOn) {
        addOns.remove(addOn);
        addOn.getVehicles().remove(this);
    }

    public void addAmenity(Amenity amenity) {
        if (!amenities.contains(amenity)) {
            amenities.add(amenity);
            if (!amenity.getVehicles().contains(this)) {
                amenity.getVehicles().add(this);
            }
        }
    }

    public void removeAmenity(Amenity amenity) {
        amenities.remove(amenity);
        amenity.getVehicles().remove(this);
    }
}
