package com.dotnt.booking_travel_backend.fleet.api.dtos;

import com.dotnt.booking_travel_backend.media.api.MediaDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDTO {
    private Long id;
    private String brand;
    private String model;
    private int year;
    private String fuelType;
    private boolean active;
    private int luggage;
    private String luggageUnit;
    private Long thumbnailId;
    private String thumbnailUrl;
    private List<Long> imageIds;
    private List<MediaDto> images;
    private VehicleCategoryDTO type;
}
